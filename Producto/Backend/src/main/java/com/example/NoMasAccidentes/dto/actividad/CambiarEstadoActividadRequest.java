package com.example.NoMasAccidentes.dto.actividad;

import com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoActividadRequest(
    @NotNull EstadoActividadPreventiva estado,
    String observaciones
) {}
