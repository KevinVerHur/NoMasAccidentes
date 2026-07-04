package com.example.NoMasAccidentes.dto.visita;

import com.example.NoMasAccidentes.model.visita.EstadoCumplimiento;

/** Resultado de un ítem de la lista de chequeo en una visita (RF19). */
public record ResultadoChequeoResponse(
    Long id,
    Long idItem,
    String descripcion,
    String categoria,
    String normaLegal,
    EstadoCumplimiento estado,
    String observacion
) {}
