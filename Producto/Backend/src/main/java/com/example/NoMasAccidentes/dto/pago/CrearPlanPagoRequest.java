package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.Periodicidad;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/** Asignar un plan de pago a un cliente; genera las cuotas (RF08). */
public record CrearPlanPagoRequest(

    @NotNull
    Long idCliente,

    @NotNull
    Long idMensualidad,

    @NotNull
    LocalDate fechaInicio,

    @NotNull @Positive
    Integer cuotasTotales,

    Periodicidad periodicidad
) {}
