package com.example.NoMasAccidentes.dto.asesoria;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CrearMultaRequest(

    @NotNull(message = "La fiscalización es obligatoria")
    Long idFiscalizacion,

    @NotNull(message = "La fecha de emisión es obligatoria")
    @PastOrPresent(message = "La fecha de emisión no puede ser futura")
    LocalDate fechaEmision,

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    BigDecimal monto,

    @Size(max = 1000, message = "El motivo no puede superar los 1000 caracteres")
    String motivo,

    @Size(max = 150, message = "La normativa infringida no puede superar los 150 caracteres")
    String normativaInfringida
) {}
