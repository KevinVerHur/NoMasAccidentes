package com.example.NoMasAccidentes.service.cliente;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.cliente.CrearRepresentanteRequest;
import com.example.NoMasAccidentes.dto.cliente.RepresentanteMapper;
import com.example.NoMasAccidentes.dto.cliente.RepresentanteResponse;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.usuario.PasswordResetToken;
import com.example.NoMasAccidentes.model.usuario.Rol;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import com.example.NoMasAccidentes.repository.usuario.PasswordResetTokenRepository;
import com.example.NoMasAccidentes.repository.usuario.RolRepository;
import com.example.NoMasAccidentes.repository.usuario.UsuarioRepository;
import com.example.NoMasAccidentes.service.usuario.CorreoService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de representantes (personas de contacto) de una empresa. Una empresa
 * puede tener varios; cada uno puede tener o no acceso al portal (rol CLIENTE).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RepresentanteService {

    private static final String ROL_CLIENTE = "CLIENTE";
    private static final long INVITACION_VALIDEZ_HORAS = 1;

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;
    private final RepresentanteMapper mapper;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final CorreoService correoService;
    private final PasswordEncoder passwordEncoder;

    public List<RepresentanteResponse> listarPorEmpresa(Long idEmpresa) {
        if (!empresaRepository.existsById(idEmpresa)) {
            throw new RecursoNoEncontradoException("Empresa", idEmpresa);
        }
        return clienteRepository.findByEmpresaId(idEmpresa).stream()
                .map(mapper::toResponse).toList();
    }

    @Transactional
    public RepresentanteResponse crear(Long idEmpresa, CrearRepresentanteRequest request) {
        Empresa empresa = empresaRepository.findById(idEmpresa)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa", idEmpresa));

        Usuario usuario = null;
        if (request.conAcceso()) {
            if (usuarioRepository.existsByEmail(request.email())) {
                throw new ConflictoNegocioException("Ya existe un usuario con ese correo");
            }
            usuario = provisionarUsuario(request, empresa);
        }

        Cliente representante = clienteRepository.save(Cliente.builder()
                .empresa(empresa)
                .nombre(request.nombre())
                .cargo(request.cargo())
                .email(request.email())
                .telefono(request.telefono())
                .usuario(usuario)
                .build());

        if (usuario != null) {
            enviarInvitacion(usuario);
        }
        log.info("Representante creado id={} empresa={} conAcceso={}",
                representante.getId(), idEmpresa, request.conAcceso());
        return mapper.toResponse(representante);
    }

    @Transactional
    public void eliminar(Long id) {
        Cliente representante = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Representante", id));
        clienteRepository.delete(representante);
        log.info("Representante eliminado (soft) id={}", id);
    }

    private Usuario provisionarUsuario(CrearRepresentanteRequest request, Empresa empresa) {
        Rol rolCliente = rolRepository.findByNombre(ROL_CLIENTE)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol CLIENTE no encontrado"));
        return usuarioRepository.save(Usuario.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nombre(request.nombre())
                .apellido(empresa.getRazonSocial())
                .rol(rolCliente)
                .activo(true)
                .build());
    }

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
}
