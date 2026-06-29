package com.example.NoMasAccidentes.dto.reporte;

/**
 * Punto de la serie mensual de accidentabilidad de un cliente (RF40).
 * {@code tasa} = accidentes × 100 / nº de trabajadores; es null si el cliente
 * no tiene registrado el número de trabajadores.
 */
public record AccidentabilidadMensualResponse(
        int mes,
        long totalAccidentes,
        long diasPerdidos,
        Double tasa
) {}
