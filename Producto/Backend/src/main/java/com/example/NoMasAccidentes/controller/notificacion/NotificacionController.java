package com.example.NoMasAccidentes.controller.notificacion;

import com.example.NoMasAccidentes.dto.notificacion.NotificacionResponse;
import com.example.NoMasAccidentes.service.notificacion.NotificacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bandeja de notificaciones in-app del usuario autenticado (cualquier rol).
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Bandeja de notificaciones in-app del usuario")
public class NotificacionController {

    private final NotificacionService service;

    /** Notificaciones del usuario autenticado, más recientes primero. */
    @GetMapping("/mias")
    @PreAuthorize("isAuthenticated()")
    public List<NotificacionResponse> mias(Principal principal) {
        return service.misNotificaciones(principal.getName());
    }

    /** Cantidad de no leídas (badge del sidebar). */
    @GetMapping("/mias/no-leidas/count")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Long> contarNoLeidas(Principal principal) {
        return Map.of("noLeidas", service.contarNoLeidas(principal.getName()));
    }

    /** Marca una notificación propia como leída. */
    @PatchMapping("/{id}/leida")
    @PreAuthorize("isAuthenticated()")
    public NotificacionResponse marcarLeida(@PathVariable Long id, Principal principal) {
        return service.marcarLeida(id, principal.getName());
    }

    /** Marca todas las notificaciones propias como leídas. */
    @PatchMapping("/mias/leer-todas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> leerTodas(Principal principal) {
        service.marcarTodasLeidas(principal.getName());
        return ResponseEntity.noContent().build();
    }
}
