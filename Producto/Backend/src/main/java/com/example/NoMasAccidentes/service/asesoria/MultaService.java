package com.example.NoMasAccidentes.service.asesoria;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.asesoria.CrearMultaRequest;
import com.example.NoMasAccidentes.dto.asesoria.MultaMapper;
import com.example.NoMasAccidentes.dto.asesoria.MultaResponse;
import com.example.NoMasAccidentes.model.asesoria.EstadoMulta;
import com.example.NoMasAccidentes.model.asesoria.Fiscalizacion;
import com.example.NoMasAccidentes.model.asesoria.Multa;
import com.example.NoMasAccidentes.repository.asesoria.FiscalizacionRepository;
import com.example.NoMasAccidentes.repository.asesoria.MultaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro y seguimiento de multas derivadas de una fiscalización (RF42-44).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MultaService {

    private final MultaRepository multaRepository;
    private final FiscalizacionRepository fiscalizacionRepository;
    private final MultaMapper multaMapper;

    @Transactional
    public MultaResponse crear(CrearMultaRequest request) {
        Fiscalizacion fiscalizacion = fiscalizacionRepository.findById(request.idFiscalizacion())
                .orElseThrow(() -> new RecursoNoEncontradoException("Fiscalización", request.idFiscalizacion()));

        Multa multa = Multa.builder()
                .fiscalizacion(fiscalizacion)
                .fechaEmision(request.fechaEmision())
                .monto(request.monto())
                .motivo(request.motivo())
                .normativaInfringida(request.normativaInfringida())
                .estadoPago(EstadoMulta.PENDIENTE)
                .build();

        Multa guardada = multaRepository.save(multa);
        log.info("Multa registrada id={} fiscalizacion={} monto={} (RF42-44)",
                guardada.getId(), fiscalizacion.getId(), request.monto());
        return multaMapper.toResponse(guardada);
    }

    /** Actualiza el estado de pago de la multa (RF42-44, seguimiento). */
    @Transactional
    public MultaResponse actualizarEstadoPago(Long id, EstadoMulta estadoPago) {
        Multa multa = buscarOFallar(id);
        multa.setEstadoPago(estadoPago);
        log.info("Multa id={} -> estadoPago={}", id, estadoPago);
        return multaMapper.toResponse(multa);
    }

    public List<MultaResponse> listarPorFiscalizacion(Long idFiscalizacion) {
        return multaRepository.findByFiscalizacionIdOrderByFechaEmisionDesc(idFiscalizacion)
                .stream().map(multaMapper::toResponse).toList();
    }

    public MultaResponse obtenerPorId(Long id) {
        return multaMapper.toResponse(buscarOFallar(id));
    }

    private Multa buscarOFallar(Long id) {
        return multaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Multa", id));
    }
}
