package com.example.NoMasAccidentes.service.capacitacion;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.capacitacion.*;
import com.example.NoMasAccidentes.model.asistente.Asistente;
import com.example.NoMasAccidentes.model.asistencia.Asistencia;
import com.example.NoMasAccidentes.model.capacitacion.Capacitacion;
import com.example.NoMasAccidentes.model.capacitacion.EstadoCapacitacion;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.repository.asistente.AsistenteRepository;
import com.example.NoMasAccidentes.repository.asistencia.AsistenciaRepository;
import com.example.NoMasAccidentes.repository.capacitacion.CapacitacionRepository;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.service.empresa.EmpresaService;
import com.example.NoMasAccidentes.service.notificacion.NotificacionEventoService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.NoMasAccidentes.model.pago.CobroExtra;
import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.pago.Pago;
import com.example.NoMasAccidentes.model.pago.PlanPago;
import com.example.NoMasAccidentes.model.pago.TipoCobro;
import com.example.NoMasAccidentes.repository.pago.CobroExtraRepository;
import com.example.NoMasAccidentes.repository.pago.PagoRepository;
import com.example.NoMasAccidentes.repository.pago.PlanPagoRepository;
import java.math.BigDecimal;

/**
 * Lógica de negocio para Capacitaciones.
 *
 * Reglas implementadas:
 *  RF-CAP1: La capacitación debe programarse con al menos 15 días de anticipación.
 *  RF-CAP2: Relación N:M con Asistente gestionada a través de Asistencia.
 *           Se valida que no se supere el límite de cupos al inscribir.
 *  RF-CAP3: Endpoint para que un asistente confirme su asistencia.
 *  RF-CAP4: Flag esCapacitacionExtra → capacitaciones con costo adicional al plan.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CapacitacionService {

    /** Días mínimos de anticipación para programar una capacitación (RF-CAP1). */
    private static final long DIAS_ANTICIPACION_MINIMA = 15;

    private final CapacitacionRepository capacitacionRepository;
    private final AsistenciaRepository   asistenciaRepository;
    private final AsistenteRepository    asistenteRepository;
    private final EmpresaRepository      empresaRepository;
    private final ProfesionalRepository  profesionalRepository;
    private final CapacitacionMapper     mapper;
    private final NotificacionEventoService notificacionEventoService;
    private final EmpresaService         empresaService;
    private final PlanPagoRepository planPagoRepository;
    private final PagoRepository pagoRepository;
    private final CobroExtraRepository cobroExtraRepository;
    
    // ─────────────────────────────────────────────
    //  Consultas
    // ─────────────────────────────────────────────

    public Page<CapacitacionResponse> listar(Pageable pageable) {
        return capacitacionRepository.findAll(pageable).map(mapper::toResponse);
    }

    public CapacitacionResponse obtener(Long id) {
        return mapper.toResponse(buscarOFallar(id));
    }

    public List<CapacitacionResponse> listarPorEmpresa(Long idEmpresa) {
        validarExisteEmpresa(idEmpresa);
        return capacitacionRepository.findByEmpresaId(idEmpresa)
                .stream().map(mapper::toResponse).toList();
    }

    /** Capacitaciones de la empresa del cliente autenticado (portal cliente, solo lectura, RF07). */
    public List<CapacitacionResponse> misCapacitaciones(String emailUsuario) {
        Long idEmpresa = empresaService.empresaAutenticada(emailUsuario).getId();
        return capacitacionRepository.findByEmpresaId(idEmpresa)
                .stream().map(mapper::toResponse).toList();
    }

    /** Retorna solo las capacitaciones con costo extra de la empresa (RF-CAP4). */
    public List<CapacitacionResponse> listarExtras(Long idEmpresa) {
        validarExisteEmpresa(idEmpresa);
        return capacitacionRepository.findByEmpresaIdAndEsCapacitacionExtraTrue(idEmpresa)
                .stream().map(mapper::toResponse).toList();
    }

    /** Retorna las capacitaciones que dicta un relator. */
    public List<CapacitacionResponse> listarPorRelator(Long idRelator) {
        return capacitacionRepository.findByRelatorId(idRelator)
                .stream().map(mapper::toResponse).toList();
    }

    // ─────────────────────────────────────────────
    //  Comandos
    // ─────────────────────────────────────────────

    /**
     * Programa una nueva capacitación con los datos del formulario.
     * Valida que la fecha esté al menos 15 días en el futuro (RF-CAP1).
     * El flag esCapacitacionExtra determina si genera costo adicional (RF-CAP4).
     */
    @Transactional
    public CapacitacionResponse crear(CrearCapacitacionRequest request) {
        validarAnticipacion(request.fechaProgramada());

        Empresa empresa = empresaRepository.findById(request.idEmpresa())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa", request.idEmpresa()));

        Profesional relator = profesionalRepository.findById(request.idRelator())
                .orElseThrow(() -> new RecursoNoEncontradoException("Relator (Profesional)", request.idRelator()));

        Capacitacion capacitacion = Capacitacion.builder()
                .empresa(empresa)
                .curso(request.curso())
                .relator(relator)
                .fechaProgramada(request.fechaProgramada())
                .horaProgramada(LocalTime.parse(request.horaProgramada()))
                .lugar(request.lugar())
                .cupos(request.cupos())
                .objetivo(request.objetivo())
                .estado(EstadoCapacitacion.PROGRAMADA)
                .esCapacitacionExtra(request.esCapacitacionExtra())
                .build();

        Capacitacion guardada = capacitacionRepository.save(capacitacion);

        log.info("Capacitación creada id={} cliente='{}' curso='{}' relator='{}' fecha={} hora={} cupos={} extra={} (RF-CAP1/RF-CAP4)",
                guardada.getId(),
                empresa.getRazonSocial(),
                guardada.getCurso(),
                relator.getUsuario().getNombre() + " " + relator.getUsuario().getApellido(),
                guardada.getFechaProgramada(),
                guardada.getHoraProgramada(),
                guardada.getCupos(),
                guardada.isEsCapacitacionExtra());

        notificacionEventoService.notificarCapacitacionProgramada(guardada);

        return mapper.toResponse(guardada);
    }

    /**
     * Inscribe uno o más asistentes a la capacitación (N:M).
     * Valida que no se supere el límite de cupos y que el asistente
     * pertenezca al mismo cliente de la capacitación (RF-CAP2).
     */
    @Transactional
    public CapacitacionResponse inscribirAsistentes(Long idCapacitacion,
                                                    InscribirAsistentesRequest request) {
        Capacitacion capacitacion = buscarOFallar(idCapacitacion);

        if (capacitacion.getEstado() == EstadoCapacitacion.CANCELADA
                || capacitacion.getEstado() == EstadoCapacitacion.REALIZADA) {
            throw new ConflictoNegocioException(
                    "No se pueden inscribir asistentes a una capacitación en estado "
                    + capacitacion.getEstado());
        }

        int inscritos   = capacitacion.getAsistencias().size();
        int nuevos      = request.idsAsistentes().size();
        int cupos       = capacitacion.getCupos();

        // Cuántos del listado ya están inscritos (para no contarlos doble)
        long yaInscritos = request.idsAsistentes().stream()
                .filter(id -> asistenciaRepository
                        .existsByCapacitacionIdAndAsistenteId(idCapacitacion, id))
                .count();

        if (inscritos + (nuevos - yaInscritos) > cupos) {
            throw new ConflictoNegocioException(
                    "No hay suficientes cupos disponibles. Cupos totales: %d, ya inscritos: %d, nuevos a inscribir: %d."
                            .formatted(cupos, inscritos, nuevos - yaInscritos));
        }

        for (Long idAsistente : request.idsAsistentes()) {
            if (asistenciaRepository.existsByCapacitacionIdAndAsistenteId(idCapacitacion, idAsistente)) {
                log.warn("Asistente id={} ya inscrito en capacitación id={}, se omite.", idAsistente, idCapacitacion);
                continue;
            }

            Asistente asistente = asistenteRepository.findById(idAsistente)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Asistente", idAsistente));

            // Validar que el asistente pertenece a la empresa de la capacitación
            if (!asistente.getEmpresa().getId().equals(capacitacion.getEmpresa().getId())) {
                throw new ConflictoNegocioException(
                        "El asistente id=%d no pertenece a la empresa de esta capacitación."
                                .formatted(idAsistente));
            }

            Asistencia asistencia = Asistencia.builder()
                    .capacitacion(capacitacion)
                    .asistente(asistente)
                    .confirmado(false)
                    .asistio(false)
                    .build();

            capacitacion.getAsistencias().add(asistencia);
        }

        log.info("Asistentes inscritos en capacitación id={} (RF-CAP2)", idCapacitacion);
        return mapper.toResponse(capacitacion);
    }

    /**
     * Permite que un asistente confirme su participación en la capacitación.
     * RF-CAP3.
     *
     * @param idCapacitacion id de la capacitación
     * @param idAsistente    id del asistente que confirma
     * @param request        observaciones opcionales
     */
    @Transactional
    public AsistenciaResponse confirmarAsistencia(Long idCapacitacion,
                                                  Long idAsistente,
                                                  ConfirmarAsistenciaRequest request) {
        Capacitacion capacitacion = buscarOFallar(idCapacitacion);

        if (capacitacion.getEstado() == EstadoCapacitacion.CANCELADA) {
            throw new ConflictoNegocioException("No se puede confirmar asistencia a una capacitación cancelada");
        }
        if (capacitacion.getEstado() == EstadoCapacitacion.REALIZADA) {
            throw new ConflictoNegocioException("La capacitación ya fue realizada");
        }

        Asistencia asistencia = asistenciaRepository
                .findByCapacitacionIdAndAsistenteId(idCapacitacion, idAsistente)
                .orElseThrow(() -> new ConflictoNegocioException(
                        "El asistente id=%d no está inscrito en la capacitación id=%d"
                                .formatted(idAsistente, idCapacitacion)));

        if (asistencia.isConfirmado()) {
            throw new ConflictoNegocioException("El asistente ya había confirmado su participación");
        }

        
        asistencia.setConfirmado(true);
        asistencia.setFechaConfirmacion(LocalDateTime.now());

        if (request != null) {
        if (request.observaciones() != null) {
            asistencia.setObservaciones(request.observaciones());
        }
        if (request.firmaDigital() != null) {
            asistencia.setFirmaDigital(request.firmaDigital());
        }
}
        log.info("Asistente id={} confirmó asistencia a capacitación id='{}' (RF-CAP3)",
                idAsistente, capacitacion.getCurso());

        return mapper.toAsistenciaResponse(asistencia);
    }

    /**
     * Inicia la capacitación: pasa de PROGRAMADA a EN_CURSO.
     * Se invoca cuando el relator comienza a dictar el curso en la fecha/hora programada.
     */
    @Transactional
    public CapacitacionResponse iniciar(Long id) {
        Capacitacion capacitacion = buscarOFallar(id);

        if (capacitacion.getEstado() != EstadoCapacitacion.PROGRAMADA) {
            throw new ConflictoNegocioException(
                    "Solo se puede iniciar una capacitación en estado PROGRAMADA. Estado actual: "
                            + capacitacion.getEstado());
        }

        capacitacion.setEstado(EstadoCapacitacion.EN_CURSO);

        log.info("Capacitación iniciada id={} curso='{}'", id, capacitacion.getCurso());
        return mapper.toResponse(capacitacion);
    }

    /**
     * Finaliza la capacitación: pasa a REALIZADA y fija fechaRealizacion.
     * Exige que al menos un asistente tenga registrada su asistencia efectiva
     * (asistio=true), evitando cerrar capacitaciones sin nadie presente
     * (ver registrarAsistencia / RF-CAP). Solo es posible desde PROGRAMADA o EN_CURSO.
     */
    @Transactional
    public CapacitacionResponse finalizar(Long id, FinalizarCapacitacionRequest request) {
        Capacitacion capacitacion = buscarOFallar(id);

        if (capacitacion.getEstado() == EstadoCapacitacion.REALIZADA) {
            throw new ConflictoNegocioException("La capacitación ya fue realizada");
        }
        if (capacitacion.getEstado() == EstadoCapacitacion.CANCELADA) {
            throw new ConflictoNegocioException("No se puede finalizar una capacitación cancelada");
        }

        boolean hayAlMenosUnPresente = capacitacion.getAsistencias().stream()
                .anyMatch(Asistencia::isAsistio);

        if (!hayAlMenosUnPresente) {
            throw new ConflictoNegocioException(
                    "No se puede finalizar la capacitación sin al menos un asistente marcado como presente. "
                    + "Registre la asistencia efectiva antes de finalizar.");
        }

        capacitacion.setEstado(EstadoCapacitacion.REALIZADA);
        capacitacion.setFechaRealizacion(LocalDate.now());
        generarCobroExtraCapacitacionSiCorresponde(capacitacion);

        if (request != null) {
            capacitacion.setObservacionActa(request.observacionActa());
        }

        // Al finalizar, avisa al cliente que el acta y los certificados quedaron disponibles.
        notificacionEventoService.notificarCapacitacionRealizada(capacitacion);

        log.info("Capacitación finalizada id={} curso='{}' presentes={}/{}",
                id,
                capacitacion.getCurso(),
                capacitacion.getAsistencias().stream().filter(Asistencia::isAsistio).count(),
                capacitacion.getAsistencias().size());

        return mapper.toResponse(capacitacion);
    }

    /**
     * Cancela una capacitación. Solo es posible si aún no fue realizada.
     */
    @Transactional
    public CapacitacionResponse cancelar(Long id) {
        Capacitacion capacitacion = buscarOFallar(id);

        if (capacitacion.getEstado() == EstadoCapacitacion.REALIZADA) {
            throw new ConflictoNegocioException("No se puede cancelar una capacitación ya realizada");
        }
        if (capacitacion.getEstado() == EstadoCapacitacion.CANCELADA) {
            throw new ConflictoNegocioException("La capacitación ya está cancelada");
        }

        capacitacion.setEstado(EstadoCapacitacion.CANCELADA);

        log.info("Capacitación id={} curso='{}' cancelada", id, capacitacion.getCurso());
        return mapper.toResponse(capacitacion);
    }

    // ─────────────────────────────────────────────
    //  Helpers privados
    // ─────────────────────────────────────────────

    /**
     * RF-CAP1: La fecha programada debe estar al menos 15 días en el futuro.
     * Lanza {@link ConflictoNegocioException} → HTTP 409 (ya manejado por GlobalExceptionHandler).
     */
    private void validarAnticipacion(LocalDate fechaProgramada) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaProgramada);
        if (dias < DIAS_ANTICIPACION_MINIMA) {
            throw new ConflictoNegocioException(
                    ("La capacitación debe programarse con al menos %d días de anticipación. " +
                     "Días desde hoy hasta la fecha indicada: %d.")
                            .formatted(DIAS_ANTICIPACION_MINIMA, dias));
        }
    }

    private Capacitacion buscarOFallar(Long id) {
        return capacitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Capacitación", id));
    }

    private void validarExisteEmpresa(Long idEmpresa) {
        if (!empresaRepository.existsById(idEmpresa)) {
            throw new RecursoNoEncontradoException("Empresa", idEmpresa);
        }
    }

