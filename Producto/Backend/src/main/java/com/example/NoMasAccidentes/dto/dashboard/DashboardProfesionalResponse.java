package com.example.NoMasAccidentes.dto.dashboard;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos del dashboard del profesional que no provienen de sus visitas (ya
 * cargadas aparte): el resumen de sus clientes asignados.
 */
public record DashboardProfesionalResponse(
        long clientesAsignados,
        long clientesMorosos,
        List<ClienteAsignado> clientes
) {

    /** {@code estado} es el nombre del enum EstadoCliente (ACTIVO/MOROSO/SUSPENDIDO). */
    public record ClienteAsignado(
            Long idCliente,
            String razonSocial,
            String rubro,
            LocalDate ultimaVisita,
            String estado
    ) {}
}
