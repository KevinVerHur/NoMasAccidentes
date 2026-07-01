package com.example.NoMasAccidentes.controller.usuario;

import com.example.NoMasAccidentes.dto.usuario.ActualizarPerfilRequest;
import com.example.NoMasAccidentes.dto.usuario.ActualizarUsuarioRequest;
import com.example.NoMasAccidentes.dto.usuario.CambiarPasswordRequest;
import com.example.NoMasAccidentes.dto.usuario.CrearUsuarioRequest;
import com.example.NoMasAccidentes.dto.usuario.UsuarioResponse;
import com.example.NoMasAccidentes.service.usuario.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD de usuarios del sistema.
 * Gestión de usuarios y gestión de credenciales.
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema (RF01–RF02)")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(
            summary = "Crear un nuevo usuario",
            description = "Registra un nuevo usuario en el sistema. Solo accesible por administradores."
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        UsuarioResponse creado = usuarioService.crear(request);
        return ResponseEntity
                .created(URI.create("/api/usuarios/" + creado.id()))
                .body(creado);
    }

    @Operation(
            summary = "Listar usuarios paginados",
            description = "Retorna una página con todos los usuarios registrados en el sistema. Solo accesible por administradores."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioService.listar(pageable);
    }

    @Operation(
            summary = "Obtener usuario por ID ",
            description = "Retorna los datos de un usuario específico por su identificador. Solo accesible por administradores."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    @Operation(
            summary = "Actualizar usuario completo ",
            description = "Reemplaza todos los datos de un usuario existente. Solo accesible por administradores."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @Operation(
            summary = "Cambiar contraseña de usuario ",
            description = "Actualiza la contraseña de un usuario específico. Solo accesible por administradores."
    )
    @PatchMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Eliminar usuario ",
            description = "Elimina permanentemente un usuario del sistema. Solo accesible por administradores."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

@GetMapping("/me")
@PreAuthorize("isAuthenticated()")
public UsuarioResponse me(Principal principal) {
    return usuarioService.obtenerPorEmail(principal.getName());
}    

@PutMapping("/me")
@PreAuthorize("isAuthenticated()")
public UsuarioResponse actualizarMiPerfil(
        Principal principal,
        @Valid @RequestBody ActualizarPerfilRequest request) {
    return usuarioService.actualizarPerfil(principal.getName(), request);
}

@PatchMapping("/me/password")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<Void> cambiarMiPassword(
        Principal principal,
        @Valid @RequestBody CambiarPasswordRequest request) {
    usuarioService.cambiarMiPassword(principal.getName(), request);
    return ResponseEntity.noContent().build();
}
}