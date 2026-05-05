package com.example.NoMasAccidentes.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del PATCH /api/usuarios/{id}/password.
 * Requiere la contraseña actual para confirmar la identidad.
 */
public record CambiarPasswordRequest(

    @NotBlank
    String passwordActual,

    @NotBlank
    @Size(min = 8, max = 100, message = "La nueva contraseña debe tener entre 8 y 100 caracteres")
    String passwordNueva
) {}
