package com.example.NoMasAccidentes.service.profesional;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.profesional.ActualizarProfesionalRequest;
import com.example.NoMasAccidentes.dto.profesional.ActualizarUbicacionRequest;
import com.example.NoMasAccidentes.dto.profesional.CrearProfesionalRequest;
import com.example.NoMasAccidentes.dto.profesional.ProfesionalMapper;
import com.example.NoMasAccidentes.dto.profesional.ProfesionalResponse;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de profesionales de prevención de riesgos.
 * Mmantención de profesionales y soporte para visitas.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProfesionalService {

    private static final String ROL_PROFESIONAL = "PROFESIONAL";

    private final ProfesionalRepository profesionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesionalMapper profesionalMapper;

    @Transactional
    public ProfesionalResponse crear(CrearProfesionalRequest request) {
        Usuario usuario = usuarioRepository.findById(request.idUsuario())
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", request.idUsuario()));

        // Regla RF03: el Usuario asociado debe tener rol PROFESIONAL.
        if (!ROL_PROFESIONAL.equals(usuario.getRol().getNombre())) {
            throw new ConflictoNegocioException(
                "El usuario debe tener rol PROFESIONAL (actual: " + usuario.getRol().getNombre() + ")");
        }
        // Un Usuario solo puede tener un Profesional asociado (relación 1-1 en MER).
        if (profesionalRepository.findByUsuarioId(usuario.getId()).isPresent()) {
            throw new ConflictoNegocioException(
                "El usuario ya tiene un profesional asociado (id=" + usuario.getId() + ")");
        }
        // RUT único en toda la tabla.
        if (profesionalRepository.findByRut(request.rut()).isPresent()) {
            throw new ConflictoNegocioException("Ya existe un profesional con RUT " + request.rut());
        }

        Profesional profesional = Profesional.builder()
            .usuario(usuario)
            .rut(request.rut())
            .telefono(request.telefono())
            .especialidad(request.especialidad())
            .activo(true)
            .build();

        Profesional guardado = profesionalRepository.save(profesional);
        log.info("Profesional creado id={} rut={} idUsuario={} (RF03)",
            guardado.getId(), guardado.getRut(), usuario.getId());
        return profesionalMapper.toResponse(guardado);
    }

    public Page<ProfesionalResponse> listar(Pageable pageable) {
        return profesionalRepository.findAll(pageable).map(profesionalMapper::toResponse);
    }

    public ProfesionalResponse obtenerPorId(Long id) {
        return profesionalMapper.toResponse(buscarOFallar(id));
    }

    @Transactional
    public ProfesionalResponse actualizar(Long id, ActualizarProfesionalRequest request) {
        Profesional profesional = buscarOFallar(id);

        // Si cambia el RUT, validar unicidad contra otros profesionales.
        if (!profesional.getRut().equals(request.rut())) {
            profesionalRepository.findByRut(request.rut()).ifPresent(otro -> {
                if (!otro.getId().equals(id)) {
                    throw new ConflictoNegocioException("Ya existe un profesional con RUT " + request.rut());
                }
            });
            profesional.setRut(request.rut());
        }
        profesional.setTelefono(request.telefono());
        profesional.setEspecialidad(request.especialidad());

        log.info("Profesional actualizado id={} (RF03)", id);
        return profesionalMapper.toResponse(profesional);
    }

    /**
     * Actualiza la geolocalización del profesional. Usado por la app móvil
     * al iniciar/realizar visitas en terreno (RF13–RF17).
     */
    @Transactional
    public ProfesionalResponse actualizarUbicacion(Long id, ActualizarUbicacionRequest request) {
        Profesional profesional = buscarOFallar(id);
        profesional.setLatitud(request.latitud());
        profesional.setLongitud(request.longitud());
        log.debug("Ubicación actualizada para profesional id={} lat={} lon={}",
            id, request.latitud(), request.longitud());
        return profesionalMapper.toResponse(profesional);
    }

    /** Soft delete vía @SQLDelete. */
    @Transactional
    public void eliminar(Long id) {
        Profesional profesional = buscarOFallar(id);
        profesionalRepository.delete(profesional);
        log.info("Profesional eliminado (soft) id={} (RNF14)", id);
    }

    private Profesional buscarOFallar(Long id) {
        return profesionalRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", id));
    }
}
