package com.example.NoMasAccidentes.service.consulta;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.consulta.ConsultaResponse;
import com.example.NoMasAccidentes.dto.consulta.CrearConsultaRequest;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.consulta.Consulta;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import com.example.NoMasAccidentes.repository.consulta.ConsultaRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultaService {

    private static final LocalTime INICIO_ATENCION = LocalTime.of(9,0);
    private static final LocalTime FIN_ATENCION = LocalTime.of(18, 0);

    private final ConsultaRepository consultaRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfesionalRepository profesionalRepository;

    public Page<ConsultaResponse> listar(Pageable pageable) {
        return consultaRepository.findAll(pageable).map(this::toResponse);
    }
    public List<ConsultaResponse> listarPorEmpresa(Long idEmpresa){
        return consultaRepository.findByEmpresaIdOrderByFechaHoraDesc(idEmpresa)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Historial de llamados atendidos por el profesional autenticado (RF02/RF41). */
    public List<ConsultaResponse> misConsultas(String emailProfesional){
        Profesional profesional = profesionalRepository.findByUsuarioEmail(emailProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No hay un profesional asociado al usuario " + emailProfesional));
        return consultaRepository.findByProfesionalIdOrderByFechaHoraDesc(profesional.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ConsultaResponse crear(CrearConsultaRequest request, String emailAutenticado){
        Empresa empresa = empresaRepository.findById(request.idEmpresa())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa", request.idEmpresa()));

        Profesional profesional = resolverProfesional(request.idProfesional(), emailAutenticado, empresa);

        LocalDateTime fechaHora = LocalDateTime.now();
        boolean fueraHorario = !esHorarioAtencion(fechaHora);

        Consulta consulta = Consulta.builder()
                .empresa(empresa)
                .profesional(profesional)
                .fechaHora(fechaHora)
                .motivo(request.motivo())
                .detalle(request.detalle())
                .fueraHorario(fueraHorario)
                .costoAdicional(fueraHorario)
                .build();

        return toResponse(consultaRepository.save(consulta));
    }

    /**
     * Atribuye el llamado a quien lo resolvió:
     * 1. Si lo registra un profesional, siempre a sí mismo (ignora idProfesional del request).
     * 2. Si lo registra un admin, al profesional indicado en el request.
     * 3. Admin sin indicar: por defecto, el profesional asignado a la empresa (puede ser null).
     */
    private Profesional resolverProfesional(Long idProfesionalRequest, String email, Empresa empresa){
        Profesional autenticado = profesionalRepository.findByUsuarioEmail(email).orElse(null);
        if (autenticado != null) return autenticado;
        if (idProfesionalRequest != null) {
            return profesionalRepository.findById(idProfesionalRequest)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", idProfesionalRequest));
        }
        return empresa.getProfesional();
    }

    private boolean esHorarioAtencion(LocalDateTime fechaHora){
        DayOfWeek dia = fechaHora.getDayOfWeek();
        LocalTime hora = fechaHora.toLocalTime();

        boolean diaHabil = dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY;
        boolean horaHabil = !hora.isBefore(INICIO_ATENCION) && !hora.isAfter(FIN_ATENCION);

        return diaHabil && horaHabil;
    }


    private ConsultaResponse toResponse(Consulta consulta) {
        Profesional profesional = consulta.getProfesional();
        return new ConsultaResponse(
                consulta.getId(),
                consulta.getEmpresa().getId(),
                consulta.getEmpresa().getRazonSocial(),
                profesional != null ? profesional.getId() : null,
                nombreProfesional(profesional),
                consulta.getFechaHora(),
                consulta.getMotivo(),
                consulta.getDetalle(),
                consulta.isFueraHorario(),
                consulta.isCostoAdicional()
        );
    }

    private String nombreProfesional(Profesional profesional) {
        if (profesional == null || profesional.getUsuario() == null) return null;
        return (profesional.getUsuario().getNombre() + " " + profesional.getUsuario().getApellido()).trim();
    }
}
