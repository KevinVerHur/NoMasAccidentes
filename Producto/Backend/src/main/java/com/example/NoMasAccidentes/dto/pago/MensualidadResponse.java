package com.example.NoMasAccidentes.dto.pago;

import java.math.BigDecimal;

public record MensualidadResponse(
    Long id,
    String nombrePlan,
    BigDecimal montoBase,
    Integer visitasIncluidas,
    Integer asesoriasIncluidas,
    Integer capacitacionesIncluidas,
    BigDecimal costoVisitaExtra,
    BigDecimal costoAsesoriaExtra,
    BigDecimal costoCapacitacionExtra,
    BigDecimal costoLlamadoFueraHorario,
    boolean activo
) {}
