package com.example.NoMasAccidentes.dto.asesoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CrearPropuestaMejoraRequest(

    @NotNull(message = "El informe es obligatorio")
    Long idInforme,

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres")
    String descripcion,

    LocalDate fechaLimite,

    @Size(max = 120, message = "El responsable no puede superar los 120 caracteres")
    String responsable
) {}
