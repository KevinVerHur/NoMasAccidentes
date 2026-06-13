package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.GravedadAccidente;
import java.time.LocalDate;

public record AccidenteResponse(
    Long id,
    Long idAsesoria,
    LocalDate fechaOcurrencia,
    String descripcion,
    GravedadAccidente gravedad,
    String trabajadorAfectado,
    Integer diasPerdidos,
    boolean fueReportadoSusseso
) {}
