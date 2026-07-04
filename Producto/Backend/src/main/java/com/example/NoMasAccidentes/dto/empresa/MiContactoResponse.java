package com.example.NoMasAccidentes.dto.empresa;

/**
 * Datos de contacto del representante autenticado (portal cliente).
 * El email es de solo lectura: es la credencial de acceso.
 */
public record MiContactoResponse(
    Long id,
    String nombre,
    String cargo,
    String email,
    String telefono
) {}
