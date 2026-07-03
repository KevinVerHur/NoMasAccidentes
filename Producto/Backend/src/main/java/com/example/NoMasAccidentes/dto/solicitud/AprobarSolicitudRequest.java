package com.example.NoMasAccidentes.dto.solicitud;

import com.example.NoMasAccidentes.model.asesoria.TipoAsesoria;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * El admin aprueba una solicitud: el sistema crea el recurso real (asesoría,
 * visita o capacitación) con los datos que complete aquí. Los campos aplican
 * según el tipo de la solicitud; el servicio valida los requeridos por tipo.
 */
public record AprobarSolicitudRequest(

    /** Comentario para el cliente (opcional). */
    @Size(max = 500)
    String comentario,

    /** Confirmación del admin de si el servicio se cobra extra (fuera del plan). */
    Boolean esExtra,

    // ── Común a asesoría y visita; para capacitación es el relator ──
    Long idProfesional,

    // ── Asesoría ──
    TipoAsesoria tipoAsesoria,

    // ── Visita y capacitación ──
    LocalDate fechaProgramada,

    // ── Visita ──
    @Size(max = 20)
    String tipoRevision,

    // ── Capacitación ──
    @Size(max = 150)
    String curso,
    /** Formato HH:mm. */
    String horaProgramada,
    @Size(max = 150)
    String lugar,
    Integer cupos,
    @Size(max = 500)
    String objetivo
) {}
