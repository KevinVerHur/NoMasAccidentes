package com.example.NoMasAccidentes.controller.profesional;

import com.example.NoMasAccidentes.dto.profesional.ActualizarProfesionalRequest;
import com.example.NoMasAccidentes.dto.profesional.ActualizarUbicacionRequest;
import com.example.NoMasAccidentes.dto.profesional.CrearProfesionalRequest;
import com.example.NoMasAccidentes.dto.profesional.ProfesionalResponse;
import com.example.NoMasAccidentes.service.profesional.ProfesionalService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD de profesionales de prevención de riesgos.
 */
@RestController
@RequestMapping("/api/profesionales")
@RequiredArgsConstructor
@Tag(name = "Profesionales", description = "Gestión de profesionales de prevención de riesgos (RF03)")
public class ProfesionalController {

    private final ProfesionalService profesionalService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfesionalResponse> crear(@Valid @RequestBody CrearProfesionalRequest request) {
        ProfesionalResponse creado = profesionalService.crear(request);
        return ResponseEntity
            .created(URI.create("/api/profesionales/" + creado.id()))
            .body(creado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public Page<ProfesionalResponse> listar(Pageable pageable) {
        return profesionalService.listar(pageable);
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
            @Valid @RequestBody ActualizarProfesionalRequest request) {
        return profesionalService.actualizar(id, request);
    }

    @PatchMapping("/{id}/ubicacion")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ProfesionalResponse actualizarUbicacion(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUbicacionRequest request) {
        return profesionalService.actualizarUbicacion(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        profesionalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
