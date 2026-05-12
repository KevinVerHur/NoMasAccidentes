package com.example.NoMasAccidentes.service.profesional;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.profesional.ActualizarProfesionalRequest;
import com.example.NoMasAccidentes.dto.profesional.ActualizarUbicacionRequest;
import com.example.NoMasAccidentes.dto.profesional.ProfesionalMapper;
import com.example.NoMasAccidentes.dto.profesional.ProfesionalResponse;
import com.example.NoMasAccidentes.dto.profesional.RegistrarProfesionalRequest;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.usuario.Rol;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.repository.usuario.RolRepository;
import com.example.NoMasAccidentes.repository.usuario.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de profesionales de prevención de riesgos.
 * Mantención de profesionales y soporte para visitas (RF03–RF04).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProfesionalService {

    private static final String ROL_PROFESIONAL = "PROFESIONAL";

    private final ProfesionalRepository profesionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final ProfesionalMapper profesionalMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registro unificado: crea el Usuario con rol PROFESIONAL y luego el Profesional (RF03).
     * No requiere que el usuario exista previamente.
     */
    @Transactional
    public ProfesionalResponse crear(RegistrarProfesionalRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoNegocioException("Ya existe un usuario con email " + request.email());
        }
        if (profesionalRepository.findByRut(request.rut()).isPresent()) {
            throw new ConflictoNegocioException("Ya existe un profesional con RUT " + request.rut());
        }

        Rol rolProfesional = rolRepository.findByNombre(ROL_PROFESIONAL)
            .orElseThrow(() -> new RecursoNoEncontradoException("Rol PROFESIONAL no encontrado"));

        Usuario usuario = Usuario.builder()
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .nombre(request.nombre())
            .apellido(request.apellido())
            .rol(rolProfesional)
            .activo(true)
            .build();
        usuarioRepository.save(usuario);

        Profesional profesional = Profesional.builder()
            .usuario(usuario)
            .rut(request.rut())
            .telefono(request.telefono())
            .especialidad(request.especialidad())
            .activo(true)
            .build();

        Profesional guardado = profesionalRepository.save(profesional);
        log.info("Profesional creado id={} rut={} email={} (RF03)",
            guardado.getId(), guardado.getRut(), usuario.getEmail());
        return toResponseConCarga(guardado);
    }

    public Page<ProfesionalResponse> listar(Pageable pageable) {
        return profesionalRepository.findAll(pageable).map(this::toResponseConCarga);
    }

    public ProfesionalResponse obtenerPorId(Long id) {
        return toResponseConCarga(buscarOFallar(id));
    }

    @Transactional
    public ProfesionalResponse actualizar(Long id, ActualizarProfesionalRequest request) {
        Profesional profesional = buscarOFallar(id);

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
        return toResponseConCarga(profesional);
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
        return toResponseConCarga(profesional);
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

    /** Construye el response incluyendo la cantidad real de clientes asignados. */
    private ProfesionalResponse toResponseConCarga(Profesional p) {
        ProfesionalResponse base = profesionalMapper.toResponse(p);
        long cantidad = clienteRepository.countByProfesionalId(p.getId());
        return new ProfesionalResponse(
            base.id(), base.idUsuario(), base.email(), base.nombreCompleto(),
            base.rut(), base.telefono(), base.especialidad(),
            base.latitud(), base.longitud(), base.estado(), base.activo(), cantidad
        );
    }
}
