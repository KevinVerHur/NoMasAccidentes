package com.example.NoMasAccidentes.dto.profesional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del POST /api/profesionales.
 * Registro de profesionales de prevención de riesgos.
 * El profesional debe estar asociado a un Usuario existente con rol PROFESIONAL.
 */
public record CrearProfesionalRequest(

    @NotNull
    Long idUsuario,

    // Formato RUT chileno: 7 u 8 dígitos, guion, dígito verificador (0-9, k o K).
    @NotBlank
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "RUT inválido. Formato esperado: 12345678-9")
    @Size(max = 12)
    String rut,

    @Size(max = 20)
    String telefono,

    @Size(max = 120)
    String especialidad
) {}
