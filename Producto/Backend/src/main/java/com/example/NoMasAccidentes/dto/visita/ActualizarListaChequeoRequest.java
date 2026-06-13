package com.example.NoMasAccidentes.dto.visita;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Modificar la lista de chequeo (RF17: máximo 2 veces al año). */
public record ActualizarListaChequeoRequest(

    @Size(max = 120)
    String nombre,

    @Valid
    List<ItemChequeoRequest> items
) {}
