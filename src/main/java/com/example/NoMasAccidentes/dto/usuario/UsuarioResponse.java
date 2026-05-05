package com.example.NoMasAccidentes.dto.usuario;

import java.time.LocalDateTime;

/**
 * Vista pública de un Usuario. Nunca expone passwordHash.
 */
public record UsuarioResponse(
    Long id,
    String email,
    String nombre,
    String apellido,
    String rol,
    boolean activo,
    LocalDateTime ultimoAcceso,
    LocalDateTime fechaCreacion
) {}
