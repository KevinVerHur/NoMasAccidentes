package com.example.NoMasAccidentes.dto.visita;

import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Datos del registro de la visita en terreno (RF14). */
public record RegistrarVisitaRequest(

    @Size(max = 2000)
    String observaciones,

    BigDecimal latitud,

    BigDecimal longitud
) {}
