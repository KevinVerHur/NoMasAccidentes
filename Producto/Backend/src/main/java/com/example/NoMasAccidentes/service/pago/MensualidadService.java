package com.example.NoMasAccidentes.service.pago;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.pago.CrearMensualidadRequest;
import com.example.NoMasAccidentes.dto.pago.MensualidadMapper;
import com.example.NoMasAccidentes.dto.pago.MensualidadResponse;
import com.example.NoMasAccidentes.model.pago.Mensualidad;
import com.example.NoMasAccidentes.repository.pago.MensualidadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catálogo de planes de pago / mensualidades (RF08).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MensualidadService {

    private final MensualidadRepository mensualidadRepository;
    private final MensualidadMapper mensualidadMapper;

    @Transactional
    public MensualidadResponse crear(CrearMensualidadRequest request) {
        if (mensualidadRepository.existsByNombrePlan(request.nombrePlan())) {
            throw new ConflictoNegocioException("Ya existe un plan con nombre " + request.nombrePlan());
        }
        Mensualidad guardada = mensualidadRepository.save(mensualidadMapper.toEntity(request));
        log.info("Plan/mensualidad creado id={} nombre={} (RF08)", guardada.getId(), guardada.getNombrePlan());
        return mensualidadMapper.toResponse(guardada);
    }

    public List<MensualidadResponse> listar() {
        return mensualidadRepository.findAll().stream().map(mensualidadMapper::toResponse).toList();
    }

    public MensualidadResponse obtenerPorId(Long id) {
        return mensualidadMapper.toResponse(buscarOFallar(id));
    }

    @Transactional
    public void eliminar(Long id) {
        mensualidadRepository.delete(buscarOFallar(id));
        log.info("Plan/mensualidad eliminado (soft) id={} (RNF14)", id);
    }

    Mensualidad buscarOFallar(Long id) {
        return mensualidadRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mensualidad", id));
    }
}
