package com.example.NoMasAccidentes.controller.pago;

import com.example.NoMasAccidentes.dto.pago.CrearPlanPagoRequest;
import com.example.NoMasAccidentes.dto.pago.PlanPagoResponse;
import com.example.NoMasAccidentes.service.pago.PlanPagoService;
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
 * Planes de pago por cliente (RF08).
 */
@RestController
@RequestMapping("/api/planes-pago")
@RequiredArgsConstructor
@Tag(name = "Planes de pago", description = "Asignación de planes y generación de cuotas (RF08)")
public class PlanPagoController {

    private final PlanPagoService planPagoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlanPagoResponse> crear(@Valid @RequestBody CrearPlanPagoRequest request) {
        PlanPagoResponse creado = planPagoService.crear(request);
        return ResponseEntity.created(URI.create("/api/planes-pago/" + creado.id())).body(creado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<PlanPagoResponse> listarPorCliente(@RequestParam Long idCliente) {
        return planPagoService.listarPorCliente(idCliente);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public PlanPagoResponse obtener(@PathVariable Long id) {
        return planPagoService.obtenerPorId(id);
    }
}
