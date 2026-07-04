package com.example.NoMasAccidentes.dto.visita;

import com.example.NoMasAccidentes.model.visita.EstadoCumplimiento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Resultado de un ítem de la lista de chequeo en la visita (RF19). */
public record ResultadoChequeoRequest(

    @NotNull
    Long idItem,

    @NotNull
    EstadoCumplimiento estado,

    @Size(max = 500)
    String observacion
) {}
