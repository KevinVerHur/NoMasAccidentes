package com.example.NoMasAccidentes.dto.visita;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record CrearVisitaRequest(

        @NotNull(message = "El cliente es obligatorio")
        Long idCliente,

        @NotNull(message = "El profesional es obligatorio")
        Long idProfesional,

        @NotNull(message = "La fecha programada es obligatoria")
        @FutureOrPresent(message = "La fecha de la visita no puede estar en el pasado")
        LocalDateTime fechaProgramada,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 200, message = "La direccion no puede superar 200 caracteres")
        String direccion,

        @Size(max = 250, message = "El motivo no puede superar 250 caracteres")
        String motivo
) {}