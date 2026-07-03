package com.example.NoMasAccidentes.controller.solicitud;

import com.example.NoMasAccidentes.dto.solicitud.AprobarSolicitudRequest;
import com.example.NoMasAccidentes.dto.solicitud.CrearSolicitudRequest;
import com.example.NoMasAccidentes.dto.solicitud.RechazarSolicitudRequest;
import com.example.NoMasAccidentes.dto.solicitud.SolicitudResponse;
import com.example.NoMasAccidentes.model.solicitud.EstadoSolicitud;
import com.example.NoMasAccidentes.service.solicitud.SolicitudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
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
 * Solicitudes de servicio del cliente (mejora web). El cliente crea/consulta las
 * suyas; el admin lista, aprueba (creando el recurso) o rechaza.
 */
@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Solicitudes de servicio del cliente y su gestión")
public class SolicitudController {

    private final SolicitudService service;

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<SolicitudResponse> crear(
            @Valid @RequestBody CrearSolicitudRequest request, Principal principal) {
        SolicitudResponse creada = service.crear(request, principal.getName());
        return ResponseEntity.created(URI.create("/api/solicitudes/" + creada.id())).body(creada);
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<SolicitudResponse> mias(Principal principal) {
        return service.misSolicitudes(principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SolicitudResponse> listar(@RequestParam(required = false) EstadoSolicitud estado) {
        return service.listar(estado);
    }

    @GetMapping("/pendientes/count")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Long> contarPendientes() {
        return Map.of("pendientes", service.contarPendientes());
    }

    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public SolicitudResponse aprobar(@PathVariable Long id, @Valid @RequestBody AprobarSolicitudRequest request) {
        return service.aprobar(id, request);
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public SolicitudResponse rechazar(@PathVariable Long id, @Valid @RequestBody RechazarSolicitudRequest request) {
        return service.rechazar(id, request);
    }
}
