package com.example.NoMasAccidentes.controller.reporte;

import com.example.NoMasAccidentes.dto.reporte.ReporteMensualResponse;
import com.example.NoMasAccidentes.service.reporte.ReporteMensualScheduler;
import com.example.NoMasAccidentes.service.reporte.ReporteMensualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reportes mensuales de gestión por cliente (RF38, RF39, RF42).
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Reportes mensuales de gestión por cliente (RF38, RF39, RF42)")
public class ReporteController {

    private final ReporteMensualService reporteService;
    private final ReporteMensualScheduler reporteScheduler;

    @Operation(summary = "Ejecutar el cierre mensual: genera el reporte del periodo para todos los clientes (RF46)")
    @PostMapping("/api/reportes/cierre-mensual")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> cierreMensual(@RequestParam int mes, @RequestParam int anio) {
        return Map.of("reportesGenerados", reporteScheduler.generarCierreMensual(mes, anio));
    }

    @Operation(summary = "Generar (o regenerar) el reporte mensual de un cliente (RF38, RF39)")
    @PostMapping("/api/reportes/generar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ResponseEntity<ReporteMensualResponse> generar(
            @RequestParam Long idEmpresa,
            @RequestParam int mes,
            @RequestParam int anio) {
        return ResponseEntity.ok(reporteService.generar(idEmpresa, mes, anio));
    }

    @Operation(summary = "Listar los reportes mensuales de una empresa (RF38, RF42)")
    @GetMapping("/api/reportes")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<ReporteMensualResponse> listarPorEmpresa(@RequestParam Long idEmpresa) {
        return reporteService.listarPorEmpresa(idEmpresa);
    }

    @Operation(summary = "Obtener un reporte mensual por id")
    @GetMapping("/api/reportes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ReporteMensualResponse obtener(@PathVariable Long id) {
        return reporteService.obtener(id);
    }

    @Operation(summary = "Descargar el PDF de un reporte mensual")
    @GetMapping("/api/reportes/{id}/descarga")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        return pdfResponse(id, reporteService.descargarPdf(id));
    }

    // ---- Portal cliente ----

    @Operation(summary = "Listar mis reportes mensuales (cliente autenticado)")
    @GetMapping("/api/mis-reportes")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<ReporteMensualResponse> misReportes(Principal principal) {
        return reporteService.listarMisReportes(principal.getName());
    }

    @Operation(summary = "Descargar uno de mis reportes mensuales (cliente autenticado)")
    @GetMapping("/api/mis-reportes/{id}/descarga")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<byte[]> descargarMio(@PathVariable Long id, Principal principal) {
        return pdfResponse(id, reporteService.descargarMiPdf(id, principal.getName()));
    }

    private ResponseEntity<byte[]> pdfResponse(Long id, byte[] pdf) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"reporte-mensual-" + id + ".pdf\"")
                .body(pdf);
    }
}
