package com.example.NoMasAccidentes.dto.checklist;

import java.time.LocalDateTime;

public record ListaChequeoResponse(
        Long id,
        Long idCliente,
        String razonSocialCliente,
        String nombre,
        String descripcion,
        String contenido,
        Integer anioControl,
        Integer modificacionesAnio,
        Integer modificacionesDisponibles,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}