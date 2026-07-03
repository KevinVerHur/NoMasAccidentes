package com.example.NoMasAccidentes.dto.notificacion;

import com.example.NoMasAccidentes.model.notificacion.Notificacion;
import org.springframework.stereotype.Component;

@Component
public class NotificacionMapper {

    public NotificacionResponse toResponse(Notificacion n) {
        return new NotificacionResponse(
                n.getId(),
                n.getTipo(),
                n.getTitulo(),
                n.getMensaje(),
                n.getEnlace(),
                n.isLeida(),
                n.getFechaCreacion(),
                n.getFechaLeida()
        );
    }
}
