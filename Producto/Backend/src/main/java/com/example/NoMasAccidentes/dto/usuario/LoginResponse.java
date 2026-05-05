package com.example.NoMasAccidentes.dto.usuario;

public record LoginResponse(
        String token,
        String email,
        String nombreCompleto,
        String rol
) {
}
