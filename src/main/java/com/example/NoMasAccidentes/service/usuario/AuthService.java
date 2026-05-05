package com.example.NoMasAccidentes.service.usuario;

import com.example.NoMasAccidentes.common.ConflictoNegocioException;
import com.example.NoMasAccidentes.dto.usuario.CrearUsuarioRequest;
import com.example.NoMasAccidentes.dto.usuario.LoginRequest;
import com.example.NoMasAccidentes.dto.usuario.LoginResponse;
import com.example.NoMasAccidentes.dto.usuario.UsuarioResponse;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import com.example.NoMasAccidentes.repository.usuario.UsuarioRepository;
import com.example.NoMasAccidentes.security.JwtService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

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
