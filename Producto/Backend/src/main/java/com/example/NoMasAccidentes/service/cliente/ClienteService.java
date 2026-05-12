package com.example.NoMasAccidentes.service.cliente;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.cliente.ActualizarClienteRequest;
import com.example.NoMasAccidentes.dto.cliente.ClienteMapper;
import com.example.NoMasAccidentes.dto.cliente.ClienteResponse;
import com.example.NoMasAccidentes.dto.cliente.CrearClienteRequest;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.cliente.EstadoCliente;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de clientes del sistema (RF06–RF12).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final ClienteMapper clienteMapper;

    @Transactional
    public ClienteResponse crear(CrearClienteRequest request) {
        if (clienteRepository.findByRut(request.rut()).isPresent()) {
            throw new ConflictoNegocioException("Ya existe un cliente con RUT " + request.rut());
        }

        Profesional profesional = resolverProfesional(request.idProfesional());

        Cliente cliente = Cliente.builder()
                .razonSocial(request.razonSocial())
                .rut(request.rut())
                .nombreContacto(request.nombreContacto())
                .email(request.email())
                .telefono(request.telefono())
                .rubro(request.rubro())
                .plan(request.plan())
                .estado(EstadoCliente.ACTIVO)
                .profesional(profesional)
                .build();

        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente creado id={} rut={} (RF06)", guardado.getId(), guardado.getRut());
        return clienteMapper.toResponse(guardado);
    }

    public Page<ClienteResponse> listar(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(clienteMapper::toResponse);
    }

    public ClienteResponse obtenerPorId(Long id) {
        return clienteMapper.toResponse(buscarOFallar(id));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ActualizarClienteRequest request) {
        Cliente cliente = buscarOFallar(id);

        if (!cliente.getRut().equals(request.rut())) {
            clienteRepository.findByRut(request.rut()).ifPresent(otro -> {
                if (!otro.getId().equals(id)) {
                    throw new ConflictoNegocioException("Ya existe un cliente con RUT " + request.rut());
                }
            });
        }

        cliente.setRazonSocial(request.razonSocial());
        cliente.setRut(request.rut());
        cliente.setNombreContacto(request.nombreContacto());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setRubro(request.rubro());
        cliente.setPlan(request.plan());
        cliente.setEstado(request.estado());
        cliente.setProfesional(resolverProfesional(request.idProfesional()));

        log.info("Cliente actualizado id={} (RF06)", id);
        return clienteMapper.toResponse(cliente);
    }

    /** Suspende el servicio del cliente (RF09). */
    @Transactional
    public ClienteResponse suspender(Long id) {
        Cliente cliente = buscarOFallar(id);
        if (cliente.getEstado() == EstadoCliente.SUSPENDIDO) {
            throw new ConflictoNegocioException("El cliente ya está suspendido");
        }
        cliente.setEstado(EstadoCliente.SUSPENDIDO);
        log.info("Cliente suspendido id={} (RF09)", id);
        return clienteMapper.toResponse(cliente);
    }

    /** Reactiva un cliente suspendido (RF09). */
    @Transactional
    public ClienteResponse reactivar(Long id) {
        Cliente cliente = buscarOFallar(id);
        cliente.setEstado(EstadoCliente.ACTIVO);
        log.info("Cliente reactivado id={} (RF09)", id);
        return clienteMapper.toResponse(cliente);
    }

    @Transactional
    public void eliminar(Long id) {
        clienteRepository.delete(buscarOFallar(id));
        log.info("Cliente eliminado (soft) id={} (RNF14)", id);
    }

    private Cliente buscarOFallar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
    }

    private Profesional resolverProfesional(Long idProfesional) {
        if (idProfesional == null) return null;
        return profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", idProfesional));
    }
}
