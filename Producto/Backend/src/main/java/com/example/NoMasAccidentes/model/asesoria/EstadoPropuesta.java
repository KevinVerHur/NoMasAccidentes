package com.example.NoMasAccidentes.model.asesoria;

/**
 * Estado de una propuesta de mejora (RF25).
 * PENDIENTE: registrada, sin iniciar.
 * EN_PROCESO: en implementación por el cliente.
 * VERIFICADA: el profesional confirmó su implementación (fecha_verificacion).
 * DESCARTADA: no se llevará a cabo.
 */
public enum EstadoPropuesta {
    PENDIENTE,
    EN_PROCESO,
    VERIFICADA,
    DESCARTADA
}
