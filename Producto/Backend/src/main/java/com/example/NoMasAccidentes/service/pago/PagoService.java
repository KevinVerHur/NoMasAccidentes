package com.example.NoMasAccidentes.service.pago;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.pago.CobroExtraMapper;
import com.example.NoMasAccidentes.dto.pago.CobroExtraResponse;
import com.example.NoMasAccidentes.dto.pago.CrearCobroExtraRequest;
import com.example.NoMasAccidentes.dto.pago.PagoMapper;
import com.example.NoMasAccidentes.dto.pago.PagoResponse;
import com.example.NoMasAccidentes.dto.pago.RegistrarPagoRequest;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.empresa.EstadoEmpresa;
import com.example.NoMasAccidentes.model.pago.CobroExtra;
import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.pago.Pago;
import com.example.NoMasAccidentes.repository.pago.CobroExtraRepository;
import com.example.NoMasAccidentes.repository.pago.PagoRepository;
import com.example.NoMasAccidentes.service.empresa.EmpresaService;
import com.example.NoMasAccidentes.service.informe.AlmacenamientoInformeService;
import com.example.NoMasAccidentes.service.notificacion.NotificacionEventoService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro de pagos, historial, morosidad y suspensión (RF09–RF12).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PagoService {

    /** Cuotas atrasadas a partir de las cuales se suspende el servicio (RF12). */
    private static final int UMBRAL_SUSPENSION = 2;

    private final PagoRepository pagoRepository;
    private final CobroExtraRepository cobroExtraRepository;
    private final PagoMapper pagoMapper;
    private final CobroExtraMapper cobroExtraMapper;
    private final EmpresaService empresaService;
    private final BoletaPagoPdfService boletaPagoPdfService;
    private final AlmacenamientoInformeService almacenamiento;
    private final NotificacionEventoService notificacionEventoService;

    /** Registra el pago de una cuota (RF09). */
    @Transactional
    public PagoResponse registrar(Long idPago, RegistrarPagoRequest request) {
        Pago pago = buscarOFallar(idPago);
        if (pago.getEstadoPago() == EstadoPago.PAGADO) {
            throw new ConflictoNegocioException("La cuota ya está pagada");
        }
        marcarComoPagada(pago, request.medioPago());
        log.info("Pago registrado id={} cuota={} (RF09)", idPago, pago.getNumeroCuota());
        return pagoMapper.toResponse(pago);
    }

    /**
     * Deja la cuota como PAGADA (fecha de hoy + medio) y regulariza el estado de
     * la empresa si corresponde. Reutilizado por el registro manual (RF09) y por
     * el pago en línea Webpay ({@link WebpayService}).
     */
    @Transactional
    public void marcarComoPagada(Pago pago, String medioPago) {
        pago.setEstadoPago(EstadoPago.PAGADO);
        pago.setFechaPago(LocalDate.now());
        pago.setMedioPago(medioPago);
        reevaluarEstadoEmpresa(pago.getPlan().getEmpresa());

        // Avisa al representante (bandeja + correo con el comprobante adjunto). El
        // PDF se genera en memoria (sin tocar S3) para no arriesgar el pago ya
        // confirmado; el correo es @Async (fire-and-forget).
        byte[] comprobante = boletaPagoPdfService.generar(pago);
        notificacionEventoService.notificarPagoRealizado(pago, comprobante);
    }

    /** Historial de pagos de una empresa (RF10). */
    public List<PagoResponse> historialPorEmpresa(Long idEmpresa) {
        return pagoRepository.findByPlanEmpresaIdOrderByFechaVencimientoDesc(idEmpresa)
                .stream().map(pagoMapper::toResponse).toList();
    }

    /** Historial de la empresa del usuario autenticado (portal cliente, solo lectura). */
    public List<PagoResponse> misPagos(String emailUsuario) {
        Long idEmpresa = empresaService.empresaAutenticada(emailUsuario).getId();
        return historialPorEmpresa(idEmpresa);
    }

    /**
     * Marca como ATRASADAS las cuotas vencidas e impagas y deja MOROSAS a las
     * empresas afectadas (RF11). Devuelve la cantidad de cuotas marcadas.
     */
    @Transactional
    public int evaluarMorosidad() {
        List<Pago> vencidas = pagoRepository
                .findByEstadoPagoAndFechaVencimientoBefore(EstadoPago.PENDIENTE, LocalDate.now());
        for (Pago pago : vencidas) {
            pago.setEstadoPago(EstadoPago.ATRASADO);
            Empresa empresa = pago.getPlan().getEmpresa();
            if (empresa.getEstado() == EstadoEmpresa.ACTIVO) {
                empresa.setEstado(EstadoEmpresa.MOROSO);
            }
        }
        log.info("Morosidad evaluada: {} cuotas marcadas ATRASADO (RF11)", vencidas.size());
        return vencidas.size();
    }

    /**
     * Suspende el servicio de las empresas con {@value #UMBRAL_SUSPENSION} o más
     * cuotas atrasadas (RF12). Devuelve la cantidad de empresas suspendidas.
     */
    @Transactional
    public int suspenderMorosos() {
        Map<Empresa, Long> atrasadasPorEmpresa = pagoRepository.findByEstadoPago(EstadoPago.ATRASADO)
                .stream()
                .collect(Collectors.groupingBy(p -> p.getPlan().getEmpresa(), Collectors.counting()));

        int suspendidos = 0;
        for (Map.Entry<Empresa, Long> e : atrasadasPorEmpresa.entrySet()) {
            Empresa empresa = e.getKey();
            if (e.getValue() >= UMBRAL_SUSPENSION && empresa.getEstado() != EstadoEmpresa.SUSPENDIDO) {
                empresa.setEstado(EstadoEmpresa.SUSPENDIDO);
                suspendidos++;
                log.info("Empresa suspendida por morosidad id={} ({} cuotas atrasadas) (RF12)",
                        empresa.getId(), e.getValue());
            }
        }
        return suspendidos;
    }

    /** Agrega un cobro extra a una cuota (RF21, RF24, RF28). */
    @Transactional
    public CobroExtraResponse agregarCobroExtra(Long idPago, CrearCobroExtraRequest request) {
        Pago pago = buscarOFallar(idPago);
        CobroExtra cobro = CobroExtra.builder()
                .pago(pago)
                .tipoCobro(request.tipoCobro())
                .idOrigen(request.idOrigen())
                .descripcion(request.descripcion())
                .monto(request.monto())
                .fechaGeneracion(LocalDate.now())
                .build();
        CobroExtra guardado = cobroExtraRepository.save(cobro);
        log.info("Cobro extra agregado id={} pago={} tipo={}", guardado.getId(), idPago, request.tipoCobro());
        return cobroExtraMapper.toResponse(guardado);
    }

    public List<CobroExtraResponse> listarCobrosExtra(Long idPago) {
        return cobroExtraRepository.findByPagoId(idPago).stream().map(cobroExtraMapper::toResponse).toList();
    }

    public PagoResponse obtenerPorId(Long id) {
        return pagoMapper.toResponse(buscarOFallar(id));
    }

    /** Si la empresa ya no tiene cuotas atrasadas, vuelve de MOROSO a ACTIVO. */
    private void reevaluarEstadoEmpresa(Empresa empresa) {
        if (empresa.getEstado() != EstadoEmpresa.MOROSO) {
            return;
        }
        boolean tieneAtrasos = !pagoRepository
                .findByPlanEmpresaIdAndEstadoPago(empresa.getId(), EstadoPago.ATRASADO).isEmpty();
        if (!tieneAtrasos) {
            empresa.setEstado(EstadoEmpresa.ACTIVO);
            log.info("Empresa regularizada id={} -> ACTIVO (RF11)", empresa.getId());
        }
    }

    private Pago buscarOFallar(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }

    /**
     * Comprobante de pago (PDF) de una cuota PAGADA de la empresa del cliente
     * autenticado (RF09). Además de devolverlo, lo persiste en el almacenamiento
     * (S3 o disco) bajo {@code comprobantes/<idEmpresa>/}, igual que el resto de
     * los documentos. Se genera al descargar para no acoplar el almacenamiento a
     * la transacción del pago.
     */
    public byte[] descargarMiBoleta(Long idPago, String emailUsuario) {
        Long idEmpresa = empresaService.empresaAutenticada(emailUsuario).getId();
        Pago pago = buscarOFallar(idPago);
        if (!pago.getPlan().getEmpresa().getId().equals(idEmpresa)) {
            throw new ConflictoNegocioException("No puedes descargar el comprobante de otra empresa");
        }
        if (pago.getEstadoPago() != EstadoPago.PAGADO) {
            throw new ConflictoNegocioException("La cuota aún no está pagada");
        }
        byte[] pdf = boletaPagoPdfService.generar(pago);
        almacenamiento.guardar("comprobantes", idEmpresa, "comprobante-" + idPago + ".pdf", pdf);
        return pdf;
    }

    public List<CobroExtraResponse> listarMisCobrosExtra(Long idPago, String emailUsuario) {
    Long idEmpresa = empresaService.empresaAutenticada(emailUsuario).getId();

    Pago pago = buscarOFallar(idPago);

    if (!pago.getPlan().getEmpresa().getId().equals(idEmpresa)) {
        throw new ConflictoNegocioException("No puedes consultar cobros de otra empresa");
    }

    return cobroExtraRepository.findByPagoId(idPago)
            .stream()
            .map(cobroExtraMapper::toResponse)
            .toList();
}

}
