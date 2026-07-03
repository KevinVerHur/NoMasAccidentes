package com.example.NoMasAccidentes.dto.notificacion;

import com.example.NoMasAccidentes.model.notificacion.TipoNotificacion;
import java.time.LocalDateTime;

/** Notificación in-app tal como la consume el frontend (bandeja del portal). */
public record NotificacionResponse(
        Long id,
        TipoNotificacion tipo,
        String titulo,
        String mensaje,
        String enlace,
        boolean leida,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaLeida
) {}
