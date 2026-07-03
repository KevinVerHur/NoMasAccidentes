package com.example.NoMasAccidentes.dto.actividad;

import jakarta.validation.constraints.Size;

/** El cliente reporta que cumplió su parte de una actividad preventiva (comentario opcional). */
public record ReportarCumplimientoRequest(
    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
    String comentario
) {}
