package com.example.NoMasAccidentes.controller.dashboard;

import com.example.NoMasAccidentes.dto.dashboard.DashboardAdminResponse;
import com.example.NoMasAccidentes.dto.dashboard.DashboardClienteResponse;
import com.example.NoMasAccidentes.dto.dashboard.DashboardProfesionalResponse;
import com.example.NoMasAccidentes.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard del administrador: consolida KPIs, visitas recientes, alertas,
 * accidentabilidad y control de pagos en una sola respuesta (RF45).
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Información consolidada del panel del administrador (RF45)")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Datos consolidados del dashboard del administrador (RF45)")
    @GetMapping("/api/dashboard/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardAdminResponse admin() {
        return dashboardService.admin();
    }

    @Operation(summary = "Datos consolidados del dashboard del cliente autenticado (RF45)")
    @GetMapping("/api/dashboard/cliente")
    @PreAuthorize("hasRole('CLIENTE')")
    public DashboardClienteResponse cliente(Principal principal) {
        return dashboardService.cliente(principal.getName());
    }

    @Operation(summary = "Resumen de clientes asignados del profesional autenticado (RF45)")
    @GetMapping("/api/dashboard/profesional")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public DashboardProfesionalResponse profesional(Principal principal) {
        return dashboardService.profesional(principal.getName());
    }
}
