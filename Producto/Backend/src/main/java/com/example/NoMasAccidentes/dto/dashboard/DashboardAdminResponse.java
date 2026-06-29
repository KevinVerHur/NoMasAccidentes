package com.example.NoMasAccidentes.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos consolidados del dashboard del administrador (RF45: centralización de
 * información). Agrega indicadores y resúmenes de los módulos operativos en una
 * sola respuesta para evitar múltiples llamadas desde el front.
 */
public record DashboardAdminResponse(
        Kpis kpis,
        List<VisitaReciente> visitasRecientes,
        List<Alerta> alertas,
        List<AccidentabilidadCliente> accidentabilidad,
        List<ControlPago> controlPagos
) {

    /** Tarjetas numéricas de la cabecera. */
    public record Kpis(
            long clientesActivos,
            long visitasPendientesSemana,
            long clientesMorosos,
            long capacitacionesMes
    ) {}

    /** Fila de la tabla "Visitas recientes". {@code estado} es el nombre del enum EstadoVisita. */
    public record VisitaReciente(
            Long idCliente,
            String cliente,
            String profesional,
            LocalDate fecha,
            String estado
    ) {}

    /** Alerta derivada. {@code severidad} ∈ {peligro, warn, info}. */
    public record Alerta(
            String severidad,
            String titulo,
            String detalle
    ) {}

    /** Ranking de accidentabilidad por cliente del año (RF40). {@code tasa} null si no hay nº de trabajadores. */
    public record AccidentabilidadCliente(
            Long idCliente,
            String cliente,
            long accidentes,
            Integer trabajadores,
            Double tasa
    ) {}

    /** Fila de la tabla "Control de pagos y morosidades". {@code estado} ∈ {Al día, Atrasado, Moroso, Suspendido}. */
    public record ControlPago(
            Long idCliente,
            String cliente,
            BigDecimal planMensual,
            LocalDate ultimoPago,
            long mesesAdeudados,
            String estado
    ) {}
}
