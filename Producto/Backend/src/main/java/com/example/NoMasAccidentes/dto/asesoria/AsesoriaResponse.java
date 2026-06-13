package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.EstadoAsesoria;
import com.example.NoMasAccidentes.model.asesoria.TipoAsesoria;
import java.time.LocalDate;

public record AsesoriaResponse(
    Long id,
    Long idCliente,
    String razonSocialCliente,
    Long idProfesional,
    String nombreProfesional,
    LocalDate fechaSolicitud,
    LocalDate fechaAtencion,
    String motivo,
    TipoAsesoria tipo,
    EstadoAsesoria estado,
    boolean esAsesoriaExtra
) {}
