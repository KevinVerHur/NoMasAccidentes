package com.example.NoMasAccidentes.dto.visita;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemChequeoRequest(

    @NotBlank @Size(max = 250)
    String descripcion,

    @Size(max = 80)
    String categoria,

    @Size(max = 250)
    String normaLegal,

    Boolean obligatorio,

    Integer orden
) {}
