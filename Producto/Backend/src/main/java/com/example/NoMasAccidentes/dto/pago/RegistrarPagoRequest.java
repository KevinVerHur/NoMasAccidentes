package com.example.NoMasAccidentes.dto.pago;

import jakarta.validation.constraints.Size;

/** Registrar el pago de una cuota (RF09). */
public record RegistrarPagoRequest(

    @Size(max = 40)
    String medioPago
) {}
