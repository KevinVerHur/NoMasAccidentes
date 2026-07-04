package com.example.NoMasAccidentes.service.pago;

import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.pago.IniciarWebpayResponse;
import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.pago.Pago;
import com.example.NoMasAccidentes.repository.pago.PagoRepository;
import com.example.NoMasAccidentes.service.empresa.EmpresaService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pago en línea de una cuota vía Webpay Plus (Transbank), RF09.
 *
 * <p>Flujo: {@code iniciarPago} crea la transacción en Webpay y devuelve el
 * token + URL; el navegador del cliente se redirige a la pasarela; al terminar,
 * Webpay redirige el navegador al backend ({@code /api/pagos/webpay/retorno}),
 * que llama a {@code confirmarPago} para hacer el commit y marcar la cuota.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WebpayService {

    private final PagoRepository pagoRepository;
    private final EmpresaService empresaService;
    private final PagoService pagoService;

    @Value("${webpay.commerce-code}")
    private String commerceCode;

    @Value("${webpay.api-key}")
    private String apiKey;

    @Value("${webpay.environment:INTEGRATION}")
    private String environment;

    @Value("${app.backend-url:http://localhost:8080}")
    private String backendUrl;

    /**
     * Inicia el pago de una cuota del cliente autenticado: valida pertenencia y
     * estado, crea la transacción en Webpay y persiste el token para
     * correlacionar el retorno. Devuelve el token y la URL de la pasarela.
     */
    @Transactional
    public IniciarWebpayResponse iniciarPago(Long idPago, String emailUsuario) {
        Long idEmpresa = empresaService.empresaAutenticada(emailUsuario).getId();
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", idPago));

        if (!pago.getPlan().getEmpresa().getId().equals(idEmpresa)) {
            throw new ConflictoNegocioException("No puedes pagar la cuota de otra empresa");
        }
        if (pago.getEstadoPago() == EstadoPago.PAGADO) {
            throw new ConflictoNegocioException("La cuota ya está pagada");
        }

        // Webpay limita la orden de compra a 26 caracteres.
        String ordenCompra = "NMA-" + idPago + "-" + UUID.randomUUID().toString().substring(0, 8);
        String sessionId = "emp-" + idEmpresa;
        double monto = pago.getMonto().doubleValue();
        String returnUrl = backendUrl + "/api/pagos/webpay/retorno";

        WebpayPlusTransactionCreateResponse respuesta;
        try {
            respuesta = transaccion().create(ordenCompra, sessionId, monto, returnUrl);
        } catch (Exception e) {
            // El SDK de Transbank lanza excepciones verificadas ante fallos de la
            // pasarela; se traducen a un conflicto de negocio para el cliente.
            log.error("Error al iniciar transacción Webpay para pago id={}", idPago, e);
            throw new ConflictoNegocioException("No se pudo iniciar el pago con Webpay. Intenta nuevamente.");
        }

        pago.setWebpayToken(respuesta.getToken());
        pago.setWebpayOrdenCompra(ordenCompra);
        log.info("Pago Webpay iniciado id={} orden={} (RF09)", idPago, ordenCompra);
        return new IniciarWebpayResponse(respuesta.getToken(), respuesta.getUrl());
    }

    /**
     * Confirma (commit) la transacción de retorno y, si Webpay la autorizó, deja
     * la cuota como PAGADA. Idempotente: si la cuota ya estaba pagada no se
     * recommittea (el token de Webpay es de un solo uso). Devuelve true si la
     * cuota queda pagada.
     */
    @Transactional
    public boolean confirmarPago(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        Pago pago = pagoRepository.findByWebpayToken(token).orElse(null);
        if (pago == null) {
            log.warn("Retorno Webpay con token desconocido");
            return false;
        }
        if (pago.getEstadoPago() == EstadoPago.PAGADO) {
            return true; // Idempotencia ante doble retorno.
        }

        WebpayPlusTransactionCommitResponse respuesta;
        try {
            respuesta = transaccion().commit(token);
        } catch (Exception e) {
            log.error("Error al confirmar transacción Webpay pago id={}", pago.getId(), e);
            return false;
        }

        boolean autorizada = "AUTHORIZED".equals(respuesta.getStatus())
                && respuesta.getResponseCode() == 0;
        if (!autorizada) {
            log.info("Pago Webpay rechazado id={} status={} code={} (RF09)",
                    pago.getId(), respuesta.getStatus(), respuesta.getResponseCode());
            return false;
        }

        pagoService.marcarComoPagada(pago, "Webpay");
        log.info("Pago Webpay confirmado id={} cuota={} (RF09)", pago.getId(), pago.getNumeroCuota());
        return true;
    }

    private WebpayPlus.Transaction transaccion() {
        return "PRODUCTION".equalsIgnoreCase(environment)
                ? WebpayPlus.Transaction.buildForProduction(commerceCode, apiKey)
                : WebpayPlus.Transaction.buildForIntegration(commerceCode, apiKey);
    }
}
