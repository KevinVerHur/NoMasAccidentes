package com.example.NoMasAccidentes.controller.asesoria;

import com.example.NoMasAccidentes.dto.asesoria.AsesoriaResponse;
import com.example.NoMasAccidentes.dto.asesoria.CrearAsesoriaRequest;
import com.example.NoMasAccidentes.service.asesoria.AsesoriaService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestión de asesorías por accidentes o fiscalizaciones (RF22–RF25).
 */
@RestController
@RequestMapping("/api/asesorias")
@RequiredArgsConstructor
@Tag(name = "Asesorías", description = "Gestión de asesorías por accidentes o fiscalizaciones (RF22–RF25)")
public class AsesoriaController {

    private final AsesoriaService asesoriaService;

    @Operation(summary = "Registrar una asesoría (RF22); marca extra si supera el límite anual (RF23, RF24)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AsesoriaResponse> crear(@Valid @RequestBody CrearAsesoriaRequest request) {
        AsesoriaResponse creada = asesoriaService.crear(request);
        return ResponseEntity.created(URI.create("/api/asesorias/" + creada.id())).body(creada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public Page<AsesoriaResponse> listar(
            @RequestParam(required = false) Long idEmpresa,
            Pageable pageable) {
        return idEmpresa != null
                ? asesoriaService.listarPorEmpresa(idEmpresa, pageable)
                : asesoriaService.listar(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public AsesoriaResponse obtener(@PathVariable Long id) {
        return asesoriaService.obtenerPorId(id);
    }

    @Operation(summary = "Asesorías asignadas al profesional autenticado")
    @GetMapping("/me")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public List<AsesoriaResponse> misAsignaciones(Principal principal) {
        return asesoriaService.misAsignaciones(principal.getName());
    }

    @Operation(summary = "Asesorías de la empresa del cliente autenticado (solo lectura, RF07)")
    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<AsesoriaResponse> misAsesorias(Principal principal) {
        return asesoriaService.misAsesorias(principal.getName());
    }

    @Operation(summary = "El profesional inicia la atención de la asesoría (RF25)")
    @PatchMapping("/{id}/atender")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public AsesoriaResponse atender(@PathVariable Long id) {
        return asesoriaService.atender(id);
    }

    @Operation(summary = "Cerrar una asesoría completada")
    @PatchMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public AsesoriaResponse cerrar(@PathVariable Long id) {
        return asesoriaService.cerrar(id);
    }

    @Operation(summary = "Cancelar una asesoría")
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public AsesoriaResponse cancelar(@PathVariable Long id) {
        return asesoriaService.cancelar(id);
    }
}
