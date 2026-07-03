package com.example.NoMasAccidentes.dto.actividad;

import com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ActividadPreventivaResponse(
    Long id,
    Long idEmpresa,
    String razonSocialEmpresa,
    String titulo,
    String descripcion,
    String normativa,
    String responsable,
    LocalDate fechaPlanificada,
    LocalDate fechaCompromiso,
    LocalDate fechaCumplimiento,
    EstadoActividadPreventiva estado,
    String observaciones,
    boolean vencida,
    boolean reportadoPorCliente,
    LocalDateTime fechaReporteCliente,
    String comentarioCliente
) {}
