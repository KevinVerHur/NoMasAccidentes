package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.EstadoMulta;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MultaResponse(
    Long id,
    Long idFiscalizacion,
    LocalDate fechaEmision,
    BigDecimal monto,
    String motivo,
    String normativaInfringida,
    EstadoMulta estadoPago
) {}
