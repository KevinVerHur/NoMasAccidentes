package com.example.NoMasAccidentes.dto.asistente;

import jakarta.validation.constraints.*;

public record AsistenteRequest(

        @NotNull(message = "El cliente es obligatorio")
        Long idCliente,

        @NotBlank(message = "El RUT es obligatorio")
        @Size(max = 12, message = "RUT inválido")
        String rut,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80)
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 120)
        String apellidos,

        @Size(max = 80)
        String cargo,

        @Size(max = 80)
        String area,

        @Email(message = "Email inválido")
        @Size(max = 120)
        String email
) {}
