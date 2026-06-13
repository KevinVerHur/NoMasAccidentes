package com.example.NoMasAccidentes.controller.pago;

import com.example.NoMasAccidentes.dto.pago.CrearMensualidadRequest;
import com.example.NoMasAccidentes.dto.pago.MensualidadResponse;
import com.example.NoMasAccidentes.service.pago.MensualidadService;
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
 * Catálogo de planes de pago / mensualidades (RF08).
 */
@RestController
@RequestMapping("/api/mensualidades")
@RequiredArgsConstructor
@Tag(name = "Mensualidades", description = "Catálogo de planes de pago (RF08)")
public class MensualidadController {

    private final MensualidadService mensualidadService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensualidadResponse> crear(@Valid @RequestBody CrearMensualidadRequest request) {
        MensualidadResponse creada = mensualidadService.crear(request);
        return ResponseEntity.created(URI.create("/api/mensualidades/" + creada.id())).body(creada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<MensualidadResponse> listar() {
        return mensualidadService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public MensualidadResponse obtener(@PathVariable Long id) {
        return mensualidadService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mensualidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
