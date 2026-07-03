package com.example.NoMasAccidentes.dto.visita;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Datos para planificar una visita (RF13). */
public record PlanificarVisitaRequest(

    @NotNull
    Long idEmpresa,

    @NotNull
    Long idProfesional,

    @NotNull
    LocalDate fechaProgramada,

    @Size(max = 20)
    String tipoRevision,

    Boolean esVisitaExtra
) {}
