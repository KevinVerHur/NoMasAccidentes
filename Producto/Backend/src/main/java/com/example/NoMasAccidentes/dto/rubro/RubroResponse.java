package com.example.NoMasAccidentes.dto.rubro;

import java.math.BigDecimal;

public record RubroResponse(
    Long id,
    String nombre,
    BigDecimal tasaAccidentabilidad
) {}
