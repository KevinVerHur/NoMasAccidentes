package com.example.NoMasAccidentes.service.visita;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.visita.CrearVisitaRequest;
import com.example.NoMasAccidentes.dto.visita.VisitaResponse;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.profesional.EstadoProfesional;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import com.example.NoMasAccidentes.model.visita.Visita;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.repository.visita.VisitaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitaService {

    private final VisitaRepository visitaRepository;
    private final ClienteRepository clienteRepository;
    private final ProfesionalRepository profesionalRepository;

    public Page<VisitaResponse> listar(Pageable pageable) {
        return visitaRepository.findAll(pageable).map(this::toResponse);
    }

    public List<VisitaResponse> listarMisVisitas(String emailUsuario) {
        return visitaRepository.findByProfesionalUsuarioEmailOrderByFechaProgramadaAsc(emailUsuario)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<VisitaResponse> listarMisVisitasProgramadas(String emailUsuario) {
        return visitaRepository
                .findByProfesionalUsuarioEmailAndEstadoOrderByFechaProgramadaAsc(
                        emailUsuario,
                        EstadoVisita.PROGRAMADA
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public VisitaResponse crear(CrearVisitaRequest request) {
        Cliente cliente = clienteRepository.findById(request.idCliente())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", request.idCliente()));

        Profesional profesional = profesionalRepository.findById(request.idProfesional())
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", request.idProfesional()));

        Visita visita = Visita.builder()
                .cliente(cliente)
                .profesional(profesional)
                .fechaProgramada(request.fechaProgramada())
                .direccion(request.direccion())
                .motivo(request.motivo())
                .estado(EstadoVisita.PROGRAMADA)
                .recordatorioEnviado(false)
                .build();

        return toResponse(visitaRepository.save(visita));
    }

    @Transactional
    public VisitaResponse iniciarMiVisita(String emailUsuario, Long idVisita) {
        Visita visita = buscarVisitaDelProfesional(emailUsuario, idVisita);

        if (visita.getEstado() != EstadoVisita.PROGRAMADA) {
            throw new ConflictoNegocioException("Solo se puede iniciar una visita programada");
        }

        Profesional profesional = visita.getProfesional();
        profesional.setEstado(EstadoProfesional.EN_VISITA);

        return toResponse(visita);
    }

    @Transactional
    public VisitaResponse finalizarMiVisita(String emailUsuario, Long idVisita) {
        Visita visita = buscarVisitaDelProfesional(emailUsuario, idVisita);

        if (visita.getEstado() == EstadoVisita.CANCELADA) {
            throw new ConflictoNegocioException("No se puede finalizar una visita cancelada");
        }

        visita.setEstado(EstadoVisita.REALIZADA);

        Profesional profesional = visita.getProfesional();
        profesional.setEstado(EstadoProfesional.DISPONIBLE);

        return toResponse(visita);
    }

    @Transactional
    public VisitaResponse cancelar(Long idVisita) {
        Visita visita = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new RecursoNoEncontradoException("Visita", idVisita));

        if (visita.getEstado() == EstadoVisita.REALIZADA) {
            throw new ConflictoNegocioException("No se puede cancelar una visita ya realizada");
        }

        visita.setEstado(EstadoVisita.CANCELADA);

        return toResponse(visita);
    }

    private Visita buscarVisitaDelProfesional(String emailUsuario, Long idVisita) {
        Visita visita = visitaRepository.findById(idVisita)
                .orElseThrow(() -> new RecursoNoEncontradoException("Visita", idVisita));

        Profesional profesional = visita.getProfesional();

        if (profesional == null || profesional.getUsuario() == null) {
            throw new ConflictoNegocioException("La visita no tiene profesional asignado");
        }

        String emailProfesional = profesional.getUsuario().getEmail();

        if (!emailProfesional.equalsIgnoreCase(emailUsuario)) {
            throw new ConflictoNegocioException("No puedes operar una visita asignada a otro profesional");
        }

        return visita;
    }

    private VisitaResponse toResponse(Visita visita) {
        Profesional profesional = visita.getProfesional();

        return new VisitaResponse(
                visita.getId(),
                visita.getCliente().getId(),
                visita.getCliente().getRazonSocial(),
                profesional != null ? profesional.getId() : null,
                profesional != null
                        ? profesional.getUsuario().getNombre() + " " + profesional.getUsuario().getApellido()
                        : null,
                visita.getFechaProgramada(),
                visita.getDireccion(),
                visita.getMotivo(),
                visita.getEstado()
        );
    }
}