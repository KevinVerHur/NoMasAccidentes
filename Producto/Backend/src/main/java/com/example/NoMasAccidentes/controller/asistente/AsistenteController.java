package com.example.NoMasAccidentes.controller.asistente;

import com.example.NoMasAccidentes.dto.asistente.AsistenteRequest;
import com.example.NoMasAccidentes.dto.asistente.AsistenteResponse;
import com.example.NoMasAccidentes.service.asistente.AsistenteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints para gestión de asistentes (trabajadores del cliente).
 *
 * GET    /api/asistentes/cliente/{idCliente}  → listar trabajadores del cliente
 * GET    /api/asistentes/{id}                 → obtener uno
 * POST   /api/asistentes                      → crear
 * PUT    /api/asistentes/{id}                 → editar
 * DELETE /api/asistentes/{id}                 → eliminar (soft delete)
 */
@RestController
@RequestMapping("/api/asistentes")
@RequiredArgsConstructor
@Tag(name = "Asistentes", description = "Trabajadores del cliente que asisten a capacitaciones")
public class AsistenteController {

    private final AsistenteService asistenteService;

    @Operation(summary = "Listar trabajadores de una empresa")
    @GetMapping("/empresa/{idEmpresa}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL', 'CLIENTE')")
    public List<AsistenteResponse> listarPorEmpresa(@PathVariable Long idEmpresa) {
        return asistenteService.listarPorEmpresa(idEmpresa);
    }

    @Operation(summary = "Obtener asistente por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL', 'CLIENTE')")
    public AsistenteResponse obtener(@PathVariable Long id) {
        return asistenteService.obtener(id);
    }

    @Operation(summary = "Crear asistente (trabajador del cliente)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<AsistenteResponse> crear(
            @Valid @RequestBody AsistenteRequest request) {
        AsistenteResponse creado = asistenteService.crear(request);
        return ResponseEntity
                .created(URI.create("/api/asistentes/" + creado.id()))
                .body(creado);
    }

    @Operation(summary = "Editar asistente")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public AsistenteResponse editar(
            @PathVariable Long id,
            @Valid @RequestBody AsistenteRequest request) {
        return asistenteService.editar(id, request);
    }

    @Operation(summary = "Eliminar asistente")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        asistenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
