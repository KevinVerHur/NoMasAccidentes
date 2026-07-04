package com.example.NoMasAccidentes.model.visita;

/**
 * Resultado de un ítem de la lista de chequeo en una visita, siguiendo el
 * modelo SI/NO/NC de la lista de autoverificación de la Dirección del Trabajo.
 */
public enum EstadoCumplimiento {
    /** Cumple la condición evaluada (SI). */
    CUMPLE,
    /** No cumple; genera propuesta de mejora (NO). */
    NO_CUMPLE,
    /** No corresponde / no aplica a esta empresa (NC). */
    NO_APLICA
}
