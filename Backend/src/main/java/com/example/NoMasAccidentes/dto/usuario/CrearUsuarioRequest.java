package com.example.NoMasAccidentes.dto.usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del POST /api/usuarios.
 * Alta de usuarios del sistema.
 */
public record CrearUsuarioRequest(

    @NotBlank
    @Email
    @Size(max = 120)
    String email,

    // En texto plano: el service lo hashea con bcrypt antes de persistir.
    @NotBlank
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    String password,

    @NotBlank
    @Size(max = 120)
    String nombre,

    @NotBlank
    @Size(max = 120)
    String apellido,

    @NotNull
    Long idRol
) {}
