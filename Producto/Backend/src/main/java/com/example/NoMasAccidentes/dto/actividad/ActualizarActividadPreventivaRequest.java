package com.example.NoMasAccidentes.dto.actividad;

import com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ActualizarActividadPreventivaRequest(
        @NotBlank @Size(max = 160) String titulo,
        @Size(max = 1000) String descripcion,
        @Size(max = 120) String responsable,
        @NotNull LocalDate fechaPlanificada,
        @NotNull LocalDate fechaCompromiso,
        EstadoActividadPreventiva estado,
        @Size(max = 1000) String observaciones
) {}