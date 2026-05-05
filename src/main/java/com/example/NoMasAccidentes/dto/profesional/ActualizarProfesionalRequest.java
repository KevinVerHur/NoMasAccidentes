package com.example.NoMasAccidentes.dto.profesional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del PUT /api/profesionales/{id}.
 * No se permite cambiar el Usuario asociado: si se requiere, eliminar y crear de nuevo.
 */
public record ActualizarProfesionalRequest(

    @NotBlank
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "RUT inválido. Formato esperado: 12345678-9")
    @Size(max = 12)
    String rut,

    @Size(max = 20)
    String telefono,

    @Size(max = 120)
    String especialidad
) {}
