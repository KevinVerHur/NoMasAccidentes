package com.example.NoMasAccidentes.dto.visita;

import java.time.LocalDate;
import java.util.List;

public record ListaChequeoResponse(
    Long id,
    Long idEmpresa,
    String nombre,
    Integer cambiosRealizadosAnio,
    Integer anioVigente,
    LocalDate fechaUltimaModificacion,
    List<ItemChequeoResponse> items
) {}
