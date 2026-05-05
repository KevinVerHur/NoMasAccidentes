package com.example.NoMasAccidentes.common;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estructura JSON estandar devuelta por GlobalExpetionHandler ante cualquier error.
 * Permite al frontend tener un contrato uniforme para mostrar mensajes al usuario.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> errores
) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, List.of());
    }

    public static ApiError of(int status, String error, String message, String path, List<String> errores) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, errores);
    }
}