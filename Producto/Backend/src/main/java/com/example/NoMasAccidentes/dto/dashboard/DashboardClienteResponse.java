package com.example.NoMasAccidentes.dto.dashboard;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos consolidados del dashboard del cliente (portal cliente): cumplimiento de
 * visitas, capacitaciones, asesorías y estado de pago, más sus próximas
 * actividades y un resumen preventivo. Solo lectura de lo propio.
 */
public record DashboardClienteResponse(
        Kpis kpis,
        List<Accion> accionesImportantes,
        List<ProximaActividad> proximasActividades,
        ResumenPreventivo resumen
) {

    public record Kpis(
            long visitasRealizadasMes,
            long visitasProgramadasMes,
            long capacitacionesPendientes,
            long asesoriasUsadas,
            int asesoriasLimite,
            String estadoPago,
            LocalDate proximoVencimiento
    ) {}

    /** {@code severidad} ∈ {peligro, warn, info, ok}. */
    public record Accion(String severidad, String titulo, String detalle) {}

    public record ProximaActividad(
            LocalDate fecha,
            String actividad,
            String profesional,
            String estado
    ) {}

    public record ResumenPreventivo(
            long accidentesMes,
            long diasPerdidosMes,
            long accidentesAnio,
            long capacitacionesRealizadasAnio
    ) {}
}
