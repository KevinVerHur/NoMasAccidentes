package com.example.NoMasAccidentes.controller.visita;

import com.example.NoMasAccidentes.dto.visita.PlanificarVisitaRequest;
import com.example.NoMasAccidentes.dto.visita.RegistrarVisitaRequest;
import com.example.NoMasAccidentes.dto.visita.VisitaResponse;
import com.example.NoMasAccidentes.service.visita.VisitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestión de visitas a clientes (RF13–RF14).
 */
@RestController
@RequestMapping("/api/visitas")
@RequiredArgsConstructor
@Tag(name = "Visitas", description = "Planificación y registro de visitas (RF13–RF14)")
public class VisitaController {

    private final VisitaService visitaService;

    @Operation(summary = "Planificar una visita (RF13)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VisitaResponse> planificar(@Valid @RequestBody PlanificarVisitaRequest request) {
        VisitaResponse creada = visitaService.planificar(request);
        return ResponseEntity.created(URI.create("/api/visitas/" + creada.id())).body(creada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public Page<VisitaResponse> listar(
            @RequestParam(required = false) Long idCliente,
            Pageable pageable) {
        return idCliente != null
                ? visitaService.listarPorCliente(idCliente, pageable)
                : visitaService.listar(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public VisitaResponse obtener(@PathVariable Long id) {
        return visitaService.obtenerPorId(id);
    }

    @Operation(summary = "Visitas asignadas al profesional autenticado")
    @GetMapping("/me")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public List<VisitaResponse> misAsignaciones(Principal principal) {
        return visitaService.misAsignaciones(principal.getName());
    }

    @Operation(summary = "Visitas del cliente autenticado (portal cliente)")
    @GetMapping("/mis-visitas")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<VisitaResponse> misVisitas(Principal principal) {
        return visitaService.misVisitas(principal.getName());
    }

    @Operation(summary = "Iniciar la visita en terreno: pasa a EN_CURSO (RF04, RF14)")
    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public VisitaResponse iniciar(@PathVariable Long id) {
        return visitaService.iniciar(id);
    }

    @Operation(summary = "Registrar la visita realizada en terreno (RF14)")
    @PatchMapping("/{id}/registrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public VisitaResponse registrar(
            @PathVariable Long id,
            @Valid @RequestBody RegistrarVisitaRequest request) {
        return visitaService.registrar(id, request);
    }

    @Operation(summary = "Cancelar una visita")
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public VisitaResponse cancelar(@PathVariable Long id) {
        return visitaService.cancelar(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        visitaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
