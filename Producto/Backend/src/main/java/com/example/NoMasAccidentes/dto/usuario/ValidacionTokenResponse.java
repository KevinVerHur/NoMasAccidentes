package com.example.NoMasAccidentes.dto.usuario;

/**
 * Resultado de validar un token de restablecimiento de contraseña.
 * {@code valido} es true solo si el token existe, no fue usado y no ha expirado.
 */
public record ValidacionTokenResponse(boolean valido) {}
