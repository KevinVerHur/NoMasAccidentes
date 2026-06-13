package com.example.NoMasAccidentes.model.visita;

/**
 * Estados del ciclo de vida de una visita.
 * PROGRAMADA: planificada (RF13), aún no realizada.
 * EN_CURSO:   el profesional inició la visita en terreno (fecha_inicio).
 * REALIZADA:  visita completada (RF14, fecha_fin).
 * CANCELADA:  no se llevó a cabo.
 */
public enum EstadoVisita {
    PROGRAMADA,
    EN_CURSO,
    REALIZADA,
    CANCELADA
}
