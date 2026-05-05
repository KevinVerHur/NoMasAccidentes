package com.example.NoMasAccidentes.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo del POST /api/auth/login.
 */
public record LoginRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {}
