package com.example.NoMasAccidentes.dto.pago;

import com.example.NoMasAccidentes.model.pago.EstadoPago;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoResponse(
    Long id,
    Long idPlan,
    Long idEmpresa,
    String razonSocialEmpresa,
    Integer numeroCuota,
    BigDecimal monto,
    LocalDate fechaEmision,
    LocalDate fechaVencimiento,
    LocalDate fechaPago,
    String medioPago,
    EstadoPago estadoPago
) {}
