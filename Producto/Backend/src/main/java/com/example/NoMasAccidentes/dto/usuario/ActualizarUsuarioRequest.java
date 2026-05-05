package com.example.NoMasAccidentes.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del PUT /api/usuarios/{id}.
 * No incluye password — el cambio de contraseña va por endpoint dedicado.
 */
public record ActualizarUsuarioRequest(

    @NotBlank
    @Email
    @Size(max = 120)
    String email,

    @NotBlank
    @Size(max = 120)
    String nombre,

    @NotBlank
    @Size(max = 120)
    String apellido,

    @NotNull
    Long idRol
) {}
