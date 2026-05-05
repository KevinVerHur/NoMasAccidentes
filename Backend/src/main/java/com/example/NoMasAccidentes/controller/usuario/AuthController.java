package com.example.NoMasAccidentes.controller.usuario;

import com.example.NoMasAccidentes.dto.usuario.CrearUsuarioRequest;
import com.example.NoMasAccidentes.dto.usuario.LoginRequest;
import com.example.NoMasAccidentes.dto.usuario.LoginResponse;
import com.example.NoMasAccidentes.service.usuario.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
