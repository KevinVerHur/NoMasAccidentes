package com.example.NoMasAccidentes.controller.usuario;

import com.example.NoMasAccidentes.dto.usuario.CrearUsuarioRequest;
import com.example.NoMasAccidentes.dto.usuario.LoginRequest;
import com.example.NoMasAccidentes.dto.usuario.LoginResponse;
import com.example.NoMasAccidentes.dto.usuario.RestablecerPasswordRequest;
import com.example.NoMasAccidentes.dto.usuario.SolicitarRecuperacionRequest;
import com.example.NoMasAccidentes.dto.usuario.ValidacionTokenResponse;
import com.example.NoMasAccidentes.service.usuario.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de autenticación.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login y emisión de tokens JWT ")
public class AuthController {

    private final AuthService authService;
    @Operation(
            summary = "Iniciar sesión ",
            description = "Autentica al usuario con sus credenciales y retorna un token JWT listo para usar."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea una cuenta y devuelve un token JWT listo para usar. El rol ADMIN (id=1) no está permitido.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Registro exitoso, token generado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o intento de registro como ADMIN"),
            @ApiResponse(responseCode = "409", description = "El email ya está registrado")
        }
    )
    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(@Valid @RequestBody CrearUsuarioRequest request) {
        return ResponseEntity.ok(authService.registrar(request));
    }

    @Operation(
        summary = "Solicitar recuperación de contraseña",
        description = "Envía un correo con un enlace de restablecimiento válido por 1 hora. " +
                      "Siempre responde 200 para no revelar si el email existe.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Correo enviado (o email no registrado, respuesta idéntica)")
        }
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> solicitarRecuperacion(@Valid @RequestBody SolicitarRecuperacionRequest request) {
        authService.solicitarRecuperacion(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Restablecer contraseña",
        description = "Valida el token recibido por correo y actualiza la contraseña.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "404", description = "Token inválido"),
            @ApiResponse(responseCode = "409", description = "Token expirado o ya usado")
        }
    )
    @PostMapping("/reset-password")
    public ResponseEntity<Void> restablecerPassword(@Valid @RequestBody RestablecerPasswordRequest request) {
        authService.restablecerPassword(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "Validar token de restablecimiento",
        description = "Indica si el enlace de restablecimiento sigue vigente. " +
                      "Permite avisar al usuario apenas abre la página si el enlace ya caducó o fue usado.",
        responses = {
            @ApiResponse(responseCode = "200", description = "valido=true si el token está vigente; valido=false en caso contrario")
        }
    )
    @GetMapping("/reset-password/validate")
    public ResponseEntity<ValidacionTokenResponse> validarToken(@RequestParam String token) {
        return ResponseEntity.ok(new ValidacionTokenResponse(authService.tokenEsVigente(token)));
    }
}
