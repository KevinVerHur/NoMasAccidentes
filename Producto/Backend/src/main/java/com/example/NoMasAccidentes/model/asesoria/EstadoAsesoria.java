package com.example.NoMasAccidentes.model.asesoria;

/**
 * Estados del ciclo de vida de una asesoría (RF22–RF25).
 * SOLICITADA: el admin registró la solicitud (RF22), aún sin atender.
 * EN_PROCESO: el profesional inició la atención (RF25); fecha_atencion fijada.
 * CERRADA:    asesoría finalizada.
 * CANCELADA:  no se llevó a cabo.
 */
public enum EstadoAsesoria {
    SOLICITADA,
    EN_PROCESO,
    CERRADA,
    CANCELADA
}
