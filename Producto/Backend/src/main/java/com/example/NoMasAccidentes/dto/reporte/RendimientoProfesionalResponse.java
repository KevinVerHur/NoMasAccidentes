package com.example.NoMasAccidentes.dto.reporte;

/**
 * Indicador de rendimiento de un profesional en un periodo (RF41).
 * {@code cumplimientoVisitas} = visitas realizadas × 100 / programadas; null si
 * no tuvo visitas programadas en el periodo.
 */
public record RendimientoProfesionalResponse(
        Long idProfesional,
        String nombreProfesional,
        long visitasRealizadas,
        long visitasProgramadas,
        long asesoriasAtendidas,
        long capacitacionesDictadas,
        Double cumplimientoVisitas
) {}
