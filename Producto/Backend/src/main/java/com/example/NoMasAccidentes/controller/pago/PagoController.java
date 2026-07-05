package com.example.NoMasAccidentes.controller.pago;

import com.example.NoMasAccidentes.dto.pago.CobroExtraResponse;
import com.example.NoMasAccidentes.dto.pago.CrearCobroExtraRequest;
import com.example.NoMasAccidentes.dto.pago.IniciarWebpayResponse;
import com.example.NoMasAccidentes.dto.pago.PagoResponse;
import com.example.NoMasAccidentes.dto.pago.RegistrarPagoRequest;
import com.example.NoMasAccidentes.service.pago.PagoService;
import com.example.NoMasAccidentes.service.pago.WebpayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    private final WebpayService webpayService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public PagoResponse obtener(@PathVariable Long id) {
        return pagoService.obtenerPorId(id);
    }

    @Operation(summary = "Historial de pagos de una empresa (RF10)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESIONAL')")
    public List<PagoResponse> historial(@RequestParam Long idEmpresa) {
        return pagoService.historialPorEmpresa(idEmpresa);
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
        int marcadas = pagoService.evaluarMorosidad();
        return Map.of(
                "cuotasMarcadas", marcadas,
                "totalAtrasadas", pagoService.contarAtrasadas());
    }

    @Operation(summary = "Suspender empresas con pagos atrasados (RF12)")
    @PostMapping("/suspender-morosos")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> suspenderMorosos() {
        return Map.of("empresasSuspendidas", pagoService.suspenderMorosos());
    }

    @Operation(summary = "Evaluar la morosidad de una empresa: marca sus cuotas vencidas como ATRASADO (RF11)")
    @PostMapping("/empresa/{idEmpresa}/evaluar-morosidad")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Integer> evaluarMorosidadEmpresa(@PathVariable Long idEmpresa) {
        int marcadas = pagoService.evaluarMorosidadEmpresa(idEmpresa);
        return Map.of(
                "cuotasMarcadas", marcadas,
                "totalAtrasadas", pagoService.contarAtrasadasEmpresa(idEmpresa));
    }

    @Operation(summary = "Suspender una empresa por pagos atrasados (RF12)")
    @PostMapping("/empresa/{idEmpresa}/suspender")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Boolean> suspenderMorosoEmpresa(@PathVariable Long idEmpresa) {
        return Map.of("suspendida", pagoService.suspenderMorosoEmpresa(idEmpresa));
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

    @GetMapping("/{id}/mis-cobros-extra")
@PreAuthorize("hasRole('CLIENTE')")
public List<CobroExtraResponse> listarMisCobrosExtra(@PathVariable Long id, Principal principal) {
    return pagoService.listarMisCobrosExtra(id, principal.getName());
}

    // ---- Pago en línea Webpay Plus (RF09) ----

    @Operation(summary = "Iniciar el pago de una cuota con Webpay (cliente autenticado)")
    @PostMapping("/{id}/webpay/iniciar")
    @PreAuthorize("hasRole('CLIENTE')")
    public IniciarWebpayResponse iniciarWebpay(@PathVariable Long id, Principal principal) {
        return webpayService.iniciarPago(id, principal.getName());
    }

    /**
     * Retorno del navegador desde Webpay (público: el redirect de Transbank no
     * lleva JWT). Confirma la transacción y redirige (302) al portal del cliente
     * con el resultado. La pertenencia ya se validó al iniciar el pago.
     */
    @Operation(summary = "Retorno de Webpay: confirma el pago y redirige al portal (público)")
    @RequestMapping(value = "/webpay/retorno", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Void> retornoWebpay(
            @RequestParam(name = "token_ws", required = false) String tokenWs,
            @RequestParam(name = "TBK_TOKEN", required = false) String tbkToken) {
        String estado;
        if (tokenWs != null && !tokenWs.isBlank()) {
            estado = webpayService.confirmarPago(tokenWs) ? "exito" : "fallo";
        } else {
            // Sin token_ws: el usuario canceló o la sesión expiró en la pasarela.
            estado = "cancelado";
        }
        URI destino = URI.create(frontendUrl + "/mis-pagos?estado=" + estado);
        return ResponseEntity.status(302).location(destino).build();
    }

    @Operation(summary = "Descargar la boleta de una cuota pagada (cliente autenticado)")
    @GetMapping("/{id}/mi-boleta")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<byte[]> descargarMiBoleta(@PathVariable Long id, Principal principal) {
        byte[] pdf = pagoService.descargarMiBoleta(id, principal.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"boleta-" + id + ".pdf\"")
                .body(pdf);
    }
}
