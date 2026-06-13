package com.example.NoMasAccidentes.controller.profesional;

import com.example.NoMasAccidentes.dto.profesional.ActualizarEstadoProfesionalRequest;
import com.example.NoMasAccidentes.dto.profesional.ActualizarProfesionalRequest;
import com.example.NoMasAccidentes.dto.profesional.ActualizarUbicacionRequest;
import com.example.NoMasAccidentes.dto.profesional.ProfesionalResponse;
import com.example.NoMasAccidentes.dto.profesional.RegistrarProfesionalRequest;
import com.example.NoMasAccidentes.service.profesional.ProfesionalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profesionales")
@RequiredArgsConstructor
@Tag(name = "Profesionales", description = "Gestion de profesionales de prevencion de riesgos (RF03)")
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfesionalResponse> crear(@Valid @RequestBody RegistrarProfesionalRequest request) {
        ProfesionalResponse creado = profesionalService.crear(request);
        return ResponseEntity.created(URI.create("/api/profesionales/" + creado.id())).body(creado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public Page<ProfesionalResponse> listar(Pageable pageable) {
        return profesionalService.listar(pageable);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public ProfesionalResponse obtenerMiPerfil(Authentication authentication) {
        return profesionalService.obtenerMiPerfil(authentication.getName());
    }

    @PatchMapping("/me/estado")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public ProfesionalResponse actualizarMiEstado(
            Authentication authentication,
            @Valid @RequestBody ActualizarEstadoProfesionalRequest request
    ) {
        return profesionalService.actualizarMiEstado(authentication.getName(), request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ProfesionalResponse obtener(@PathVariable Long id) {
        return profesionalService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProfesionalResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarProfesionalRequest request
    ) {
        return profesionalService.actualizar(id, request);
    }

    @PatchMapping("/{id}/ubicacion")
    @PreAuthorize("hasRole('ADMIN')")
    public ProfesionalResponse actualizarUbicacion(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUbicacionRequest request
    ) {
        return profesionalService.actualizarUbicacion(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        profesionalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ProfesionalResponse actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoProfesionalRequest request
    ) {
        return profesionalService.actualizarEstado(id, request);
    }
}