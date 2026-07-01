package com.example.NoMasAccidentes.dto.empresa;

import com.example.NoMasAccidentes.model.empresa.EstadoEmpresa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ActualizarEmpresaRequest(

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

    @NotNull
    EstadoEmpresa estado,

    Long idProfesional
) {}
