package com.example.NoMasAccidentes.dto.solicitud;

import com.example.NoMasAccidentes.model.solicitud.TipoSolicitud;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** El cliente solicita un servicio desde el portal (crea una solicitud PENDIENTE). */
public record CrearSolicitudRequest(

    @NotNull(message = "El tipo de solicitud es obligatorio (ASESORIA, CAPACITACION o VISITA)")
    TipoSolicitud tipo,

    @NotBlank(message = "Describe brevemente lo que necesitas")
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    String descripcion,

    /** Fecha preferida (opcional, orientativa para la consultora). */
    LocalDate fechaPreferida
) {}
