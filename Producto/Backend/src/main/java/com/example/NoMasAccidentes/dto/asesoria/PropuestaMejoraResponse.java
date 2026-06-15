package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.EstadoPropuesta;
import java.time.LocalDate;

public record PropuestaMejoraResponse(
    Long id,
    Long idInforme,
    String descripcion,
    LocalDate fechaPropuesta,
    LocalDate fechaLimite,
    LocalDate fechaVerificacion,
    EstadoPropuesta estado,
    String responsable
) {}
