package com.example.NoMasAccidentes.service.profesional;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.profesional.RegistrarUbicacionRequest;
import com.example.NoMasAccidentes.dto.profesional.UbicacionProfesionalResponse;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.profesional.UbicacionProfesional;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.repository.profesional.UbicacionProfesionalRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class UbicacionProfesionalService {

    private static final long MINUTOS_UBICACION_ACTIVA = 30;

    //regla de negocio hora legal uso mapa
    private static final ZoneId ZONA_CHILE = ZoneId.of("America/Santiago");
    private static final LocalTime INICIO_JORNADA = LocalTime.of(8, 0);
    private static final LocalTime FIN_JORNADA = LocalTime.of(18, 0);

    private final ProfesionalRepository profesionalRepository;
    private final UbicacionProfesionalRepository ubicacionRepository;

    

    @Transactional
    public UbicacionProfesionalResponse registrarMiUbicacion(
            String emailUsuario,
            RegistrarUbicacionRequest request
    ) {
        if (!estaDentroDeJornadaLaboralChile()) {
                throw new ConflictoNegocioException(
                        "El seguimiento de ubicacion solo esta permitido de lunes a viernes entre 08:00 y 18:00, hora de Chile."
                );
        }
        Profesional profesional = profesionalRepository.findByUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Profesional asociado al usuario no encontrado"
                ));

        profesional.setLatitud(request.latitud());
        profesional.setLongitud(request.longitud());

        UbicacionProfesional ubicacion = UbicacionProfesional.builder()
                .profesional(profesional)
                .latitud(request.latitud())
                .longitud(request.longitud())
                .fechaRegistro(LocalDateTime.now())
                .build();

        UbicacionProfesional guardada = ubicacionRepository.save(ubicacion);

        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<UbicacionProfesionalResponse> listarUbicacionesActivas() {
        //regla de negocio hora legal uso mapa admin
        if (!estaDentroDeJornadaLaboralChile()){
                return List.of();
        }

        LocalDateTime desde = LocalDateTime.now().minusMinutes(MINUTOS_UBICACION_ACTIVA);

        return ubicacionRepository.findUltimasUbicacionesActivas(desde)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UbicacionProfesionalResponse obtenerMiUltimaUbicacion(String emailUsuario) {
        return ubicacionRepository.findTopByProfesionalUsuarioEmailOrderByFechaRegistroDesc(emailUsuario)
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Ubicacion del profesional no encontrada"
                ));
    }

    private UbicacionProfesionalResponse toResponse(UbicacionProfesional ubicacion) {
        Profesional p = ubicacion.getProfesional();

        return new UbicacionProfesionalResponse(
                p.getId(),
                p.getUsuario().getNombre() + " " + p.getUsuario().getApellido(),
                p.getUsuario().getEmail(),
                p.getEstado(),
                ubicacion.getLatitud(),
                ubicacion.getLongitud(),
                ubicacion.getFechaRegistro()
        );
    }

    //regla de negocio hora legal uso mapa
    private boolean estaDentroDeJornadaLaboralChile() {
        ZonedDateTime ahoraChile = ZonedDateTime.now(ZONA_CHILE);
        DayOfWeek dia = ahoraChile.getDayOfWeek();
        LocalTime hora = ahoraChile.toLocalTime();

        boolean diaLaboral = dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY;
        boolean dentroHorario = !hora.isBefore(INICIO_JORNADA) && hora.isBefore(FIN_JORNADA);

        return diaLaboral && dentroHorario;
    }
}