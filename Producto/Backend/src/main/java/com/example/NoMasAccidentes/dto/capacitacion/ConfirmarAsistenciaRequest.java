package com.example.NoMasAccidentes.dto.capacitacion;

import jakarta.validation.constraints.Size;

/** Payload opcional al confirmar asistencia. El asistente puede adjuntar una observación. */
public record ConfirmarAsistenciaRequest(

        @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
        String observaciones,
        @Size(max = 300, message = "La firma digital no puede superar 300 caracteres")
        String firmaDigital
) {}
