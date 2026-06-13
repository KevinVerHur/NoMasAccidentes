package com.example.NoMasAccidentes.controller.asesoria;

import com.example.NoMasAccidentes.dto.asesoria.CrearMultaRequest;
import com.example.NoMasAccidentes.dto.asesoria.MultaResponse;
import com.example.NoMasAccidentes.model.asesoria.EstadoMulta;
import com.example.NoMasAccidentes.service.asesoria.MultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro y seguimiento de multas derivadas de una fiscalización (RF42-44).
 */
@RestController
@RequestMapping("/api/multas")
@RequiredArgsConstructor
@Tag(name = "Multas", description = "Multas derivadas de fiscalizaciones (RF42-44)")
public class MultaController {

    private final MultaService multaService;

    @Operation(summary = "Registrar una multa de una fiscalización (RF42-44)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MultaResponse> crear(@Valid @RequestBody CrearMultaRequest request) {
        MultaResponse creada = multaService.crear(request);
        return ResponseEntity.created(URI.create("/api/multas/" + creada.id())).body(creada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<MultaResponse> listarPorFiscalizacion(@RequestParam Long idFiscalizacion) {
        return multaService.listarPorFiscalizacion(idFiscalizacion);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public MultaResponse obtener(@PathVariable Long id) {
        return multaService.obtenerPorId(id);
    }

    @Operation(summary = "Actualizar el estado de pago de la multa (RF42-44)")
    @PatchMapping("/{id}/estado-pago")
    @PreAuthorize("hasRole('ADMIN')")
    public MultaResponse actualizarEstadoPago(
            @PathVariable Long id,
            @RequestParam EstadoMulta estado) {
        return multaService.actualizarEstadoPago(id, estado);
    }
}
