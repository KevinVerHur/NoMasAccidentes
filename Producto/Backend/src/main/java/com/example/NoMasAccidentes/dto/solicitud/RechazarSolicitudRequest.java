package com.example.NoMasAccidentes.dto.solicitud;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** El admin rechaza una solicitud, indicando el motivo para el cliente. */
public record RechazarSolicitudRequest(

    @NotBlank(message = "Indica el motivo del rechazo")
    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    String comentario
) {}
