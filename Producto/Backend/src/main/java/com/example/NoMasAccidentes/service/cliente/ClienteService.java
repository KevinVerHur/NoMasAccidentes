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
import com.example.NoMasAccidentes.model.usuario.PasswordResetToken;
import com.example.NoMasAccidentes.model.usuario.Rol;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.repository.usuario.PasswordResetTokenRepository;
import com.example.NoMasAccidentes.repository.usuario.RolRepository;
import com.example.NoMasAccidentes.repository.usuario.UsuarioRepository;
import com.example.NoMasAccidentes.service.usuario.CorreoService;
import com.example.NoMasAccidentes.service.visita.ListaChequeoService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private static final String ROL_CLIENTE = "CLIENTE";
    private static final long INVITACION_VALIDEZ_HORAS = 1;

    private final ClienteRepository clienteRepository;
    private final ProfesionalRepository profesionalRepository;
    private final ClienteMapper clienteMapper;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final CorreoService correoService;
    private final PasswordEncoder passwordEncoder;
    private final ListaChequeoService listaChequeoService;

    @Transactional
    public ClienteResponse crear(CrearClienteRequest request) {
        if (clienteRepository.findByRut(request.rut()).isPresent()) {
            throw new ConflictoNegocioException("Ya existe un cliente con RUT " + request.rut());
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoNegocioException("Ya existe un usuario con email " + request.email());
        }

        Profesional profesional = resolverProfesional(request.idProfesional());
        Usuario usuario = provisionarUsuarioCliente(request);

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
                .usuario(usuario)
                .build();

        Cliente guardado = clienteRepository.save(cliente);
        listaChequeoService.crearPorDefecto(guardado);
        enviarInvitacion(usuario);
        log.info("Cliente creado id={} rut={} con cuenta usuario id={} (RF06)",
                guardado.getId(), guardado.getRut(), usuario.getId());
        return clienteMapper.toResponse(guardado);
    }

    /**
     * Crea la cuenta de acceso del cliente con rol CLIENTE. La contraseña queda en un
     * valor aleatorio inutilizable: el cliente define la suya vía el enlace de invitación.
     */
    private Usuario provisionarUsuarioCliente(CrearClienteRequest request) {
        Rol rolCliente = rolRepository.findByNombre(ROL_CLIENTE)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol CLIENTE no encontrado"));

        Usuario usuario = Usuario.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nombre(request.nombreContacto())
                .apellido(request.razonSocial())
                .rol(rolCliente)
                .activo(true)
                .build();
        return usuarioRepository.save(usuario);
    }

    /** Genera el token de activación y dispara el correo de invitación (válido 48 h). */
    private void enviarInvitacion(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        tokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .usuario(usuario)
                .creadoEn(LocalDateTime.now())
                .expiraEn(LocalDateTime.now().plusHours(INVITACION_VALIDEZ_HORAS))
                .build());
        correoService.enviarInvitacionCliente(usuario.getEmail(), token);
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
        
        if (cliente.getUsuario() != null) {
            cliente.getUsuario().setActivo(false);
        }

        log.info("Cliente suspendido id={} (RF09/RF12)", id);
        return clienteMapper.toResponse(cliente);
    }

    /** Reactiva un cliente suspendido (RF09/RF12). */
    @Transactional
    public ClienteResponse reactivar(Long id) {
        Cliente cliente = buscarOFallar(id);

        cliente.setEstado(EstadoCliente.ACTIVO);

        if(cliente.getUsuario() != null) {
            cliente.getUsuario().setActivo(true);
        }

        log.info("Cliente reactivado id={} (RF09/EF12)", id);
        return clienteMapper.toResponse(cliente);
    }

    @Transactional
    public void eliminar(Long id) {
        clienteRepository.delete(buscarOFallar(id));
        log.info("Cliente eliminado (soft) id={} (RNF14)", id);
    }

    /** Resuelve el cliente asociado al usuario autenticado (portal cliente). */
    public Cliente clienteAutenticado(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No hay un cliente asociado al usuario " + email));
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

    public ClienteResponse clientePorEmail(String email) {
    Cliente c = clienteAutenticado(email);
    return clienteMapper.toResponse(c);
}
}
