package com.example.NoMasAccidentes.service.solicitud;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.asesoria.CrearAsesoriaRequest;
import com.example.NoMasAccidentes.dto.capacitacion.CrearCapacitacionRequest;
import com.example.NoMasAccidentes.dto.solicitud.AprobarSolicitudRequest;
import com.example.NoMasAccidentes.dto.solicitud.CrearSolicitudRequest;
import com.example.NoMasAccidentes.dto.solicitud.RechazarSolicitudRequest;
import com.example.NoMasAccidentes.dto.solicitud.SolicitudMapper;
import com.example.NoMasAccidentes.dto.solicitud.SolicitudResponse;
import com.example.NoMasAccidentes.dto.visita.PlanificarVisitaRequest;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.solicitud.EstadoSolicitud;
import com.example.NoMasAccidentes.model.solicitud.Solicitud;
import com.example.NoMasAccidentes.model.solicitud.TipoSolicitud;
import com.example.NoMasAccidentes.repository.solicitud.SolicitudRepository;
import com.example.NoMasAccidentes.service.asesoria.AsesoriaService;
import com.example.NoMasAccidentes.service.capacitacion.CapacitacionService;
import com.example.NoMasAccidentes.service.empresa.EmpresaService;
import com.example.NoMasAccidentes.service.notificacion.NotificacionEventoService;
import com.example.NoMasAccidentes.service.visita.VisitaService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Solicitudes del cliente (mejora web más allá del RF).
 * El cliente solicita; el admin aprueba —creando el recurso real vía los
 * servicios existentes— o rechaza. Ambas transiciones notifican al otro lado.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final SolicitudMapper mapper;
    private final EmpresaService empresaService;
    private final AsesoriaService asesoriaService;
    private final VisitaService visitaService;
    private final CapacitacionService capacitacionService;
    private final NotificacionEventoService notificacionEventoService;

    /** El cliente autenticado crea una solicitud (estado PENDIENTE) → avisa al admin. */
    @Transactional
    public SolicitudResponse crear(CrearSolicitudRequest request, String emailUsuario) {
        Empresa empresa = empresaService.empresaAutenticada(emailUsuario);
        Solicitud solicitud = Solicitud.builder()
                .empresa(empresa)
                .tipo(request.tipo())
                .estado(EstadoSolicitud.PENDIENTE)
                .descripcion(request.descripcion())
                .fechaPreferida(request.fechaPreferida())
                .esExtra(false)
                .build();
        Solicitud guardada = solicitudRepository.save(solicitud);
        log.info("Solicitud creada id={} empresa={} tipo={}", guardada.getId(), empresa.getId(), request.tipo());
        notificacionEventoService.notificarSolicitudRecibida(guardada);
        return mapper.toResponse(guardada);
    }

    /** Tracking del cliente autenticado. */
    public List<SolicitudResponse> misSolicitudes(String emailUsuario) {
        Long idEmpresa = empresaService.empresaAutenticada(emailUsuario).getId();
        return solicitudRepository.findByEmpresaIdOrderByFechaCreacionDesc(idEmpresa)
                .stream().map(mapper::toResponse).toList();
    }

    /** Bandeja del admin (opcionalmente filtrada por estado), con sugerencia de extra. */
    public List<SolicitudResponse> listar(EstadoSolicitud estado) {
        List<Solicitud> solicitudes = estado == null
                ? solicitudRepository.findAllByOrderByFechaCreacionDesc()
                : solicitudRepository.findByEstadoOrderByFechaCreacionDesc(estado);
        return solicitudes.stream().map(this::toResponseConSugerencia).toList();
    }

    /** Cantidad de solicitudes pendientes (badge del admin). */
    public long contarPendientes() {
        return solicitudRepository.countByEstado(EstadoSolicitud.PENDIENTE);
    }

    /**
     * El admin aprueba: crea el recurso real (asesoría/visita/capacitación) con los
     * datos aportados y notifica al cliente. Solo desde estado PENDIENTE.
     */
    @Transactional
    public SolicitudResponse aprobar(Long id, AprobarSolicitudRequest request) {
        Solicitud solicitud = buscarOFallar(id);
        exigirPendiente(solicitud);

        Long idEmpresa = solicitud.getEmpresa().getId();
        boolean esExtra = Boolean.TRUE.equals(request.esExtra());

        switch (solicitud.getTipo()) {
            case ASESORIA -> {
                requerir(request.idProfesional() != null, "Debes asignar un profesional para la asesoría");
                requerir(request.tipoAsesoria() != null, "Debes indicar el tipo de asesoría (ACCIDENTE o FISCALIZACION)");
                asesoriaService.crear(new CrearAsesoriaRequest(
                        idEmpresa, request.idProfesional(), request.tipoAsesoria(), solicitud.getDescripcion()));
            }
            case VISITA -> {
                requerir(request.idProfesional() != null, "Debes asignar un profesional para la visita");
                requerir(request.fechaProgramada() != null, "Debes indicar la fecha de la visita");
                visitaService.planificar(new PlanificarVisitaRequest(
                        idEmpresa, request.idProfesional(), request.fechaProgramada(),
                        request.tipoRevision(), esExtra));
            }
            case CAPACITACION -> {
                requerir(request.idProfesional() != null, "Debes asignar un relator para la capacitación");
                requerir(request.curso() != null && !request.curso().isBlank(), "El curso es obligatorio");
                requerir(request.fechaProgramada() != null, "Debes indicar la fecha de la capacitación");
                requerir(request.horaProgramada() != null && !request.horaProgramada().isBlank(), "La hora es obligatoria");
                requerir(request.lugar() != null && !request.lugar().isBlank(), "El lugar es obligatorio");
                requerir(request.cupos() != null && request.cupos() >= 1, "Los cupos son obligatorios");
                capacitacionService.crear(new CrearCapacitacionRequest(
                        idEmpresa, request.curso(), request.idProfesional(), request.fechaProgramada(),
                        request.horaProgramada(), request.lugar(), request.cupos(), request.objetivo(), esExtra));
            }
        }

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setEsExtra(esExtra);
        solicitud.setRespuestaAdmin(request.comentario());
        solicitud.setFechaRespuesta(LocalDateTime.now());
        log.info("Solicitud id={} APROBADA tipo={} extra={}", id, solicitud.getTipo(), esExtra);
        notificacionEventoService.notificarSolicitudRespondida(solicitud);
        return mapper.toResponse(solicitud);
    }

    /** El admin rechaza indicando el motivo → notifica al cliente. Solo desde PENDIENTE. */
    @Transactional
    public SolicitudResponse rechazar(Long id, RechazarSolicitudRequest request) {
        Solicitud solicitud = buscarOFallar(id);
        exigirPendiente(solicitud);
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setRespuestaAdmin(request.comentario());
        solicitud.setFechaRespuesta(LocalDateTime.now());
        log.info("Solicitud id={} RECHAZADA", id);
        notificacionEventoService.notificarSolicitudRespondida(solicitud);
        return mapper.toResponse(solicitud);
    }

    // ── helpers ──

    private SolicitudResponse toResponseConSugerencia(Solicitud s) {
        Boolean sugerencia = null;
        if (s.getTipo() == TipoSolicitud.ASESORIA && s.getEstado() == EstadoSolicitud.PENDIENTE) {
            sugerencia = asesoriaService.seriaExtra(s.getEmpresa().getId());
        }
        return mapper.toResponse(s, sugerencia);
    }

    private void exigirPendiente(Solicitud solicitud) {
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ConflictoNegocioException("La solicitud ya fue respondida (estado " + solicitud.getEstado() + ")");
        }
    }

    private void requerir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new ConflictoNegocioException(mensaje);
        }
    }

    private Solicitud buscarOFallar(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Solicitud", id));
    }
}
