package com.example.NoMasAccidentes.dto.empresa;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Alta de una empresa cliente junto con su primer representante (persona de
 * contacto con acceso al portal). Un solo formulario crea ambas cosas: la
 * empresa (persona jurídica) y el representante, sobre quien se provisiona el
 * login rol CLIENTE y se dispara la invitación por correo.
 */
public record CrearEmpresaRequest(

    // ----- Datos de la empresa -----
    @NotBlank @Size(max = 200)
    String razonSocial,

    @NotBlank
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "RUT inválido. Formato esperado: 12345678-9")
    @Size(max = 12)
    String rut,

    @Size(max = 200)
    String direccion,

    @Size(max = 80)
    String comuna,

    @NotNull(message = "Debe indicar el rubro de la empresa")
    Long idRubro,

    @NotBlank @Size(max = 40)
    String plan,

    @PositiveOrZero
    Integer cantidadTrabajadores,

    Long idProfesional,

    // ----- Datos del primer representante (contacto) -----
    @NotBlank @Size(max = 120)
    String nombreContacto,

    @Size(max = 80)
    String cargoContacto,

    @NotBlank @Email @Size(max = 120)
    String email,

    @Size(max = 20)
    String telefono
) {}
