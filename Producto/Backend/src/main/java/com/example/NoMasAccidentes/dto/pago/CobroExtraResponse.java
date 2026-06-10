package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.TipoCobro;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CobroExtraResponse(
    Long id,
    Long idPago,
    TipoCobro tipoCobro,
    Long idOrigen,
    String descripcion,
    BigDecimal monto,
    LocalDate fechaGeneracion
) {}
