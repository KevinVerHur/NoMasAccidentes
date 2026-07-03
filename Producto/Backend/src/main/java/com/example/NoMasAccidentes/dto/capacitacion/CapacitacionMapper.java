package com.example.NoMasAccidentes.dto.capacitacion;

import com.example.NoMasAccidentes.model.asistente.Asistente;
import com.example.NoMasAccidentes.model.asistencia.Asistencia;
import com.example.NoMasAccidentes.model.capacitacion.Capacitacion;
import com.example.NoMasAccidentes.model.profesional.Profesional;
import com.example.NoMasAccidentes.model.usuario.Usuario;
import org.springframework.stereotype.Component;

@Component
public class CapacitacionMapper {

    public CapacitacionResponse toResponse(Capacitacion c) {
        Profesional relator = c.getRelator();
        Usuario usuarioRelator = relator.getUsuario();
        String nombreRelator = usuarioRelator.getNombre() + " " + usuarioRelator.getApellido();

        int inscritos = c.getAsistencias().size();
        int cuposDisponibles = Math.max(0, c.getCupos() - inscritos);

        return new CapacitacionResponse(
                c.getId(),
                c.getEmpresa().getId(),
                c.getEmpresa().getRazonSocial(),
                c.getCurso(),
                relator.getId(),
                nombreRelator,
                c.getFechaProgramada(),
                c.getHoraProgramada().toString(),
                c.getLugar(),
                c.getCupos(),
                c.getObjetivo(),
                c.getFechaRealizacion(),
                c.getEstado(),
                c.isEsCapacitacionExtra(),
                c.getObservacionActa(),
                cuposDisponibles,
                c.getAsistencias().stream().map(this::toAsistenciaResponse).toList()
        );
    }

    public AsistenciaResponse toAsistenciaResponse(Asistencia a) {
        Asistente asistente = a.getAsistente();
        return new AsistenciaResponse(
                a.getId(),
                asistente.getId(),
                asistente.getNombre() + " " + asistente.getApellidos(),
                asistente.getRut(),
                asistente.getCargo(),
                a.isConfirmado(),
                a.isAsistio(),
                a.getFechaConfirmacion(),
                a.getObservaciones(),
                a.getFirmaDigital()
        );
    }
}
