package com.example.NoMasAccidentes.dto.actividad;

import com.example.NoMasAccidentes.model.actividad.ActividadPreventiva;
import com.example.NoMasAccidentes.model.actividad.EstadoActividadPreventiva;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class ActividadPreventivaMapper {
    
    public ActividadPreventivaResponse toResponse(ActividadPreventiva a){
        boolean vencida = a.getEstado() != EstadoActividadPreventiva.CUMPLIDA
                && a.getFechaCompromiso().isBefore(LocalDate.now());

        return new ActividadPreventivaResponse(
                a.getId(),
                a.getEmpresa().getId(),
                a.getEmpresa().getRazonSocial(),
                a.getTitulo(),
                a.getDescripcion(),
                a.getNormativa(),
                a.getResponsable(),
                a.getFechaPlanificada(),
                a.getFechaCompromiso(),
                a.getFechaCumplimiento(),
                vencida ? EstadoActividadPreventiva.VENCIDA : a.getEstado(),
                a.getObservaciones(),
                vencida
        );
    }
}
