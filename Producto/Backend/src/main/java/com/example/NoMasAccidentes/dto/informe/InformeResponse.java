package com.example.NoMasAccidentes.dto.informe;

import com.example.NoMasAccidentes.model.informe.EstadoInforme;
import java.time.LocalDate;

public record InformeResponse(
    Long id,
    Long idVisita,
    String razonSocialEmpresa,
    String nombreProfesional,
    LocalDate fechaEmision,
    EstadoInforme estado,
    String hallazgos,
    boolean tieneArchivo
) {}
