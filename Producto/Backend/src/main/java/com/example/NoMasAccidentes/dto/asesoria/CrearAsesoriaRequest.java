package com.example.NoMasAccidentes.dto.asesoria;

import com.example.NoMasAccidentes.model.asesoria.TipoAsesoria;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearAsesoriaRequest(

    @NotNull(message = "El cliente es obligatorio")
    Long idCliente,

    @NotNull(message = "El profesional es obligatorio")
    Long idProfesional,

    @NotNull(message = "El tipo de asesoría es obligatorio (ACCIDENTE o FISCALIZACION)")
    TipoAsesoria tipo,

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    String motivo
) {}
