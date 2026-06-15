package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.informe.EstadoInforme;
import java.time.LocalDate;

public record InformeAsesoriaResponse(
    Long id,
    Long idAsesoria,
    LocalDate fechaEmision,
    EstadoInforme estado,
    String contenido,
    String hallazgos,
    boolean tieneArchivo
) {}
