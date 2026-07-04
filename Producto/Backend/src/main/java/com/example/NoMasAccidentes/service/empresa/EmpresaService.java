package com.example.NoMasAccidentes.service.empresa;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.empresa.ActualizarEmpresaRequest;
import com.example.NoMasAccidentes.dto.empresa.CrearEmpresaRequest;
import com.example.NoMasAccidentes.dto.empresa.EmpresaMapper;
import com.example.NoMasAccidentes.dto.empresa.EmpresaResponse;
import com.example.NoMasAccidentes.model.cliente.Cliente;
import com.example.NoMasAccidentes.model.empresa.Empresa;
import com.example.NoMasAccidentes.model.empresa.EstadoEmpresa;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.rubro.Rubro;
import com.example.NoMasAccidentes.model.usuario.PasswordResetToken;
import com.example.NoMasAccidentes.model.usuario.Rol;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.cliente.ClienteRepository;
import com.example.NoMasAccidentes.repository.empresa.EmpresaRepository;
import com.example.NoMasAccidentes.repository.profesional.ProfesionalRepository;
import com.example.NoMasAccidentes.repository.rubro.RubroRepository;
import com.example.NoMasAccidentes.repository.usuario.PasswordResetTokenRepository;
import com.example.NoMasAccidentes.repository.usuario.RolRepository;
import com.example.NoMasAccidentes.repository.usuario.UsuarioRepository;
import com.example.NoMasAccidentes.service.usuario.CorreoService;
import com.example.NoMasAccidentes.service.visita.ListaChequeoService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.NoMasAccidentes.service.pago.PlanPagoService;
/**
 * Gestión de empresas cliente (RF06–RF12). El alta crea la empresa (persona
 * jurídica) junto con su primer representante (persona de contacto), sobre
 * quien se provisiona el acceso al portal (rol CLIENTE) por invitación.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EmpresaService {

    private static final String ROL_CLIENTE = "CLIENTE";
    private static final long INVITACION_VALIDEZ_HORAS = 1;

    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final RubroRepository rubroRepository;
    private final ProfesionalRepository profesionalRepository;
    private final EmpresaMapper empresaMapper;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final CorreoService correoService;
    private final PasswordEncoder passwordEncoder;
    private final ListaChequeoService listaChequeoService;
    private final PlanPagoService planPagoService;
    @Transactional
    public EmpresaResponse crear(CrearEmpresaRequest request) {
        if (empresaRepository.findByRut(request.rut()).isPresent()) {
            throw new ConflictoNegocioException("Ya existe una empresa con RUT " + request.rut());
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ConflictoNegocioException("Ya existe un usuario con ese correo");
        }

        Rubro rubro = resolverRubro(request.idRubro());
        Profesional profesional = resolverProfesional(request.idProfesional());

        Empresa empresa = empresaRepository.save(Empresa.builder()
                .razonSocial(request.razonSocial())
                .rut(request.rut())
                .direccion(request.direccion())
                .comuna(request.comuna())
                .rubro(rubro)
                .plan(request.plan())
                .cantidadTrabajadores(request.cantidadTrabajadores())
                .estado(EstadoEmpresa.ACTIVO)
                .profesional(profesional)
                .build());

        listaChequeoService.crearPorDefecto(empresa);
        crearPrimerRepresentante(empresa, request);
        planPagoService.asignarPlanBasico(empresa);

        
        log.info("Empresa creada id={} rut={} (RF06)", empresa.getId(), empresa.getRut());
        return empresaMapper.toResponse(empresa);
    }

    /**
     * Crea el primer representante de la empresa y le provisiona el acceso al
     * portal (rol CLIENTE, contraseña aleatoria inutilizable + invitación por
     * correo para que defina la suya). La credencial es de la persona.
     */
    private void crearPrimerRepresentante(Empresa empresa, CrearEmpresaRequest request) {
        Usuario usuario = provisionarUsuarioRepresentante(request);
        clienteRepository.save(Cliente.builder()
                .empresa(empresa)
                .nombre(request.nombreContacto())
                .cargo(request.cargoContacto())
                .email(request.email())
                .telefono(request.telefono())
                .usuario(usuario)
                .build());
        enviarInvitacion(usuario);
    }

    private Usuario provisionarUsuarioRepresentante(CrearEmpresaRequest request) {
        Rol rolCliente = rolRepository.findByNombre(ROL_CLIENTE)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol CLIENTE no encontrado"));
        return usuarioRepository.save(Usuario.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .nombre(request.nombreContacto())
                .apellido(request.razonSocial())
                .rol(rolCliente)
                .activo(true)
                .build());
    }

    /** Genera el token de activación y dispara el correo de invitación. */
    private void enviarInvitacion(Usuario usuario) {
        String token = UUID.randomUUID().toString();
        tokenRepository.save(PasswordResetToken.builder()
                .token(token)
                .usuario(usuario)
                .creadoEn(LocalDateTime.now())
                .expiraEn(LocalDateTime.now().plusHours(INVITACION_VALIDEZ_HORAS))
                .build());
        correoService.enviarInvitacionCliente(usuario.getEmail(), token);
    }

    public Page<EmpresaResponse> listar(Pageable pageable) {
        return empresaRepository.findAll(pageable).map(empresaMapper::toResponse);
    }

    public EmpresaResponse obtenerPorId(Long id) {
        return empresaMapper.toResponse(buscarOFallar(id));
    }

    @Transactional
    public EmpresaResponse actualizar(Long id, ActualizarEmpresaRequest request) {
        Empresa empresa = buscarOFallar(id);

        if (!empresa.getRut().equals(request.rut())) {
            empresaRepository.findByRut(request.rut()).ifPresent(otra -> {
                if (!otra.getId().equals(id)) {
                    throw new ConflictoNegocioException("Ya existe una empresa con RUT " + request.rut());
                }
            });
        }

        empresa.setRazonSocial(request.razonSocial());
        empresa.setRut(request.rut());
        empresa.setDireccion(request.direccion());
        empresa.setComuna(request.comuna());
        empresa.setRubro(resolverRubro(request.idRubro()));
        empresa.setPlan(request.plan());
        empresa.setCantidadTrabajadores(request.cantidadTrabajadores());
        empresa.setEstado(request.estado());
        empresa.setProfesional(resolverProfesional(request.idProfesional()));

        log.info("Empresa actualizada id={} (RF06)", id);
        return empresaMapper.toResponse(empresa);
    }

    /** Suspende el servicio de la empresa y bloquea el acceso de sus representantes (RF12). */
    @Transactional
    public EmpresaResponse suspender(Long id) {
        Empresa empresa = buscarOFallar(id);
        if (empresa.getEstado() == EstadoEmpresa.SUSPENDIDO) {
            throw new ConflictoNegocioException("La empresa ya está suspendida");
        }
        empresa.setEstado(EstadoEmpresa.SUSPENDIDO);
        cambiarAccesoRepresentantes(id, false);
        log.info("Empresa suspendida id={} (RF09/RF12)", id);
        return empresaMapper.toResponse(empresa);
    }

    /** Reactiva la empresa y restablece el acceso de sus representantes (RF09/RF12). */
    @Transactional
    public EmpresaResponse reactivar(Long id) {
        Empresa empresa = buscarOFallar(id);
        empresa.setEstado(EstadoEmpresa.ACTIVO);
        cambiarAccesoRepresentantes(id, true);
        log.info("Empresa reactivada id={} (RF09/RF12)", id);
        return empresaMapper.toResponse(empresa);
    }

    private void cambiarAccesoRepresentantes(Long idEmpresa, boolean activo) {
        clienteRepository.findByEmpresaId(idEmpresa).forEach(rep -> {
            if (rep.getUsuario() != null) {
                usuarioRepository.findById(rep.getUsuario().getId()).ifPresent(u -> {
                    u.setActivo(activo);
                    usuarioRepository.save(u);
                });
            }
        });
    }

    @Transactional
    public void eliminar(Long id) {
        empresaRepository.delete(buscarOFallar(id));
        log.info("Empresa eliminada (soft) id={} (RNF14)", id);
    }

    /** Resuelve la empresa del usuario autenticado, vía su representante (portal cliente). */
    public Empresa empresaAutenticada(String email) {
        return clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No hay un representante asociado al usuario " + email))
                .getEmpresa();
    }

    public EmpresaResponse empresaPorEmail(String email) {
        return empresaMapper.toResponse(empresaAutenticada(email));
    }

    private Empresa buscarOFallar(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa", id));
    }

    private Rubro resolverRubro(Long idRubro) {
        return rubroRepository.findById(idRubro)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rubro", idRubro));
    }

    private Profesional resolverProfesional(Long idProfesional) {
        if (idProfesional == null) return null;
        return profesionalRepository.findById(idProfesional)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", idProfesional));
    }
}
