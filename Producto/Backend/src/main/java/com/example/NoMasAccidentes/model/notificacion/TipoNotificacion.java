package com.example.NoMasAccidentes.model.notificacion;

/**
 * Tipos de notificación in-app. Cada valor corresponde a un evento del sistema
 * que además dispara un correo transaccional (ver NotificacionEventoService).
 */
public enum TipoNotificacion {
    /** Se planificó una visita para el profesional asignado (RF13). */
    VISITA_PLANIFICADA,
    /** Se programó una capacitación para la empresa cliente (RF-CAP1). */
    CAPACITACION_PROGRAMADA,
    /** Se registró una asesoría para la empresa cliente (RF22). */
    ASESORIA_REGISTRADA,
    /** Un cliente envió una solicitud de servicio (aviso al admin). */
    SOLICITUD_RECIBIDA,
    /** El admin respondió (aprobó/rechazó) una solicitud del cliente. */
    SOLICITUD_RESPONDIDA,
    /** Una actividad preventiva del cliente venció sin cumplirse (aviso al cliente). */
    ACTIVIDAD_VENCIDA,
    /** El cliente reportó que cumplió su parte de una actividad (aviso a la consultora). */
    CUMPLIMIENTO_REPORTADO,
    /** El cliente pagó una cuota (aviso al representante, con comprobante adjunto en el correo) (RF09). */
    PAGO_REALIZADO
}
