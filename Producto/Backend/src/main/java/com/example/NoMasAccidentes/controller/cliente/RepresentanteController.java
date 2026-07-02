package com.example.NoMasAccidentes.controller.cliente;

import com.example.NoMasAccidentes.dto.cliente.CrearRepresentanteRequest;
import com.example.NoMasAccidentes.dto.cliente.RepresentanteResponse;
import com.example.NoMasAccidentes.service.cliente.RepresentanteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Representantes (contactos) de una empresa (RF06). Solo ADMIN.
 */
@RestController
@RequestMapping("/api/empresas/{idEmpresa}/representantes")
@RequiredArgsConstructor
@Tag(name = "Representantes", description = "Contactos de una empresa y su acceso al portal (RF06)")
public class RepresentanteController {

    private final RepresentanteService representanteService;

    @Operation(summary = "Listar los representantes de una empresa")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<RepresentanteResponse> listar(@PathVariable Long idEmpresa) {
        return representanteService.listarPorEmpresa(idEmpresa);
    }

    @Operation(summary = "Agregar un representante a la empresa (con o sin acceso al portal)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RepresentanteResponse> crear(
            @PathVariable Long idEmpresa,
            @Valid @RequestBody CrearRepresentanteRequest request) {
        RepresentanteResponse creado = representanteService.crear(idEmpresa, request);
        return ResponseEntity
                .created(URI.create("/api/empresas/" + idEmpresa + "/representantes/" + creado.id()))
                .body(creado);
    }

    @Operation(summary = "Eliminar un representante (soft delete)")
    @DeleteMapping("/{idRepresentante}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long idEmpresa,
            @PathVariable Long idRepresentante) {
        representanteService.eliminar(idRepresentante);
        return ResponseEntity.noContent().build();
    }
}
