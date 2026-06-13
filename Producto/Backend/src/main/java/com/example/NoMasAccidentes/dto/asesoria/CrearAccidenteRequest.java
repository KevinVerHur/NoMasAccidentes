package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.GravedadAccidente;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CrearAccidenteRequest(

    @NotNull(message = "La asesoría es obligatoria")
    Long idAsesoria,

    @NotNull(message = "La fecha de ocurrencia es obligatoria")
    @PastOrPresent(message = "La fecha de ocurrencia no puede ser futura")
    LocalDate fechaOcurrencia,

    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    String descripcion,

    @NotNull(message = "La gravedad es obligatoria (LEVE, GRAVE o FATAL)")
    GravedadAccidente gravedad,

    @Size(max = 150, message = "El trabajador afectado no puede superar los 150 caracteres")
    String trabajadorAfectado,

    @PositiveOrZero(message = "Los días perdidos no pueden ser negativos")
    Integer diasPerdidos,

    boolean fueReportadoSusseso
) {}
