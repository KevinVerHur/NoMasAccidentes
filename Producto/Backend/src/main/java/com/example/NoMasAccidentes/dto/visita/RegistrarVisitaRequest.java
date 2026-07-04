package com.example.NoMasAccidentes.dto.visita;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

/** Datos del registro de la visita en terreno (RF14) con el resultado del chequeo (RF19). */
public record RegistrarVisitaRequest(

    @Size(max = 2000)
    String observaciones,

    BigDecimal latitud,

    BigDecimal longitud,

    /** Marcado de la lista de chequeo (Cumple/No cumple/No aplica por ítem). */
    @Valid
    List<ResultadoChequeoRequest> resultados
) {}
