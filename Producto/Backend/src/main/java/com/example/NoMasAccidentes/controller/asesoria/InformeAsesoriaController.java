package com.example.NoMasAccidentes.controller.asesoria;

import com.example.NoMasAccidentes.dto.asesoria.InformeAsesoriaResponse;
import com.example.NoMasAccidentes.service.asesoria.InformeAsesoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Informe de la asesoría (RF15 para asesorías, RF25). La descarga del PDF se
 * sirve por el endpoint genérico {@code /api/informes/{id}/descarga}.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Informes de asesoría", description = "Informe posterior a la asesoría (RF15, RF25)")
public class InformeAsesoriaController {

    private final InformeAsesoriaService informeAsesoriaService;

    @Operation(summary = "Generar el informe PDF de una asesoría atendida (RF15, RF25)")
    @PostMapping("/api/asesorias/{idAsesoria}/informe")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ResponseEntity<InformeAsesoriaResponse> generar(@PathVariable Long idAsesoria) {
        InformeAsesoriaResponse informe = informeAsesoriaService.generarParaAsesoria(idAsesoria);
        return ResponseEntity
                .created(URI.create("/api/informes/" + informe.id()))
                .body(informe);
    }

    @Operation(summary = "Obtener el informe de una asesoría")
    @GetMapping("/api/asesorias/{idAsesoria}/informe")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public InformeAsesoriaResponse obtenerPorAsesoria(@PathVariable Long idAsesoria) {
        return informeAsesoriaService.obtenerPorAsesoria(idAsesoria);
    }
}
