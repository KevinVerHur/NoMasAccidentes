package com.example.NoMasAccidentes.controller.checklist;

import com.example.NoMasAccidentes.dto.checklist.ActualizarListaChequeoRequest;
import com.example.NoMasAccidentes.dto.checklist.CrearListaChequeoRequest;
import com.example.NoMasAccidentes.dto.checklist.ListaChequeoResponse;
import com.example.NoMasAccidentes.service.checklist.ListaChequeoService;
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
 * Gestion de listas de chequeo asociadas a clientes.
 * RF16/RF17.
 */
@RestController
@RequestMapping("/api/listas-chequeo")
@RequiredArgsConstructor
@Tag(name = "Listas de chequeo", description = "Gestion de listas de chequeo por cliente (RF16/RF17)")
public class ListaChequeoController {

    private final ListaChequeoService listaChequeoService;

    @Operation(
            summary = "Crear lista de chequeo",
            description = "Crea una lista de chequeo asociada a un cliente. Solo accesible por administradores."
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ListaChequeoResponse> crear(
            @Valid @RequestBody CrearListaChequeoRequest request
    ) {
        ListaChequeoResponse creada = listaChequeoService.crear(request);
        return ResponseEntity
                .created(URI.create("/api/listas-chequeo/" + creada.id()))
                .body(creada);
    }

    @Operation(
            summary = "Listar listas de chequeo por cliente",
            description = "Retorna las listas de chequeo asociadas a un cliente."
    )
    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<ListaChequeoResponse> listarPorCliente(@PathVariable Long idCliente) {
        return listaChequeoService.listarPorCliente(idCliente);
    }

    @Operation(
            summary = "Obtener lista de chequeo por ID",
            description = "Retorna una lista de chequeo especifica."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ListaChequeoResponse obtener(@PathVariable Long id) {
        return listaChequeoService.obtenerPorId(id);
    }

    @Operation(
            summary = "Actualizar lista de chequeo",
            description = "Actualiza una lista de chequeo. Maximo 2 modificaciones por anio."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ListaChequeoResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarListaChequeoRequest request
    ) {
        return listaChequeoService.actualizar(id, request);
    }

    @Operation(
            summary = "Eliminar lista de chequeo",
            description = "Elimina logicamente una lista de chequeo."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        listaChequeoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}