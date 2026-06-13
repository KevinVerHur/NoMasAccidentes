package com.example.NoMasAccidentes.controller.asesoria;

import com.example.NoMasAccidentes.dto.asesoria.CrearPropuestaMejoraRequest;
import com.example.NoMasAccidentes.dto.asesoria.PropuestaMejoraResponse;
import com.example.NoMasAccidentes.model.asesoria.EstadoPropuesta;
import com.example.NoMasAccidentes.service.asesoria.PropuestaMejoraService;
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
 * Propuestas de mejora del informe de una asesoría (RF25).
 */
@RestController
@RequestMapping("/api/propuestas-mejora")
@RequiredArgsConstructor
@Tag(name = "Propuestas de mejora", description = "Acciones de mejora del informe de asesoría (RF25)")
public class PropuestaMejoraController {

    private final PropuestaMejoraService propuestaService;

    @Operation(summary = "Registrar una propuesta de mejora del informe (RF25)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ResponseEntity<PropuestaMejoraResponse> crear(@Valid @RequestBody CrearPropuestaMejoraRequest request) {
        PropuestaMejoraResponse creada = propuestaService.crear(request);
        return ResponseEntity.created(URI.create("/api/propuestas-mejora/" + creada.id())).body(creada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<PropuestaMejoraResponse> listarPorInforme(@RequestParam Long idInforme) {
        return propuestaService.listarPorInforme(idInforme);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public PropuestaMejoraResponse obtener(@PathVariable Long id) {
        return propuestaService.obtenerPorId(id);
    }

    @Operation(summary = "Actualizar el estado de una propuesta de mejora (RF25)")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public PropuestaMejoraResponse actualizarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPropuesta estado) {
        return propuestaService.actualizarEstado(id, estado);
    }
}
