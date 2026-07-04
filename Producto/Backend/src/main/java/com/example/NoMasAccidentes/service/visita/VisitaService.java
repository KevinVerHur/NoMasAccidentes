package com.example.NoMasAccidentes.service.visita;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.visita.PlanificarVisitaRequest;
import com.example.NoMasAccidentes.dto.visita.RegistrarVisitaRequest;
import com.example.NoMasAccidentes.dto.visita.ResultadoChequeoRequest;
import com.example.NoMasAccidentes.dto.visita.ResultadoChequeoResponse;
import com.example.NoMasAccidentes.dto.visita.VisitaMapper;
import com.example.NoMasAccidentes.dto.visita.VisitaResponse;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.profesional.EstadoProfesional;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.model.visita.ItemChequeo;
import com.example.NoMasAccidentes.model.visita.ListaChequeo;
import com.example.NoMasAccidentes.model.visita.ResultadoChequeo;
import com.example.NoMasAccidentes.model.visita.Visita;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.repository.visita.ListaChequeoRepository;
import com.example.NoMasAccidentes.repository.visita.ResultadoChequeoRepository;
import com.example.NoMasAccidentes.repository.visita.VisitaRepository;
import com.example.NoMasAccidentes.service.empresa.EmpresaService;
import com.example.NoMasAccidentes.service.notificacion.NotificacionEventoService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de visitas a empresas cliente (RF13–RF14).
 * Ciclo: PROGRAMADA -> EN_CURSO -> REALIZADA (o CANCELADA).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VisitaService {

    private final VisitaRepository visitaRepository;
    private final ListaChequeoRepository listaChequeoRepository;
    private final ResultadoChequeoRepository resultadoChequeoRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfesionalRepository profesionalRepository;
    private final VisitaMapper visitaMapper;
    private final EmpresaService empresaService;
    private final NotificacionEventoService notificacionEventoService;

    /** Planifica una visita (RF13). Requiere que la empresa tenga lista de chequeo (RF16). */
    @Transactional
    public VisitaResponse planificar(PlanificarVisitaRequest request) {
        Empresa empresa = empresaRepository.findById(request.idEmpresa())
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa", request.idEmpresa()));
        Profesional profesional = profesionalRepository.findById(request.idProfesional())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", request.idProfesional()));
        ListaChequeo lista = listaChequeoRepository.findByEmpresaId(empresa.getId())
                .orElseThrow(() -> new ConflictoNegocioException(
                        "La empresa no tiene lista de chequeo. Cree una antes de planificar visitas (RF16)."));

        if (request.fechaProgramada().isBefore(LocalDate.now())) {
            throw new ConflictoNegocioException("No se puede planificar una visita en una fecha pasada");
        }
        if (visitaRepository.existsByEmpresaIdAndFechaProgramadaAndEstado(
                empresa.getId(), request.fechaProgramada(), EstadoVisita.PROGRAMADA)) {
            throw new ConflictoNegocioException(
                    "Ya existe una visita programada para esa empresa en esa fecha");
        }

        Visita visita = Visita.builder()
                .empresa(empresa)
                .profesional(profesional)
                .listaChequeo(lista)
                .tipoRevision(request.tipoRevision())
                .fechaProgramada(request.fechaProgramada())
                .estado(EstadoVisita.PROGRAMADA)
                .esVisitaExtra(Boolean.TRUE.equals(request.esVisitaExtra()))
                .build();

        Visita guardada = visitaRepository.save(visita);
        log.info("Visita planificada id={} empresa={} fecha={} (RF13)",
                guardada.getId(), empresa.getId(), request.fechaProgramada());
        notificacionEventoService.notificarVisitaPlanificada(guardada);
        return visitaMapper.toResponse(guardada);
    }

    /** Inicia la visita en terreno: pasa a EN_CURSO y marca al profesional EN_VISITA (RF04). */
    @Transactional
    public VisitaResponse iniciar(Long id) {
        Visita visita = buscarOFallar(id);
        if (visita.getEstado() != EstadoVisita.PROGRAMADA) {
            throw new ConflictoNegocioException("Solo se puede iniciar una visita en estado PROGRAMADA");
        }
        visita.setEstado(EstadoVisita.EN_CURSO);
        visita.setFechaInicio(LocalDateTime.now());
        visita.getProfesional().setEstado(EstadoProfesional.EN_VISITA);
        log.info("Visita iniciada id={}; profesional={} -> EN_VISITA (RF04)",
                id, visita.getProfesional().getId());
        return visitaMapper.toResponse(visita);
    }

    /** Registra la visita realizada en terreno (RF14) y libera al profesional (DISPONIBLE). */
    @Transactional
    public VisitaResponse registrar(Long id, RegistrarVisitaRequest request) {
        Visita visita = buscarOFallar(id);
        if (visita.getEstado() == EstadoVisita.REALIZADA || visita.getEstado() == EstadoVisita.CANCELADA) {
            throw new ConflictoNegocioException("La visita no está en un estado que permita registrarla");
        }
        if (visita.getFechaInicio() == null) {
            visita.setFechaInicio(LocalDateTime.now());
        }
        visita.setFechaFin(LocalDateTime.now());
        visita.setEstado(EstadoVisita.REALIZADA);
        visita.setObservaciones(request.observaciones());
        visita.setLatitud(request.latitud());
        visita.setLongitud(request.longitud());
        visita.getProfesional().setEstado(EstadoProfesional.DISPONIBLE);
        guardarResultados(visita, request.resultados());
        log.info("Visita registrada en terreno id={} (RF14); profesional={} -> DISPONIBLE",
                id, visita.getProfesional().getId());
        return visitaMapper.toResponse(visita);
    }

    /** Persiste el marcado de la lista de chequeo de la visita (RF19), en modo upsert. */
    private void guardarResultados(Visita visita, List<ResultadoChequeoRequest> resultados) {
        if (resultados == null || resultados.isEmpty()) {
            return;
        }
        for (ResultadoChequeoRequest req : resultados) {
            ItemChequeo item = visita.getListaChequeo().getItems().stream()
                    .filter(it -> it.getId().equals(req.idItem()))
                    .findFirst()
                    .orElseThrow(() -> new ConflictoNegocioException(
                            "El ítem " + req.idItem() + " no pertenece a la lista de chequeo de la visita"));

            ResultadoChequeo resultado = resultadoChequeoRepository
                    .findByVisitaIdAndItemId(visita.getId(), item.getId())
                    .orElseGet(() -> ResultadoChequeo.builder().visita(visita).item(item).build());
            resultado.setEstado(req.estado());
            resultado.setObservacion(req.observacion());
            resultadoChequeoRepository.save(resultado);
        }
    }

    /** Resultado del chequeo registrado en una visita (RF19). */
    public List<ResultadoChequeoResponse> resultadosDeVisita(Long idVisita) {
        buscarOFallar(idVisita);
        return resultadoChequeoRepository.findByVisitaIdOrderByIdAsc(idVisita).stream()
                .map(r -> new ResultadoChequeoResponse(
                        r.getId(),
                        r.getItem().getId(),
                        r.getItem().getDescripcion(),
                        r.getItem().getCategoria(),
                        r.getItem().getNormaLegal(),
                        r.getEstado(),
                        r.getObservacion()))
                .toList();
    }

    @Transactional
    public VisitaResponse cancelar(Long id) {
        Visita visita = buscarOFallar(id);
        if (visita.getEstado() == EstadoVisita.REALIZADA) {
            throw new ConflictoNegocioException("No se puede cancelar una visita ya realizada");
        }
        visita.setEstado(EstadoVisita.CANCELADA);
        log.info("Visita cancelada id={}", id);
        return visitaMapper.toResponse(visita);
    }

    public Page<VisitaResponse> listar(Pageable pageable) {
        return visitaRepository.findAll(pageable).map(visitaMapper::toResponse);
    }

    public Page<VisitaResponse> listarPorEmpresa(Long idEmpresa, Pageable pageable) {
        return visitaRepository.findByEmpresaId(idEmpresa, pageable).map(visitaMapper::toResponse);
    }

    public VisitaResponse obtenerPorId(Long id) {
        return visitaMapper.toResponse(buscarOFallar(id));
    }

    /** Visitas asignadas al profesional autenticado (dashboard profesional). */
    public List<VisitaResponse> misAsignaciones(String emailUsuario) {
        return visitaRepository.findByProfesionalUsuarioEmailOrderByFechaProgramadaAsc(emailUsuario)
                .stream().map(visitaMapper::toResponse).toList();
    }

    /** Visitas de la empresa del usuario autenticado (portal cliente, solo lectura). */
    public List<VisitaResponse> misVisitas(String emailUsuario) {
        Long idEmpresa = empresaService.empresaAutenticada(emailUsuario).getId();
        return visitaRepository.findByEmpresaIdOrderByFechaProgramadaDesc(idEmpresa)
                .stream().map(visitaMapper::toResponse).toList();
    }

    /** Cuenta de visitas de la empresa en un mes; apoya el control de RF13 (mínimo 2/mes). */
    public long contarVisitasDelMes(Long idEmpresa, int anio, int mes) {
        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        return visitaRepository.countByEmpresaIdAndFechaProgramadaBetween(idEmpresa, desde, hasta);
    }

    @Transactional
    public void eliminar(Long id) {
        visitaRepository.delete(buscarOFallar(id));
        log.info("Visita eliminada (soft) id={} (RNF14)", id);
    }

    private Visita buscarOFallar(Long id) {
        return visitaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Visita", id));
    }
}
