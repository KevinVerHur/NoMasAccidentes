package com.example.NoMasAccidentes.service.asesoria;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.asesoria.AsesoriaMapper;
import com.example.NoMasAccidentes.dto.asesoria.AsesoriaResponse;
import com.example.NoMasAccidentes.dto.asesoria.CrearAsesoriaRequest;
import com.example.NoMasAccidentes.model.asesoria.Asesoria;
import com.example.NoMasAccidentes.model.asesoria.EstadoAsesoria;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.repository.asesoria.AsesoriaRepository;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.service.empresa.EmpresaService;
import com.example.NoMasAccidentes.service.notificacion.NotificacionEventoService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de asesorías por accidentes o fiscalizaciones (RF22–RF25).
 * Ciclo: SOLICITADA -> EN_PROCESO -> CERRADA (o CANCELADA).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AsesoriaService {

    /** Asesorías incluidas en el plan por empresa al año (RF23). */
    private static final long ASESORIAS_INCLUIDAS_ANUALES = 10;

    private final AsesoriaRepository asesoriaRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfesionalRepository profesionalRepository;
    private final AsesoriaMapper asesoriaMapper;
    private final NotificacionEventoService notificacionEventoService;
    private final EmpresaService empresaService;

    /** Registra una asesoría (RF22) y determina si es extra según el límite anual (RF23, RF24). */
    @Transactional
    public AsesoriaResponse crear(CrearAsesoriaRequest request) {
        Empresa empresa = empresaRepository.findById(request.idEmpresa())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa", request.idEmpresa()));
        Profesional profesional = profesionalRepository.findById(request.idProfesional())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", request.idProfesional()));

        LocalDate hoy = LocalDate.now();
        boolean esExtra = excedeLimiteIncluidas(empresa.getId(), hoy.getYear());

        Asesoria asesoria = Asesoria.builder()
                .empresa(empresa)
                .profesional(profesional)
                .fechaSolicitud(hoy)
                .motivo(request.motivo())
                .tipo(request.tipo())
                .estado(EstadoAsesoria.SOLICITADA)
                .esAsesoriaExtra(esExtra)
                .build();

        Asesoria guardada = asesoriaRepository.save(asesoria);
        log.info("Asesoría creada id={} empresa={} tipo={} extra={} (RF22-RF24)",
                guardada.getId(), empresa.getId(), request.tipo(), esExtra);
        notificacionEventoService.notificarAsesoriaRegistrada(guardada);
        return asesoriaMapper.toResponse(guardada);
    }

    /** El profesional inicia la atención de la asesoría (RF25). */
    @Transactional
    public AsesoriaResponse atender(Long id) {
        Asesoria asesoria = buscarOFallar(id);
        if (asesoria.getEstado() != EstadoAsesoria.SOLICITADA) {
            throw new ConflictoNegocioException("Solo se puede atender una asesoría en estado SOLICITADA");
        }
        asesoria.setEstado(EstadoAsesoria.EN_PROCESO);
        asesoria.setFechaAtencion(LocalDate.now());
        log.info("Asesoría id={} en atención -> EN_PROCESO (RF25)", id);
        return asesoriaMapper.toResponse(asesoria);
    }

    /** Cierra la asesoría una vez completada la atención. */
    @Transactional
    public AsesoriaResponse cerrar(Long id) {
        Asesoria asesoria = buscarOFallar(id);
        if (asesoria.getEstado() == EstadoAsesoria.CERRADA
                || asesoria.getEstado() == EstadoAsesoria.CANCELADA) {
            throw new ConflictoNegocioException("La asesoría no está en un estado que permita cerrarla");
        }
        asesoria.setEstado(EstadoAsesoria.CERRADA);
        log.info("Asesoría cerrada id={}", id);
        return asesoriaMapper.toResponse(asesoria);
    }

    @Transactional
    public AsesoriaResponse cancelar(Long id) {
        Asesoria asesoria = buscarOFallar(id);
        if (asesoria.getEstado() == EstadoAsesoria.CERRADA) {
            throw new ConflictoNegocioException("No se puede cancelar una asesoría ya cerrada");
        }
        asesoria.setEstado(EstadoAsesoria.CANCELADA);
        log.info("Asesoría cancelada id={}", id);
        return asesoriaMapper.toResponse(asesoria);
    }

    public Page<AsesoriaResponse> listar(Pageable pageable) {
        return asesoriaRepository.findAll(pageable).map(asesoriaMapper::toResponse);
    }

    public Page<AsesoriaResponse> listarPorEmpresa(Long idEmpresa, Pageable pageable) {
        return asesoriaRepository.findByEmpresaId(idEmpresa, pageable).map(asesoriaMapper::toResponse);
    }

    public AsesoriaResponse obtenerPorId(Long id) {
        return asesoriaMapper.toResponse(buscarOFallar(id));
    }

    /** Asesorías asignadas al profesional autenticado (portal profesional). */
    public List<AsesoriaResponse> misAsignaciones(String emailUsuario) {
        return asesoriaRepository.findByProfesionalUsuarioEmailOrderByFechaSolicitudDesc(emailUsuario)
                .stream().map(asesoriaMapper::toResponse).toList();
    }

    /** Asesorías de la empresa del cliente autenticado (portal cliente, solo lectura, RF07). */
    public List<AsesoriaResponse> misAsesorias(String emailUsuario) {
        Long idEmpresa = empresaService.empresaAutenticada(emailUsuario).getId();
        return asesoriaRepository.findByEmpresaIdOrderByFechaSolicitudDesc(idEmpresa)
                .stream().map(asesoriaMapper::toResponse).toList();
    }

    /**
     * Sugerencia para el admin: ¿una nueva asesoría de esta empresa se cobraría extra
     * según el límite anual incluido (RF23/RF24)? Usado al aprobar una solicitud.
     */
    public boolean seriaExtra(Long idEmpresa) {
        return excedeLimiteIncluidas(idEmpresa, LocalDate.now().getYear());
    }

    /** True si la empresa ya consumió sus asesorías incluidas en el año (RF23 → cobro extra RF24). */
    private boolean excedeLimiteIncluidas(Long idEmpresa, int anio) {
        LocalDate desde = LocalDate.of(anio, 1, 1);
        LocalDate hasta = LocalDate.of(anio, 12, 31);
        long incluidasUsadas = asesoriaRepository
                .countByEmpresaIdAndFechaSolicitudBetweenAndEstadoNot(
                        idEmpresa, desde, hasta, EstadoAsesoria.CANCELADA);
        return incluidasUsadas >= ASESORIAS_INCLUIDAS_ANUALES;
    }

    private Asesoria buscarOFallar(Long id) {
        return asesoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asesoría", id));
    }
}
