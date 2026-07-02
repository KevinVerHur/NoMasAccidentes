package com.example.NoMasAccidentes.dto.capacitacion;

import com.example.NoMasAccidentes.model.capacitacion.EstadoCapacitacion;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * Respuesta completa de una capacitación.
 * Incluye todos los campos del formulario y el listado de asistentes inscritos.
 */
public record CapacitacionResponse(
        Long id,

        // — Campos del formulario —
        Long idEmpresa,
        String razonSocialEmpresa,   // razonSocial de la Empresa

        String curso,

        Long idRelator,
        String relator,              // "Nombre Apellido" del Profesional

     
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fechaProgramada,
        

        @JsonFormat(pattern = "HH:mm")
        String horaProgramada,
        
        String lugar,
        Integer cupos,
        String objetivo,

        // — Campos de control —
        LocalDate fechaRealizacion,
        EstadoCapacitacion estado,
        boolean esCapacitacionExtra,
        String observacionActa,
        /** Cupos disponibles = cupos - asistentes inscritos confirmados */
        int cuposDisponibles,

        List<AsistenciaResponse> asistencias
) {}
