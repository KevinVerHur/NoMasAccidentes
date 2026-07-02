package com.example.NoMasAccidentes.dto.reporte;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Vista de un reporte mensual de gestión por cliente (RF38, RF39).
 */
public record ReporteMensualResponse(
        Long id,
        Long idEmpresa,
        String razonSocialEmpresa,
        int mes,
        int anio,
        LocalDate fechaEmision,
        int totalVisitas,
        int totalCapacitaciones,
        int totalAsesorias,
        int totalLlamados,
        int totalAccidentes,
        int totalMultas,
        BigDecimal costosExtra,
        boolean esActualizacionExtra,
        boolean tieneArchivo
) {}
