package com.example.NoMasAccidentes.controller.informe;

import com.example.NoMasAccidentes.dto.informe.InformeResponse;
import com.example.NoMasAccidentes.service.informe.InformeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Informes posteriores a la visita (RF15).
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Informes", description = "Informes posteriores a la visita (RF15)")
public class InformeController {

    private final InformeService informeService;

    @Operation(summary = "Generar el informe PDF de una visita realizada (RF15)")
    @PostMapping("/api/visitas/{idVisita}/informe")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ResponseEntity<InformeResponse> generar(@PathVariable Long idVisita) {
        InformeResponse informe = informeService.generarParaVisita(idVisita);
        return ResponseEntity
                .created(URI.create("/api/informes/" + informe.id()))
                .body(informe);
    }

    @Operation(summary = "Obtener el informe de una visita")
    @GetMapping("/api/visitas/{idVisita}/informe")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public InformeResponse obtenerPorVisita(@PathVariable Long idVisita) {
        return informeService.obtenerPorVisita(idVisita);
    }

    @Operation(summary = "Descargar el PDF del informe")
    @GetMapping("/api/informes/{id}/descarga")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        return pdfResponse(id, informeService.descargarPdf(id));
    }

    // ---- Portal cliente (solo lectura de lo propio) ----

    @Operation(summary = "Listar los informes del cliente autenticado (RF15)")
    @GetMapping("/api/mis-informes")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<InformeResponse> misInformes(Principal principal) {
        return informeService.listarMisInformes(principal.getName());
    }

    @Operation(summary = "Descargar un informe propio del cliente")
    @GetMapping("/api/mis-informes/{id}/descarga")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<byte[]> descargarMio(@PathVariable Long id, Principal principal) {
        return pdfResponse(id, informeService.descargarMiPdf(id, principal.getName()));
    }

    private ResponseEntity<byte[]> pdfResponse(Long id, byte[] pdf) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"informe-" + id + ".pdf\"")
                .body(pdf);
    }
}
