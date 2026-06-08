package com.example.NoMasAccidentes.dto.visita;

import com.example.NoMasAccidentes.model.visita.EstadoVisita;
import java.time.LocalDateTime;

public record VisitaResponse(
        Long id,
        Long idCliente,
        String cliente,
        Long idProfesional,
        String profesional,
        LocalDateTime fechaProgramada,
        String direccion,
        String motivo,
        EstadoVisita estado
) {}