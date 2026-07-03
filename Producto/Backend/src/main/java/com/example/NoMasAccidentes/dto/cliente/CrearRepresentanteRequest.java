package com.example.NoMasAccidentes.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Alta de un representante (persona de contacto) de una empresa. Si
 * {@code conAcceso} es true, se le provisiona una cuenta rol CLIENTE y se le
 * envía la invitación por correo para que active su acceso al portal.
 */
public record CrearRepresentanteRequest(

    @NotBlank @Size(max = 120)
    String nombre,

    @Size(max = 80)
    String cargo,

    @NotBlank @Email @Size(max = 120)
    String email,

    @Size(max = 20)
    String telefono,

    boolean conAcceso
) {}
