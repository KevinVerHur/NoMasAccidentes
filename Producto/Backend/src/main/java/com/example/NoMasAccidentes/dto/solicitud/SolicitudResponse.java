package com.example.NoMasAccidentes.dto.solicitud;

import com.example.NoMasAccidentes.model.solicitud.EstadoSolicitud;
import com.example.NoMasAccidentes.model.solicitud.TipoSolicitud;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Solicitud tal como la consumen el portal cliente (tracking) y el admin (bandeja). */
public record SolicitudResponse(
        Long id,
        Long idEmpresa,
        String razonSocialEmpresa,
        TipoSolicitud tipo,
        EstadoSolicitud estado,
        String descripcion,
        LocalDate fechaPreferida,
        boolean esExtra,
        String respuestaAdmin,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaRespuesta,
        /** Sugerencia del sistema de si correspondería cobro extra (solo asesoría). */
        Boolean sugerenciaExtra
) {}
