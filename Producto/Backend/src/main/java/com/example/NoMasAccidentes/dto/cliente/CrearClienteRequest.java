package com.example.NoMasAccidentes.dto.cliente;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CrearClienteRequest(

    @NotBlank @Size(max = 200)
    String razonSocial,

    @NotBlank
    @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "RUT inválido. Formato esperado: 12345678-9")
    @Size(max = 12)
    String rut,

    @NotBlank @Size(max = 120)
    String nombreContacto,

    @NotBlank @Email @Size(max = 120)
    String email,

    @Size(max = 20)
    String telefono,

    @NotBlank @Size(max = 80)
    String rubro,

    @NotBlank @Size(max = 40)
    String plan,

    @PositiveOrZero
    Integer cantidadTrabajadores,

    Long idProfesional
) {}