@Transactional
public AsistenciaResponse registrarAsistencia(Long idCapacitacion,
                                               Long idAsistente,
                                               boolean asistio) {
    Asistencia asistencia = asistenciaRepository
            .findByCapacitacionIdAndAsistenteId(idCapacitacion, idAsistente)
            .orElseThrow(() -> new ConflictoNegocioException(
                    "El asistente id=%d no está inscrito en la capacitación id=%d"
                            .formatted(idAsistente, idCapacitacion)));

    asistencia.setAsistio(asistio);

    log.info("Asistencia registrada: asistente id={} capacitación id={} asistio={}",
            idAsistente, idCapacitacion, asistio);

    return mapper.toAsistenciaResponse(asistencia);
}
private void generarCobroExtraCapacitacionSiCorresponde(Capacitacion capacitacion) {
    if (!capacitacion.isEsCapacitacionExtra()) {
        return;
    }

    if (cobroExtraRepository.existsByTipoCobroAndIdOrigen(TipoCobro.CAPACITACION_EXTRA, capacitacion.getId())) {
        log.info("Cobro extra de capacitación ya existe para capacitacion id={}", capacitacion.getId());
        return;
    }

    PlanPago plan = planPagoRepository
        .findFirstByEmpresa_IdAndFechaInicioLessThanEqualOrderByFechaInicioDesc(
                capacitacion.getEmpresa().getId(),
                LocalDate.now()
        )
        .orElse(null);
    if (plan == null) {
        log.warn("No se generó cobro extra: empresa id={} no tiene plan de pago vigente",
                capacitacion.getEmpresa().getId());
        return;
    }

    BigDecimal monto = plan.getMensualidad().getCostoCapacitacionExtra();

    if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
        log.warn("No se generó cobro extra: plan id={} no tiene costo de capacitación extra configurado",
                plan.getId());
        return;
    }

    Pago pago = pagoRepository
            .findFirstByPlanIdAndEstadoPagoInOrderByFechaVencimientoAsc(
                    plan.getId(),
                    List.of(EstadoPago.PENDIENTE, EstadoPago.ATRASADO)
            )
            .orElse(null);

    if (pago == null) {
        log.warn("No se generó cobro extra: plan id={} no tiene cuotas pendientes o atrasadas", plan.getId());
        return;
    }

    CobroExtra cobro = CobroExtra.builder()
            .pago(pago)
            .tipoCobro(TipoCobro.CAPACITACION_EXTRA)
            .idOrigen(capacitacion.getId())
            .descripcion("Capacitación extra: " + capacitacion.getCurso())
            .monto(monto)
            .fechaGeneracion(LocalDate.now())
            .build();

    cobroExtraRepository.save(cobro);

    log.info("Cobro extra generado por capacitación id={} pago id={} monto={}",
            capacitacion.getId(), pago.getId(), monto);
}
}