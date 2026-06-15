package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.EntidadFiscalizadora;
import com.example.NoMasAccidentes.model.asesoria.ResultadoFiscalizacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CrearFiscalizacionRequest(

    @NotNull(message = "La asesoría es obligatoria")
    Long idAsesoria,

    @NotNull(message = "La fecha de la fiscalización es obligatoria")
    @PastOrPresent(message = "La fecha de la fiscalización no puede ser futura")
    LocalDate fecha,

    @NotNull(message = "La entidad fiscalizadora es obligatoria")
    EntidadFiscalizadora entidadFiscalizadora,

    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    String motivo,

    ResultadoFiscalizacion resultado,

    @Size(max = 2000, message = "Las observaciones no pueden superar los 2000 caracteres")
    String observaciones
) {}
