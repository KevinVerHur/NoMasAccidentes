package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.EntidadFiscalizadora;
import com.example.NoMasAccidentes.model.asesoria.ResultadoFiscalizacion;
import java.time.LocalDate;

public record FiscalizacionResponse(
    Long id,
    Long idAsesoria,
    LocalDate fecha,
    EntidadFiscalizadora entidadFiscalizadora,
    String motivo,
    ResultadoFiscalizacion resultado,
    String observaciones
) {}
