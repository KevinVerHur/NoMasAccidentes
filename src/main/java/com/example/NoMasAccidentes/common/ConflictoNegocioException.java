package com.example.NoMasAccidentes.common;

/**
 * Se lanza cuando una operacion viola una regla de negocio
 * (ejemplo: RUT repetido, intentar pagar una mensualidad ya pagada),
 * Traducida a HTTP 409 por GlobalExceptionHandler.
 */
public class ConflictoNegocioException extends RuntimeException {

    public ConflictoNegocioException(String mensaje) {
        super(mensaje);
    }
}