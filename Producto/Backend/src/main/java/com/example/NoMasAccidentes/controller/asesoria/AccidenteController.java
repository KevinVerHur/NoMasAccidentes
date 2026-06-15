package com.example.NoMasAccidentes.controller.asesoria;

import com.example.NoMasAccidentes.dto.asesoria.AccidenteResponse;
import com.example.NoMasAccidentes.dto.asesoria.CrearAccidenteRequest;
import com.example.NoMasAccidentes.service.asesoria.AccidenteService;
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
 * Registro de accidentes laborales de una asesoría (RF22).
 */
@RestController
@RequestMapping("/api/accidentes")
@RequiredArgsConstructor
@Tag(name = "Accidentes", description = "Accidentes laborales asociados a una asesoría (RF22, RF35)")
public class AccidenteController {

    private final AccidenteService accidenteService;

    @Operation(summary = "Registrar un accidente laboral de una asesoría (RF22)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ResponseEntity<AccidenteResponse> crear(@Valid @RequestBody CrearAccidenteRequest request) {
        AccidenteResponse creado = accidenteService.crear(request);
        return ResponseEntity.created(URI.create("/api/accidentes/" + creado.id())).body(creado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<AccidenteResponse> listarPorAsesoria(@RequestParam Long idAsesoria) {
        return accidenteService.listarPorAsesoria(idAsesoria);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public AccidenteResponse obtener(@PathVariable Long id) {
        return accidenteService.obtenerPorId(id);
    }
}
