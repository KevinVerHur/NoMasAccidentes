package com.example.NoMasAccidentes.dto.actividad;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CrearActividadPreventivaRequest (
    @NotNull Long idCliente,
    @NotBlank @Size(max = 160) String titulo,
    @Size(max = 1000) String descripcion,
    @Size(max = 120) String normativa,
    @Size(max = 120) String responsable, 
    @NotNull LocalDate fechaPlanificada,
    @NotNull LocalDate fechaCompromiso, 
    @Size(max = 1000) String observaciones

) {}
