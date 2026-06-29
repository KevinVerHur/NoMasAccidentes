package com.example.NoMasAccidentes.controller.reporte;

import com.example.NoMasAccidentes.dto.reporte.AccidentabilidadMensualResponse;
import com.example.NoMasAccidentes.dto.reporte.RendimientoProfesionalResponse;
import com.example.NoMasAccidentes.service.reporte.IndicadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Indicadores de gestión: accidentabilidad por cliente (RF40) y rendimiento por
 * profesional (RF41).
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Indicadores", description = "Indicadores de accidentabilidad (RF40) y rendimiento profesional (RF41)")
public class IndicadorController {

    private final IndicadorService indicadorService;

    @Operation(summary = "Serie mensual de accidentabilidad de un cliente para el año (RF40)")
    @GetMapping("/api/indicadores/accidentabilidad")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<AccidentabilidadMensualResponse> accidentabilidad(
            @RequestParam Long idCliente,
            @RequestParam int anio) {
        return indicadorService.accidentabilidadAnual(idCliente, anio);
    }

    @Operation(summary = "Rendimiento de los profesionales en un periodo (RF41)")
    @GetMapping("/api/indicadores/rendimiento-profesional")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<RendimientoProfesionalResponse> rendimientoProfesional(
            @RequestParam int mes,
            @RequestParam int anio) {
        return indicadorService.rendimientoProfesional(mes, anio);
    }

    @Operation(summary = "Mi serie mensual de accidentabilidad (cliente autenticado, RF40)")
    @GetMapping("/api/mis-indicadores/accidentabilidad")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<AccidentabilidadMensualResponse> miAccidentabilidad(
            @RequestParam int anio, Principal principal) {
        return indicadorService.miAccidentabilidad(principal.getName(), anio);
    }
}
