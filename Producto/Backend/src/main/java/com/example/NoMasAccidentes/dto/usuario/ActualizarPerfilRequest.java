package com.example.NoMasAccidentes.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del PUT /api/usuarios/me.
 * Permite actualizar datos propios sin cambiar rol ni permisos.
 */
public record ActualizarPerfilRequest(

    @NotBlank
    @Email
    @Size(max = 120)
    String email,

    @NotBlank
    @Size(max = 120)
    String nombre,

    @NotBlank
    @Size(max = 120)
    String apellido
) {}
