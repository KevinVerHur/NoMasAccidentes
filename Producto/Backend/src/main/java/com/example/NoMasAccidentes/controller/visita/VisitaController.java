package com.example.NoMasAccidentes.controller.visita;

import com.example.NoMasAccidentes.dto.visita.CrearVisitaRequest;
import com.example.NoMasAccidentes.dto.visita.VisitaResponse;
import com.example.NoMasAccidentes.service.visita.VisitaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visitas")
@RequiredArgsConstructor
public class VisitaController {

    private final VisitaService visitaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<VisitaResponse> listar(Pageable pageable) {
        return visitaService.listar(pageable);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public List<VisitaResponse> listarMisVisitas(Authentication authentication) {
        return visitaService.listarMisVisitas(authentication.getName());
    }

    @GetMapping("/me/programadas")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public List<VisitaResponse> listarMisVisitasProgramadas(Authentication authentication) {
        return visitaService.listarMisVisitasProgramadas(authentication.getName());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VisitaResponse> crear(@Valid @RequestBody CrearVisitaRequest request) {
        VisitaResponse creada = visitaService.crear(request);

        return ResponseEntity
                .created(URI.create("/api/visitas/" + creada.id()))
                .body(creada);
    }

    @PatchMapping("/{id}/iniciar")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public VisitaResponse iniciarMiVisita(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return visitaService.iniciarMiVisita(authentication.getName(), id);
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public VisitaResponse finalizarMiVisita(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return visitaService.finalizarMiVisita(authentication.getName(), id);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public VisitaResponse cancelar(@PathVariable Long id) {
        return visitaService.cancelar(id);
    }
}