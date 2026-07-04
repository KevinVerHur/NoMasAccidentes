package com.example.NoMasAccidentes.dto.consulta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearConsultaRequest (
    
    @NotNull(message = "La empresa es obligatoria")
    Long idEmpresa,

    /** Solo lo usa el admin para atribuir el llamado a un profesional. Un profesional
     *  que registra queda siempre atribuido a sí mismo (este campo se ignora). */
    Long idProfesional,

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 500, message = "El motivo no puede superar 500 caracteres")
    String motivo,

    @Size(max = 1000, message = "El detalle no puede superar los 1000 caracteres")
    String detalle

) {}
