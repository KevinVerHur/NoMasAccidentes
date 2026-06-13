package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.TipoCobro;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Agregar un cobro extra a una cuota (RF21, RF24, RF28). */
public record CrearCobroExtraRequest(

    @NotNull
    TipoCobro tipoCobro,

    Long idOrigen,

    @Size(max = 500)
    String descripcion,

    @NotNull @Positive
    BigDecimal monto
) {}
