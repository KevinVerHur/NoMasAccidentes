package com.example.NoMasAccidentes.service.profesional;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.profesional.ActualizarEstadoProfesionalRequest;
import com.example.NoMasAccidentes.dto.profesional.ActualizarProfesionalRequest;
import com.example.NoMasAccidentes.dto.profesional.ActualizarUbicacionRequest;
import com.example.NoMasAccidentes.dto.profesional.ProfesionalMapper;
import com.example.NoMasAccidentes.dto.profesional.ProfesionalResponse;
import com.example.NoMasAccidentes.dto.profesional.RegistrarProfesionalRequest;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.usuario.Rol;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProfesionalService {

    private static final String ROL_PROFESIONAL = "PROFESIONAL";

    private final ProfesionalRepository profesionalRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EmpresaRepository empresaRepository;
    private final ProfesionalMapper profesionalMapper;
    private final PasswordEncoder passwordEncoder;

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

        log.info("Profesional creado id={} rut={} email={}",
                guardado.getId(), guardado.getRut(), usuario.getEmail());

        return toResponseConCarga(guardado);
    }

    public Page<ProfesionalResponse> listar(Pageable pageable) {
        return profesionalRepository.findAll(pageable).map(this::toResponseConCarga);
    }

    public ProfesionalResponse obtenerPorId(Long id) {
        return toResponseConCarga(buscarOFallar(id));
    }

    public ProfesionalResponse obtenerMiPerfil(String emailUsuario) {
        return toResponseConCarga(buscarPorEmailOFallar(emailUsuario));
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

        log.info("Profesional actualizado id={}", id);

        return toResponseConCarga(profesional);
    }

    @Transactional
    public ProfesionalResponse actualizarUbicacion(Long id, ActualizarUbicacionRequest request) {
        Profesional profesional = buscarOFallar(id);

        profesional.setLatitud(request.latitud());
        profesional.setLongitud(request.longitud());

        log.debug("Ubicacion actualizada para profesional id={} lat={} lon={}",
                id, request.latitud(), request.longitud());

        return toResponseConCarga(profesional);
    }

    @Transactional
    public ProfesionalResponse actualizarEstado(Long id, ActualizarEstadoProfesionalRequest request) {
        Profesional profesional = buscarOFallar(id);

        profesional.setEstado(request.estado());

        log.info("Estado actualizado para profesional id={} estado={}", id, request.estado());

        return toResponseConCarga(profesional);
    }

    @Transactional
    public ProfesionalResponse actualizarMiEstado(
            String emailUsuario,
            ActualizarEstadoProfesionalRequest request
    ) {
        Profesional profesional = buscarPorEmailOFallar(emailUsuario);

        profesional.setEstado(request.estado());

        log.info("Estado propio actualizado para profesional id={} estado={}",
                profesional.getId(), request.estado());

        return toResponseConCarga(profesional);
    }

    @Transactional
    public void eliminar(Long id) {
        Profesional profesional = buscarOFallar(id);

        profesionalRepository.delete(profesional);

        log.info("Profesional eliminado id={}", id);
    }

    private Profesional buscarOFallar(Long id) {
        return profesionalRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", id));
    }

    private Profesional buscarPorEmailOFallar(String emailUsuario) {
        return profesionalRepository.findByUsuarioEmail(emailUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Profesional asociado al usuario no encontrado"
                ));
    }

    private ProfesionalResponse toResponseConCarga(Profesional p) {
        ProfesionalResponse base = profesionalMapper.toResponse(p);
        long cantidad = empresaRepository.countByProfesionalId(p.getId());

        return new ProfesionalResponse(
                base.id(),
                base.idUsuario(),
                base.email(),
                base.nombreCompleto(),
                base.rut(),
                base.telefono(),
                base.especialidad(),
                base.latitud(),
                base.longitud(),
                base.estado(),
                base.activo(),
                cantidad
        );
    }
    @Transactional
    public ProfesionalResponse actualizarMiPerfil(String emailUsuario, ActualizarProfesionalRequest request) {
        Profesional profesional = buscarPorEmailOFallar(emailUsuario);

        if (!profesional.getRut().equals(request.rut())) {
            profesionalRepository.findByRut(request.rut()).ifPresent(otro -> {
                if (!otro.getId().equals(profesional.getId())) {
                    throw new ConflictoNegocioException("Ya existe un profesional con RUT " + request.rut());
            }
        });

        profesional.setRut(request.rut());
        }

        profesional.setTelefono(request.telefono());
        profesional.setEspecialidad(request.especialidad());

        log.info("Perfil profesional propio actualizado id={}", profesional.getId());

        return toResponseConCarga(profesional);
    }
}