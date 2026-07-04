package com.example.NoMasAccidentes.model.solicitud;

/** Tipo de recurso que el cliente solicita desde el portal. */
public enum TipoSolicitud {
    ASESORIA,
    CAPACITACION,
    VISITA,
    /** Reporte de accidente del cliente; al aprobar crea una asesoría tipo ACCIDENTE (urgente). */
    ACCIDENTE
}
