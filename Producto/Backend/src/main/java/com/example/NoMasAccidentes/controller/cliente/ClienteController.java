package com.example.NoMasAccidentes.controller.cliente;

import com.example.NoMasAccidentes.dto.cliente.ActualizarClienteRequest;
import com.example.NoMasAccidentes.dto.cliente.ClienteResponse;
import com.example.NoMasAccidentes.dto.cliente.CrearClienteRequest;
import com.example.NoMasAccidentes.service.cliente.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRUD de clientes del sistema (RF06–RF12).
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de empresas clientes (RF06–RF12)")
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(
            summary = "Crear un nuevo cliente",
            description = "Registra una nueva empresa cliente en el sistema. Solo accesible por administradores."
    )
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody CrearClienteRequest request) {
        ClienteResponse creado = clienteService.crear(request);
        return ResponseEntity.created(URI.create("/api/clientes/" + creado.id())).body(creado);
    }

    @Operation(
            summary = "Listar clientes paginados ",
            description = "Retorna una página de clientes registrados. Accesible por administradores y profesionales."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public Page<ClienteResponse> listar(Pageable pageable) {
        return clienteService.listar(pageable);
    }

    @Operation(
            summary = "Obtener cliente por ID",
            description = "Retorna los datos de un cliente específico por su identificador. Accesible por administradores y profesionales."
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public ClienteResponse obtener(@PathVariable Long id) {
        return clienteService.obtenerPorId(id);
    }

    @Operation(
            summary = "Actualizar cliente completo",
            description = "Reemplaza todos los datos de un cliente existente. Solo accesible por administradores."
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarClienteRequest request) {
        return clienteService.actualizar(id, request);
    }

    @Operation(
            summary = "Suspender servicio del cliente ",
            description = "Suspende el acceso al servicio de un cliente activo. Solo accesible por administradores."
    )
    @PatchMapping("/{id}/suspender")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse suspender(@PathVariable Long id) {
        return clienteService.suspender(id);
    }

    @Operation(
            summary = "Reactivar cliente suspendido ",
            description = "Reactiva el acceso al servicio de un cliente previamente suspendido. Solo accesible por administradores."
    )
    @PatchMapping("/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ClienteResponse reactivar(@PathVariable Long id) {
        return clienteService.reactivar(id);
    }

    @Operation(
            summary = "Eliminar cliente ",
            description = "Elimina permanentemente un cliente del sistema. Solo accesible por administradores."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}