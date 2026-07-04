package com.example.NoMasAccidentes.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del PUT /api/empresas/me/contacto. El representante edita solo sus
 * datos de contacto; el email (credencial) y los datos de la empresa no se
 * tocan desde aquí (los gestiona la consultora).
 */
public record ActualizarMiContactoRequest(

    @NotBlank
    @Size(max = 120)
    String nombre,

    @Size(max = 80)
    String cargo,

    @Size(max = 20)
    String telefono
) {}
