package com.example.NoMasAccidentes.dto.capacitacion;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/** Inscribe uno o más asistentes a una capacitación (relación N:M). */
public record InscribirAsistentesRequest(

        @NotEmpty(message = "Debe indicar al menos un asistente")
        List<Long> idsAsistentes
) {}
