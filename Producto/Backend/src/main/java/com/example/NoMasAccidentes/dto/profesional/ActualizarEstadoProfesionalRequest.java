package com.example.NoMasAccidentes.dto.profesional;

import com.example.NoMasAccidentes.model.profesional.EstadoProfesional;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoProfesionalRequest(
    @NotNull(message = "El estado del profesional es obligatorio")
    EstadoProfesional estado
) {}
