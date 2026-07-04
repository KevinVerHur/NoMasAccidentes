package com.example.NoMasAccidentes.dto.visita;

public record ItemChequeoResponse(
    Long id,
    String descripcion,
    String categoria,
    String normaLegal,
    boolean obligatorio,
    Integer orden
) {}
