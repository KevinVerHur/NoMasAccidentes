package com.example.NoMasAccidentes.service.visita;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.visita.PlanificarVisitaRequest;
import com.example.NoMasAccidentes.dto.visita.RegistrarVisitaRequest;
import com.example.NoMasAccidentes.dto.visita.VisitaMapper;
import com.example.NoMasAccidentes.dto.visita.VisitaResponse;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.profesional.EstadoProfesional;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.model.visita.ListaChequeo;
import com.example.NoMasAccidentes.model.visita.Visita;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.repository.visita.ListaChequeoRepository;
import com.example.NoMasAccidentes.repository.visita.VisitaRepository;
import com.example.NoMasAccidentes.service.cliente.ClienteService;
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
 * Gestión de visitas a clientes (RF13–RF14).
 * Ciclo: PROGRAMADA -> EN_CURSO -> REALIZADA (o CANCELADA).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class VisitaService {

    private final VisitaRepository visitaRepository;
    private final ListaChequeoRepository listaChequeoRepository;
    private final ClienteRepository clienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final VisitaMapper visitaMapper;
    private final ClienteService clienteService;

    /** Planifica una visita (RF13). Requiere que el cliente tenga lista de chequeo (RF16). */
    @Transactional
    public VisitaResponse planificar(PlanificarVisitaRequest request) {
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", request.idCliente()));
        Profesional profesional = profesionalRepository.findById(request.idProfesional())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", request.idProfesional()));
        ListaChequeo lista = listaChequeoRepository.findByClienteId(cliente.getId())
                .orElseThrow(() -> new ConflictoNegocioException(
                        "El cliente no tiene lista de chequeo. Cree una antes de planificar visitas (RF16)."));

        if (request.fechaProgramada().isBefore(LocalDate.now())) {
            throw new ConflictoNegocioException("No se puede planificar una visita en una fecha pasada");
        }
        if (visitaRepository.existsByClienteIdAndFechaProgramadaAndEstado(
                cliente.getId(), request.fechaProgramada(), EstadoVisita.PROGRAMADA)) {
            throw new ConflictoNegocioException(
                    "Ya existe una visita programada para ese cliente en esa fecha");
        }

        Visita visita = Visita.builder()
                .cliente(cliente)
                .profesional(profesional)
                .listaChequeo(lista)
                .tipoRevision(request.tipoRevision())
                .fechaProgramada(request.fechaProgramada())
                .estado(EstadoVisita.PROGRAMADA)
                .esVisitaExtra(Boolean.TRUE.equals(request.esVisitaExtra()))
                .build();

        Visita guardada = visitaRepository.save(visita);
        log.info("Visita planificada id={} cliente={} fecha={} (RF13)",
                guardada.getId(), cliente.getId(), request.fechaProgramada());
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
        log.info("Visita registrada en terreno id={} (RF14); profesional={} -> DISPONIBLE",
                id, visita.getProfesional().getId());
        return visitaMapper.toResponse(visita);
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

    public Page<VisitaResponse> listarPorCliente(Long idCliente, Pageable pageable) {
        return visitaRepository.findByClienteId(idCliente, pageable).map(visitaMapper::toResponse);
    }

    public VisitaResponse obtenerPorId(Long id) {
        return visitaMapper.toResponse(buscarOFallar(id));
    }

    /** Visitas asignadas al profesional autenticado (dashboard profesional). */
    public List<VisitaResponse> misAsignaciones(String emailUsuario) {
        return visitaRepository.findByProfesionalUsuarioEmailOrderByFechaProgramadaAsc(emailUsuario)
                .stream().map(visitaMapper::toResponse).toList();
    }

    /** Visitas del cliente autenticado (portal cliente, solo lectura). */
    public List<VisitaResponse> misVisitas(String emailUsuario) {
        Long idCliente = clienteService.clienteAutenticado(emailUsuario).getId();
        return visitaRepository.findByClienteIdOrderByFechaProgramadaDesc(idCliente)
                .stream().map(visitaMapper::toResponse).toList();
    }

    /** Cuenta de visitas del cliente en un mes; apoya el control de RF13 (mínimo 2/mes). */
    public long contarVisitasDelMes(Long idCliente, int anio, int mes) {
        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = desde.withDayOfMonth(desde.lengthOfMonth());
        return visitaRepository.countByClienteIdAndFechaProgramadaBetween(idCliente, desde, hasta);
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
