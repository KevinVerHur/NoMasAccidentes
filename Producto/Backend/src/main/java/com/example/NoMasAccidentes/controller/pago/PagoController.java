package com.example.NoMasAccidentes.controller.pago;

import com.example.NoMasAccidentes.dto.pago.CobroExtraResponse;
import com.example.NoMasAccidentes.dto.pago.CrearCobroExtraRequest;
import com.example.NoMasAccidentes.dto.pago.PagoResponse;
import com.example.NoMasAccidentes.dto.pago.RegistrarPagoRequest;
import com.example.NoMasAccidentes.service.pago.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * Pagos, historial, morosidad y suspensión (RF09–RF12).
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Registro de pagos, morosidad y suspensión (RF09–RF12)")
public class PagoController {

    private final PagoService pagoService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public PagoResponse obtener(@PathVariable Long id) {
        return pagoService.obtenerPorId(id);
    }

    @Operation(summary = "Historial de pagos de un cliente (RF10)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<PagoResponse> historial(@RequestParam Long idCliente) {
        return pagoService.historialPorCliente(idCliente);
    }

    @Operation(summary = "Historial de pagos del cliente autenticado (portal cliente, RF10)")
    @GetMapping("/mis-pagos")
    @PreAuthorize("hasRole('CLIENTE')")
    public List<PagoResponse> misPagos(Principal principal) {
        return pagoService.misPagos(principal.getName());
    }

    @Operation(summary = "Registrar el pago de una cuota (RF09)")
    @PatchMapping("/{id}/registrar")
    @PreAuthorize("hasRole('ADMIN')")
    public PagoResponse registrar(@PathVariable Long id, @Valid @RequestBody RegistrarPagoRequest request) {
        return pagoService.registrar(id, request);
    }

    @Operation(summary = "Evaluar morosidad: marca cuotas vencidas como ATRASADO (RF11)")
    @PostMapping("/evaluar-morosidad")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> evaluarMorosidad() {
        return Map.of("cuotasMarcadas", pagoService.evaluarMorosidad());
    }

    @Operation(summary = "Suspender clientes con pagos atrasados (RF12)")
    @PostMapping("/suspender-morosos")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> suspenderMorosos() {
        return Map.of("clientesSuspendidos", pagoService.suspenderMorosos());
    }

    @Operation(summary = "Agregar un cobro extra a una cuota (RF21, RF24, RF28)")
    @PostMapping("/{id}/cobros-extra")
    @PreAuthorize("hasRole('ADMIN')")
    public CobroExtraResponse agregarCobroExtra(@PathVariable Long id, @Valid @RequestBody CrearCobroExtraRequest request) {
        return pagoService.agregarCobroExtra(id, request);
    }

    @GetMapping("/{id}/cobros-extra")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<CobroExtraResponse> listarCobrosExtra(@PathVariable Long id) {
        return pagoService.listarCobrosExtra(id);
    }
}
