package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.Periodicidad;
import java.time.LocalDate;

public record PlanPagoResponse(
    Long id,
    Long idEmpresa,
    String razonSocialEmpresa,
    Long idMensualidad,
    String nombrePlan,
    LocalDate fechaInicio,
    LocalDate fechaTermino,
    Integer cuotasTotales,
    Periodicidad periodicidad,
    boolean activo
) {}
