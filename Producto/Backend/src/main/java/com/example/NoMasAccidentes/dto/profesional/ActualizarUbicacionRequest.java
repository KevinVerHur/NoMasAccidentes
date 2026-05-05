package com.example.NoMasAccidentes.dto.profesional;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Cuerpo del PATCH /api/profesionales/{id}/ubicacion.
 * Reportar coordenadas GPS actuales del profesional para visitas en terreno.
 */
public record ActualizarUbicacionRequest(

    @NotNull
    @DecimalMin(value = "-90.0",  message = "Latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0",   message = "Latitud debe estar entre -90 y 90")
    BigDecimal latitud,

    @NotNull
    @DecimalMin(value = "-180.0", message = "Longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0",  message = "Longitud debe estar entre -180 y 180")
    BigDecimal longitud
) {}
