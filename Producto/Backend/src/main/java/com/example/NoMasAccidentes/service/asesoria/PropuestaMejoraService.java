package com.example.NoMasAccidentes.service.asesoria;

import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.asesoria.CrearPropuestaMejoraRequest;
import com.example.NoMasAccidentes.dto.asesoria.PropuestaMejoraMapper;
import com.example.NoMasAccidentes.dto.asesoria.PropuestaMejoraResponse;
import com.example.NoMasAccidentes.model.asesoria.EstadoPropuesta;
import com.example.NoMasAccidentes.model.asesoria.PropuestaMejora;
import com.example.NoMasAccidentes.model.informe.Informe;
import com.example.NoMasAccidentes.repository.asesoria.PropuestaMejoraRepository;
import com.example.NoMasAccidentes.repository.informe.InformeRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Propuestas de mejora del informe de una asesoría (RF25): registro y
 * seguimiento de las acciones recomendadas.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PropuestaMejoraService {

    private final PropuestaMejoraRepository propuestaRepository;
    private final InformeRepository informeRepository;
    private final PropuestaMejoraMapper propuestaMapper;

    @Transactional
    public PropuestaMejoraResponse crear(CrearPropuestaMejoraRequest request) {
        Informe informe = informeRepository.findById(request.idInforme())
                .orElseThrow(() -> new RecursoNoEncontradoException("Informe", request.idInforme()));

        PropuestaMejora propuesta = PropuestaMejora.builder()
                .informe(informe)
                .descripcion(request.descripcion())
                .fechaPropuesta(LocalDate.now())
                .fechaLimite(request.fechaLimite())
                .responsable(request.responsable())
                .estado(EstadoPropuesta.PENDIENTE)
                .build();

        PropuestaMejora guardada = propuestaRepository.save(propuesta);
        log.info("Propuesta de mejora registrada id={} informe={} (RF25)",
                guardada.getId(), informe.getId());
        return propuestaMapper.toResponse(guardada);
    }

    /** Actualiza el estado de la propuesta; al verificarla fija la fecha de verificación. */
    @Transactional
    public PropuestaMejoraResponse actualizarEstado(Long id, EstadoPropuesta estado) {
        PropuestaMejora propuesta = buscarOFallar(id);
        propuesta.setEstado(estado);
        propuesta.setFechaVerificacion(estado == EstadoPropuesta.VERIFICADA ? LocalDate.now() : null);
        log.info("Propuesta de mejora id={} -> estado={}", id, estado);
        return propuestaMapper.toResponse(propuesta);
    }

    public List<PropuestaMejoraResponse> listarPorInforme(Long idInforme) {
        return propuestaRepository.findByInformeIdOrderByFechaPropuestaDesc(idInforme)
                .stream().map(propuestaMapper::toResponse).toList();
    }

    public PropuestaMejoraResponse obtenerPorId(Long id) {
        return propuestaMapper.toResponse(buscarOFallar(id));
    }

    private PropuestaMejora buscarOFallar(Long id) {
        return propuestaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Propuesta de mejora", id));
    }
}
