package com.example.NoMasAccidentes.dto.capacitacion;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;


/**
 * Request para programar una nueva capacitación.
 * Refleja los campos del formulario "Modal: Nueva Capacitación".
 *
 * La validación de los 15 días de anticipación es una regla de negocio
 * y se aplica en {@code CapacitacionService}, no aquí.
 */
public record CrearCapacitacionRequest(

        /** Dropdown "Cliente *" */
        @NotNull(message = "El cliente es obligatorio")
        Long idCliente,

        /** Campo libre "Curso *" */
        @NotBlank(message = "El curso es obligatorio")
        @Size(max = 150, message = "El curso no puede superar 150 caracteres")
        String curso,

        /** Dropdown "Relator *" → id del Profesional que dicta la capacitación */
        @NotNull(message = "El relator es obligatorio")
        Long idRelator,

        /** Campo fecha "Fecha *" (formato yyyy-MM-dd desde el frontend) */
        @NotNull(message = "La fecha es obligatoria")
        @Future(message = "La fecha programada debe ser una fecha futura")
        @JsonFormat(pattern = "yyyy-MM-dd")

        LocalDate fechaProgramada,
        
        /** Campo hora "Hora *" (formato HH:mm desde el frontend) */
        // DESPUÉS
        @NotBlank(message = "La hora es obligatoria")
        @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "Formato de hora inválido (HH:mm)")
        String horaProgramada,  
 
        @NotBlank(message = "El lugar es obligatorio")
        @Size(max = 150, message = "El lugar no puede superar 150 caracteres")
        String lugar,

        /** Campo numérico "Cupos" */
        @NotNull(message = "Los cupos son obligatorios")
        @Min(value = 1, message = "Debe haber al menos 1 cupo disponible")
        @Max(value = 500, message = "El máximo de cupos es 500")
        Integer cupos,

        /** Textarea "Objetivo" */
        @Size(max = 500, message = "El objetivo no puede superar 500 caracteres")
        String objetivo,

        /**
         * Flag de costo extra (RF-CAP4).
         * No aparece en el formulario básico pero se envía desde el backend
         * según el plan del cliente.
         */
        boolean esCapacitacionExtra
) {}
