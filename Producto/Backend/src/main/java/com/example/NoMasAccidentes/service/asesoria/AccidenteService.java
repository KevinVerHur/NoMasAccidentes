package com.example.NoMasAccidentes.service.asesoria;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.asesoria.AccidenteMapper;
import com.example.NoMasAccidentes.dto.asesoria.AccidenteResponse;
import com.example.NoMasAccidentes.dto.asesoria.CrearAccidenteRequest;
import com.example.NoMasAccidentes.model.asesoria.Accidente;
import com.example.NoMasAccidentes.model.asesoria.Asesoria;
import com.example.NoMasAccidentes.repository.asesoria.AccidenteRepository;
import com.example.NoMasAccidentes.repository.asesoria.AsesoriaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro de accidentes laborales asociados a una asesoría (RF22).
 * Sustenta los indicadores de accidentabilidad por cliente (RF35).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AccidenteService {

    private final AccidenteRepository accidenteRepository;
    private final AsesoriaRepository asesoriaRepository;
    private final AccidenteMapper accidenteMapper;

    @Transactional
    public AccidenteResponse crear(CrearAccidenteRequest request) {
        Asesoria asesoria = asesoriaRepository.findById(request.idAsesoria())
                .orElseThrow(() -> new RecursoNoEncontradoException("Asesoría", request.idAsesoria()));

        Accidente accidente = Accidente.builder()
                .asesoria(asesoria)
                .fechaOcurrencia(request.fechaOcurrencia())
                .descripcion(request.descripcion())
                .gravedad(request.gravedad())
                .trabajadorAfectado(request.trabajadorAfectado())
                .diasPerdidos(request.diasPerdidos())
                .fueReportadoSusseso(request.fueReportadoSusseso())
                .build();

        Accidente guardado = accidenteRepository.save(accidente);
        log.info("Accidente registrado id={} asesoria={} gravedad={} (RF22)",
                guardado.getId(), asesoria.getId(), request.gravedad());
        return accidenteMapper.toResponse(guardado);
    }

    public List<AccidenteResponse> listarPorAsesoria(Long idAsesoria) {
        return accidenteRepository.findByAsesoriaIdOrderByFechaOcurrenciaDesc(idAsesoria)
                .stream().map(accidenteMapper::toResponse).toList();
    }

    public AccidenteResponse obtenerPorId(Long id) {
        return accidenteMapper.toResponse(buscarOFallar(id));
    }

    private Accidente buscarOFallar(Long id) {
        return accidenteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Accidente", id));
    }
}
