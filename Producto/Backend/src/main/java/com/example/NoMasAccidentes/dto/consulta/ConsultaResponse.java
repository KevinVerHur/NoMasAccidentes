package com.example.NoMasAccidentes.dto.consulta;

import java.time.LocalDateTime; 

public record ConsultaResponse (
    Long id,
    Long idEmpresa,
    String razonSocialEmpresa,
    LocalDateTime fechaHora,
    String motivo, 
    String detalle,
    boolean fueraHorario, 
    boolean costoAdicional
) {}
