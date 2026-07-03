package com.example.NoMasAccidentes.controller.empresa;

import com.example.NoMasAccidentes.dto.empresa.ActualizarEmpresaRequest;
import com.example.NoMasAccidentes.dto.empresa.CrearEmpresaRequest;
import com.example.NoMasAccidentes.dto.empresa.EmpresaResponse;
import com.example.NoMasAccidentes.service.empresa.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
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
 * CRUD de empresas cliente (RF06–RF12). El alta crea también el primer
 * representante con acceso al portal.
 */
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "Gestión de empresas clientes (RF06–RF12)")
public class EmpresaController {

    private final EmpresaService empresaService;

    @Operation(summary = "Crear una nueva empresa cliente",
            description = "Registra una empresa y su primer representante. Solo ADMIN.")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpresaResponse> crear(@Valid @RequestBody CrearEmpresaRequest request) {
        EmpresaResponse creada = empresaService.crear(request);
        return ResponseEntity.created(URI.create("/api/empresas/" + creada.id())).body(creada);
    }

    @Operation(summary = "Listar empresas paginadas")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public Page<EmpresaResponse> listar(Pageable pageable) {
        return empresaService.listar(pageable);
    }

    @Operation(summary = "Obtener la empresa del usuario autenticado (portal cliente)")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public EmpresaResponse me(Principal principal) {
        return empresaService.empresaPorEmail(principal.getName());
    }

    @Operation(summary = "Obtener empresa por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public EmpresaResponse obtener(@PathVariable Long id) {
        return empresaService.obtenerPorId(id);
    }

    @Operation(summary = "Actualizar empresa completa")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public EmpresaResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEmpresaRequest request) {
        return empresaService.actualizar(id, request);
    }

    @Operation(summary = "Suspender servicio de la empresa")
    @PatchMapping("/{id}/suspender")
    @PreAuthorize("hasRole('ADMIN')")
    public EmpresaResponse suspender(@PathVariable Long id) {
        return empresaService.suspender(id);
    }

    @Operation(summary = "Reactivar empresa suspendida")
    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public EmpresaResponse reactivar(@PathVariable Long id) {
        return empresaService.reactivar(id);
    }

    @Operation(summary = "Eliminar empresa (soft delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        empresaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
