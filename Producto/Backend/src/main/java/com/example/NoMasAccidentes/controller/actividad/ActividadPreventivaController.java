package com.example.NoMasAccidentes.controller.actividad;

import com.example.NoMasAccidentes.dto.actividad.*;
import com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva;
import com.example.NoMasAccidentes.service.actividad.ActividadPreventivaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/actividades-preventivas")
@RequiredArgsConstructor
@Tag(name = "Actividades preventivas", description = "Seguimiento de actividades preventivas")
public class ActividadPreventivaController {
    

    private final ActividadPreventivaService service; 

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActividadPreventivaResponse> crear(@Valid @RequestBody CrearActividadPreventivaRequest request) {
        ActividadPreventivaResponse creada = service.crear(request);
        return ResponseEntity.created(URI.create("/api/actividades-preventivas/" + creada.id())).body(creada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public Page<ActividadPreventivaResponse> listar(
            @RequestParam(required = false) Long idCliente,
            @RequestParam(required = false) EstadoActividadPreventiva estado,
            Pageable pageable){
        return service.listar(idCliente, estado, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ActividadPreventivaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ActividadPreventivaResponse actualizar(
            @PathVariable Long id, 
            @Valid @RequestBody ActualizarActividadPreventivaRequest request){
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRol('ADMIN', 'PROFESIONAL')")
    public ActividadPreventivaResponse cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoActividadRequest request){
        return service.cambiarEstado(id, request);
    }

    @GetMapping("/mis-actividades")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<ActividadPreventivaResponse> misActividades(Principal principal){
        return service.misActividades(principal.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
