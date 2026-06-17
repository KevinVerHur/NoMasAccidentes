package com.example.NoMasAccidentes.service.usuario;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.common.RecursoNoEncontradoException;
import com.example.NoMasAccidentes.dto.usuario.CrearUsuarioRequest;
import com.example.NoMasAccidentes.dto.usuario.LoginRequest;
import com.example.NoMasAccidentes.dto.usuario.LoginResponse;
import com.example.NoMasAccidentes.dto.usuario.RestablecerPasswordRequest;
import com.example.NoMasAccidentes.dto.usuario.SolicitarRecuperacionRequest;
import com.example.NoMasAccidentes.dto.usuario.UsuarioResponse;
import com.example.NoMasAccidentes.model.usuario.PasswordResetToken;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.usuario.PasswordResetTokenRepository;
import com.example.NoMasAccidentes.repository.usuario.UsuarioRepository;
import com.example.NoMasAccidentes.security.JwtService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lógica de autenticación.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final CorreoService correoService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // Spring Security verifica credenciales (lanza BadCredentialsException si fallan,
        // que GlobalExceptionHandler traduce a 401).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(); // No debería ocurrir: el authenticate() ya lo cargó

        // Registrar último acceso
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol().getNombre());
        log.info("Login exitoso para email={} (RF01)", usuario.getEmail());

        return new LoginResponse(
                token,
                usuario.getEmail(),
                "%s %s".formatted(usuario.getNombre(), usuario.getApellido()),
                usuario.getRol().getNombre()
        );
    }

    /**
     * Genera un token de recuperación y envía el correo al usuario (RF — recuperación de cuenta).
     * Si el email no existe, responde igual para no revelar qué emails están registrados.
     */
    @Transactional
    public void solicitarRecuperacion(SolicitarRecuperacionRequest request) {
        usuarioRepository.findByEmail(request.email()).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            tokenRepository.save(PasswordResetToken.builder()
                    .token(token)
                    .usuario(usuario)
                    .creadoEn(LocalDateTime.now())
                    .expiraEn(LocalDateTime.now().plusHours(1))
                    .build());
            correoService.enviarRecuperacionPassword(usuario.getEmail(), token);
            log.info("Token de recuperación generado para email={}", usuario.getEmail());
        });
    }

    /**
     * Indica si un token de restablecimiento está vigente (existe, no usado y no expirado).
     * Se usa al abrir la página de restablecimiento para avisar de inmediato si el enlace caducó.
     */
    public boolean tokenEsVigente(String token) {
        return tokenRepository.findByToken(token)
                .map(PasswordResetToken::estaVigente)
                .orElse(false);
    }

    /**
     * Valida el token y actualiza la contraseña del usuario.
     */
    @Transactional
    public void restablecerPassword(RestablecerPasswordRequest request) {
        PasswordResetToken prt = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new RecursoNoEncontradoException("Token inválido o expirado"));

        if (!prt.estaVigente()) {
            throw new ConflictoNegocioException("El enlace de recuperación ya fue usado o expiró");
        }

        Usuario usuario = prt.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(request.nuevaPassword()));
        usuarioRepository.save(usuario);

        prt.setUsado(true);
        tokenRepository.save(prt);

        log.info("Contraseña restablecida para email={}", usuario.getEmail());
    }

    /** Autoregistro público (RF01). El rol ADMIN (id=1) está bloqueado. */
    @Transactional
    public LoginResponse registrar(CrearUsuarioRequest request) {
        if (Long.valueOf(1L).equals(request.idRol())) {
            throw new ConflictoNegocioException("No es posible registrarse con el rol ADMIN");
        }
        UsuarioResponse creado = usuarioService.crear(request);
        String token = jwtService.generarToken(creado.email(), creado.rol());
        log.info("Autoregistro exitoso email={} rol={} (RF01)", creado.email(), creado.rol());
        return new LoginResponse(
                token,
                creado.email(),
                "%s %s".formatted(creado.nombre(), creado.apellido()),
                creado.rol()
        );
    }
}
