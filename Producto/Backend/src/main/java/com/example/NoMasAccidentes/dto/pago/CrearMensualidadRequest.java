package com.example.NoMasAccidentes.dto.pago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Crear un plan del catálogo (RF08). */
public record CrearMensualidadRequest(

    @NotBlank @Size(max = 80)
    String nombrePlan,

    @NotNull @Positive
    BigDecimal montoBase,

    Integer visitasIncluidas,
    Integer asesoriasIncluidas,
    Integer capacitacionesIncluidas,
    BigDecimal costoVisitaExtra,
    BigDecimal costoAsesoriaExtra,
    BigDecimal costoCapacitacionExtra,
    BigDecimal costoLlamadoFueraHorario
) {}
