package com.example.NoMasAccidentes.dto.configuracion;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarConfiguracionEmpresaRequest(
        @NotBlank
        @Size(max = 200)
        String nombreEmpresa,

        @NotBlank
        @Pattern(regexp = "^\\d{7,8}-[\\dkK]$")
        String rut,

        @NotBlank
        @Email
        @Size(max = 120)
        String emailContacto,

        @Size(max = 20)
        String telefono,

        @Size(max = 200)
        String direccion,

        @Size(max = 80)
        String region
) {
}