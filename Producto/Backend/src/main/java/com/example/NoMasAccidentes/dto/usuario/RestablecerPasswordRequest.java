package com.example.NoMasAccidentes.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RestablecerPasswordRequest(
    @NotBlank String token,
    @NotBlank @Size(min = 6, max = 100) String nuevaPassword
) {}
