package com.example.NoMasAccidentes.dto.asistente;

public record AsistenteResponse(
        Long id,
        Long idCliente,
        String cliente,
        String rut,
        String nombre,
        String apellidos,
        String nombreCompleto,
        String cargo,
        String area,
        String email
) {}
