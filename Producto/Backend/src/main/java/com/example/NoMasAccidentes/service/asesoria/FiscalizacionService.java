package com.example.NoMasAccidentes.service.asesoria;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.asesoria.CrearFiscalizacionRequest;
import com.example.NoMasAccidentes.dto.asesoria.FiscalizacionMapper;
import com.example.NoMasAccidentes.dto.asesoria.FiscalizacionResponse;
import com.example.NoMasAccidentes.model.asesoria.Asesoria;
import com.example.NoMasAccidentes.model.asesoria.Fiscalizacion;
import com.example.NoMasAccidentes.repository.asesoria.AsesoriaRepository;
import com.example.NoMasAccidentes.repository.asesoria.FiscalizacionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro de fiscalizaciones asociadas a una asesoría (RF22) y base del
 * cumplimiento normativo (RF42-44).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FiscalizacionService {

    private final FiscalizacionRepository fiscalizacionRepository;
    private final AsesoriaRepository asesoriaRepository;
    private final FiscalizacionMapper fiscalizacionMapper;

    @Transactional
    public FiscalizacionResponse crear(CrearFiscalizacionRequest request) {
        Asesoria asesoria = asesoriaRepository.findById(request.idAsesoria())
                .orElseThrow(() -> new RecursoNoEncontradoException("Asesoría", request.idAsesoria()));

        Fiscalizacion fiscalizacion = Fiscalizacion.builder()
                .asesoria(asesoria)
                .fecha(request.fecha())
                .entidadFiscalizadora(request.entidadFiscalizadora())
                .motivo(request.motivo())
                .resultado(request.resultado())
                .observaciones(request.observaciones())
                .build();

        Fiscalizacion guardada = fiscalizacionRepository.save(fiscalizacion);
        log.info("Fiscalización registrada id={} asesoria={} entidad={} (RF22, RF42-44)",
                guardada.getId(), asesoria.getId(), request.entidadFiscalizadora());
        return fiscalizacionMapper.toResponse(guardada);
    }

    public List<FiscalizacionResponse> listarPorAsesoria(Long idAsesoria) {
        return fiscalizacionRepository.findByAsesoriaIdOrderByFechaDesc(idAsesoria)
                .stream().map(fiscalizacionMapper::toResponse).toList();
    }

    public FiscalizacionResponse obtenerPorId(Long id) {
        return fiscalizacionMapper.toResponse(buscarOFallar(id));
    }

    private Fiscalizacion buscarOFallar(Long id) {
        return fiscalizacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Fiscalización", id));
    }
}
