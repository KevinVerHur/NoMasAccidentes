package com.example.NoMasAccidentes.dto.capacitacion;

import java.time.LocalDateTime;

public record AsistenciaResponse(
        Long idAsistencia,
        Long idAsistente,
        String nombreAsistente,   // "Nombre Apellidos"
        String rutAsistente,
        String cargoAsistente,
        boolean confirmado,
        boolean asistio,
        LocalDateTime fechaConfirmacion,
        String observaciones,
        String firmaDigital
) {}
