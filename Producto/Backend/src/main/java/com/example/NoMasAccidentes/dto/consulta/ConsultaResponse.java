package com.example.NoMasAccidentes.dto.consulta;

import java.time.LocalDateTime; 

public record ConsultaResponse (
    Long id, 
    Long idCliente,
    String cliente, 
    LocalDateTime fechaHora,
    String motivo, 
    String detalle,
    boolean fueraHorario, 
    boolean costoAdicional
) {}
