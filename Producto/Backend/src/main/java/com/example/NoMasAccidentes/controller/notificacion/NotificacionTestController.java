package com.example.NoMasAccidentes.controller.notificacion;

import com.example.NoMasAccidentes.service.notificacion.NotificacionScheduler;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ⚠️ TEMPORAL — SOLO PARA PRUEBAS MANUALES DE RF30/RF32. ELIMINAR ANTES DE PRODUCCIÓN.
 *
 * Dispara a demanda los jobs de notificación que normalmente corren por cron,
 * para poder verificar capacitaciones (recordatorio/incumplimiento) y actividades
 * preventivas vencidas sin esperar a la hora programada.
 */
@RestController
@RequestMapping("/api/test/notificaciones")
@RequiredArgsConstructor
@Tag(name = "TEST notificaciones", description = "TEMPORAL: dispara los jobs de notificación a demanda")
public class NotificacionTestController {

    private final NotificacionScheduler scheduler;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> dispararTodos() {
        scheduler.enviarRecordatoriosCapacitaciones();
        scheduler.notificarCapacitacionesNoRealizadas();
        scheduler.notificarActividadesPreventivasVencidas();
        return Map.of("estado", "Jobs de notificación disparados. Revisa los logs, Mailpit y los flags en la BD.");
    }
}
