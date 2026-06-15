package com.example.NoMasAccidentes.controller.asesoria;

import com.example.NoMasAccidentes.dto.asesoria.CrearFiscalizacionRequest;
import com.example.NoMasAccidentes.dto.asesoria.FiscalizacionResponse;
import com.example.NoMasAccidentes.service.asesoria.FiscalizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro de fiscalizaciones de una asesoría (RF22, RF42-44).
 */
@RestController
@RequestMapping("/api/fiscalizaciones")
@RequiredArgsConstructor
@Tag(name = "Fiscalizaciones", description = "Fiscalizaciones asociadas a una asesoría (RF22, RF42-44)")
public class FiscalizacionController {

    private final FiscalizacionService fiscalizacionService;

    @Operation(summary = "Registrar una fiscalización de una asesoría (RF22)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ResponseEntity<FiscalizacionResponse> crear(@Valid @RequestBody CrearFiscalizacionRequest request) {
        FiscalizacionResponse creada = fiscalizacionService.crear(request);
        return ResponseEntity.created(URI.create("/api/fiscalizaciones/" + creada.id())).body(creada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<FiscalizacionResponse> listarPorAsesoria(@RequestParam Long idAsesoria) {
        return fiscalizacionService.listarPorAsesoria(idAsesoria);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public FiscalizacionResponse obtener(@PathVariable Long id) {
        return fiscalizacionService.obtenerPorId(id);
    }
}
