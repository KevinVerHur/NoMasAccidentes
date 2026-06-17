package com.example.NoMasAccidentes.dto.profesional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registro unificado de profesional: crea el Usuario con rol PROFESIONAL
 * y el Profesional en una sola operación (RF03).
 */
public record RegistrarProfesionalRequest(

    @NotBlank
    @Email
    @Size(max = 120)
    String email,

    @NotBlank
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
        message = "La contraseña debe incluir al menos una mayúscula, un número y un símbolo")
    String password,

    @NotBlank
    @Size(max = 120)
    String nombre,

    @NotBlank
    @Size(max = 120)
    String apellido,

    @NotBlank
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "RUT inválido. Formato esperado: 12345678-9")
    @Size(max = 12)
    String rut,

    @Size(max = 120)
    String especialidad,

    @Size(max = 20)
    String telefono
) {}
