package com.example.NoMasAccidentes.dto.solicitud;

import com.example.NoMasAccidentes.model.solicitud.Solicitud;
import org.springframework.stereotype.Component;

@Component
public class SolicitudMapper {

    public SolicitudResponse toResponse(Solicitud s) {
        return toResponse(s, null);
    }

    /** {@code sugerenciaExtra} lo calcula el servicio (solo aplica a asesorías pendientes). */
    public SolicitudResponse toResponse(Solicitud s, Boolean sugerenciaExtra) {
        return new SolicitudResponse(
                s.getId(),
                s.getEmpresa().getId(),
                s.getEmpresa().getRazonSocial(),
                s.getTipo(),
                s.getEstado(),
                s.getDescripcion(),
                s.getFechaPreferida(),
                s.isEsExtra(),
                s.getRespuestaAdmin(),
                s.getFechaCreacion(),
                s.getFechaRespuesta(),
                sugerenciaExtra
        );
    }
}
