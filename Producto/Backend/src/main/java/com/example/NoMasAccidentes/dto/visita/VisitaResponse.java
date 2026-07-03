package com.example.NoMasAccidentes.dto.visita;

import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record VisitaResponse(
    Long id,
    Long idEmpresa,
    String razonSocialEmpresa,
    Long idProfesional,
    String nombreProfesional,
    Long idListaChequeo,
    String tipoRevision,
    LocalDate fechaProgramada,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    EstadoVisita estado,
    BigDecimal latitud,
    BigDecimal longitud,
    String observaciones,
    boolean esVisitaExtra
) {}
