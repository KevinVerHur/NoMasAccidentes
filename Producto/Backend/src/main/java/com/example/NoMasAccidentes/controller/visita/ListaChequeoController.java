package com.example.NoMasAccidentes.controller.visita;

import com.example.NoMasAccidentes.dto.visita.ActualizarListaChequeoRequest;
import com.example.NoMasAccidentes.dto.visita.CrearListaChequeoRequest;
import com.example.NoMasAccidentes.dto.visita.ListaChequeoResponse;
import com.example.NoMasAccidentes.service.visita.ListaChequeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestión de listas de chequeo por cliente (RF16–RF17).
 */
@RestController
@RequestMapping("/api/listas-chequeo")
@RequiredArgsConstructor
@Tag(name = "Listas de chequeo", description = "Listas de chequeo por cliente (RF16–RF17)")
public class ListaChequeoController {

    private final ListaChequeoService listaChequeoService;

    @Operation(summary = "Crear la lista de chequeo de un cliente (RF16)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListaChequeoResponse> crear(@Valid @RequestBody CrearListaChequeoRequest request) {
        ListaChequeoResponse creada = listaChequeoService.crear(request);
        return ResponseEntity.created(URI.create("/api/listas-chequeo/" + creada.id())).body(creada);
    }

    @Operation(summary = "Modificar la lista de chequeo (RF17: máximo 2 veces al año)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ListaChequeoResponse modificar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarListaChequeoRequest request) {
        return listaChequeoService.modificar(id, request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ListaChequeoResponse obtener(@PathVariable Long id) {
        return listaChequeoService.obtenerPorId(id);
    }

    @Operation(summary = "Obtener la lista de chequeo de una empresa")
    @GetMapping("/empresa/{idEmpresa}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ListaChequeoResponse obtenerPorEmpresa(@PathVariable Long idEmpresa) {
        return listaChequeoService.obtenerPorEmpresa(idEmpresa);
    }
}
