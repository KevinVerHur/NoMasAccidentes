package com.example.NoMasAccidentes.dto.visita;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Crear la lista de chequeo de una empresa (RF16). */
public record CrearListaChequeoRequest(

    @NotNull
    Long idEmpresa,

    @Size(max = 120)
    String nombre,

    @Valid
    List<ItemChequeoRequest> items
) {}
