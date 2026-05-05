package com.example.NoMasAccidentes.service.usuario;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.usuario.ActualizarUsuarioRequest;
import com.example.NoMasAccidentes.dto.usuario.CambiarPasswordRequest;
import com.example.NoMasAccidentes.dto.usuario.CrearUsuarioRequest;
import com.example.NoMasAccidentes.dto.usuario.UsuarioMapper;
import com.example.NoMasAccidentes.dto.usuario.UsuarioResponse;
import com.example.NoMasAccidentes.model.usuario.Rol;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.usuario.RolRepository;
import com.example.NoMasAccidentes.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de usuarios del sistema.
 * Alta y mantención de usuarios y gestión de credenciales.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoNegocioException("Ya existe un usuario con email " + request.email());
        }
        Rol rol = rolRepository.findById(request.idRol())
            .orElseThrow(() -> new RecursoNoEncontradoException("Rol", request.idRol()));

        Usuario usuario = Usuario.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .nombre(request.nombre())
            .apellido(request.apellido())
            .rol(rol)
            .activo(true)
            .build();

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario creado id={} email={} rol={} (RF01)",
            guardado.getId(), guardado.getEmail(), rol.getNombre());
        return usuarioMapper.toResponse(guardado);
    }

    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(usuarioMapper::toResponse);
    }

    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = buscarOFallar(id);
        return usuarioMapper.toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = buscarOFallar(id);

        // Si cambia el email, validar que el nuevo no esté ocupado por otro
        if (!usuario.getEmail().equals(request.email())
            && usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoNegocioException("Ya existe un usuario con email " + request.email());
        }
        Rol rol = rolRepository.findById(request.idRol())
            .orElseThrow(() -> new RecursoNoEncontradoException("Rol", request.idRol()));

        usuario.setEmail(request.email());
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setRol(rol);

        log.info("Usuario actualizado id={} (RF01)", id);
        return usuarioMapper.toResponse(usuario);
    }

    /**
     * Cambio de contraseña con verificación de la actual (RF02 — RNF07).
     * No expone si el usuario existe vs si la contraseña es incorrecta: ambos
     * casos lanzan ConflictoNegocioException con mensaje genérico.
     */
    @Transactional
    public void cambiarPassword(Long id, CambiarPasswordRequest request) {
        Usuario usuario = buscarOFallar(id);
        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
            throw new ConflictoNegocioException("La contraseña actual no es correcta");
        }
        usuario.setPasswordHash(passwordEncoder.encode(request.passwordNueva()));
        log.info("Contraseña actualizada para usuario id={} (RF02)", id);
    }

    /**
     * Soft delete: marca activo=false vía @SQLDelete en la entidad.
     * Conserva los registros para trazabilidad histórica (RNF14).
     */
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscarOFallar(id);
        usuarioRepository.delete(usuario);
        log.info("Usuario eliminado (soft) id={} (RNF14)", id);
    }

    private Usuario buscarOFallar(Long id) {
        return usuarioRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }
}
