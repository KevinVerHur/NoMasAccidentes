package com.example.NoMasAccidentes.service.pago;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.pago.CobroExtraMapper;
import com.example.NoMasAccidentes.dto.pago.CobroExtraResponse;
import com.example.NoMasAccidentes.dto.pago.CrearCobroExtraRequest;
import com.example.NoMasAccidentes.dto.pago.PagoMapper;
import com.example.NoMasAccidentes.dto.pago.PagoResponse;
import com.example.NoMasAccidentes.dto.pago.RegistrarPagoRequest;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.cliente.EstadoCliente;
import com.example.NoMasAccidentes.model.pago.CobroExtra;
import com.example.NoMasAccidentes.model.pago.EstadoPago;
import com.example.NoMasAccidentes.model.pago.Pago;
import com.example.NoMasAccidentes.repository.pago.CobroExtraRepository;
import com.example.NoMasAccidentes.repository.pago.PagoRepository;
import com.example.NoMasAccidentes.service.cliente.ClienteService;
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
    private final ClienteService clienteService;

    /** Registra el pago de una cuota (RF09). */
    @Transactional
    public PagoResponse registrar(Long idPago, RegistrarPagoRequest request) {
        Pago pago = buscarOFallar(idPago);
        if (pago.getEstadoPago() == EstadoPago.PAGADO) {
            throw new ConflictoNegocioException("La cuota ya está pagada");
        }
        pago.setEstadoPago(EstadoPago.PAGADO);
        pago.setFechaPago(LocalDate.now());
        pago.setMedioPago(request.medioPago());
        log.info("Pago registrado id={} cuota={} (RF09)", idPago, pago.getNumeroCuota());

        reevaluarEstadoCliente(pago.getPlan().getCliente());
        return pagoMapper.toResponse(pago);
    }

    /** Historial de pagos de un cliente (RF10). */
    public List<PagoResponse> historialPorCliente(Long idCliente) {
        return pagoRepository.findByPlanClienteIdOrderByFechaVencimientoDesc(idCliente)
                .stream().map(pagoMapper::toResponse).toList();
    }

    /** Historial del cliente autenticado (portal cliente, solo lectura). */
    public List<PagoResponse> misPagos(String emailUsuario) {
        Long idCliente = clienteService.clienteAutenticado(emailUsuario).getId();
        return historialPorCliente(idCliente);
    }

    /**
     * Marca como ATRASADAS las cuotas vencidas e impagas y deja MOROSOS a los
     * clientes afectados (RF11). Devuelve la cantidad de cuotas marcadas.
     */
    @Transactional
    public int evaluarMorosidad() {
        List<Pago> vencidas = pagoRepository
                .findByEstadoPagoAndFechaVencimientoBefore(EstadoPago.PENDIENTE, LocalDate.now());
        for (Pago pago : vencidas) {
            pago.setEstadoPago(EstadoPago.ATRASADO);
            Cliente cliente = pago.getPlan().getCliente();
            if (cliente.getEstado() == EstadoCliente.ACTIVO) {
                cliente.setEstado(EstadoCliente.MOROSO);
            }
        }
        log.info("Morosidad evaluada: {} cuotas marcadas ATRASADO (RF11)", vencidas.size());
        return vencidas.size();
    }

    /**
     * Suspende el servicio de los clientes con {@value #UMBRAL_SUSPENSION} o más
     * cuotas atrasadas (RF12). Devuelve la cantidad de clientes suspendidos.
     */
    @Transactional
    public int suspenderMorosos() {
        Map<Cliente, Long> atrasadasPorCliente = pagoRepository.findByEstadoPago(EstadoPago.ATRASADO)
                .stream()
                .collect(Collectors.groupingBy(p -> p.getPlan().getCliente(), Collectors.counting()));

        int suspendidos = 0;
        for (Map.Entry<Cliente, Long> e : atrasadasPorCliente.entrySet()) {
            Cliente cliente = e.getKey();
            if (e.getValue() >= UMBRAL_SUSPENSION && cliente.getEstado() != EstadoCliente.SUSPENDIDO) {
                cliente.setEstado(EstadoCliente.SUSPENDIDO);
                suspendidos++;
                log.info("Cliente suspendido por morosidad id={} ({} cuotas atrasadas) (RF12)",
                        cliente.getId(), e.getValue());
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

    /** Si el cliente ya no tiene cuotas atrasadas, vuelve de MOROSO a ACTIVO. */
    private void reevaluarEstadoCliente(Cliente cliente) {
        if (cliente.getEstado() != EstadoCliente.MOROSO) {
            return;
        }
        boolean tieneAtrasos = !pagoRepository
                .findByPlanClienteIdAndEstadoPago(cliente.getId(), EstadoPago.ATRASADO).isEmpty();
        if (!tieneAtrasos) {
            cliente.setEstado(EstadoCliente.ACTIVO);
            log.info("Cliente regularizado id={} -> ACTIVO (RF11)", cliente.getId());
        }
    }

    private Pago buscarOFallar(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }
}
