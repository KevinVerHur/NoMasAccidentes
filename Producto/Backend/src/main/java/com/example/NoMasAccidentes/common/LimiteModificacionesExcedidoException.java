package com.example.NoMasAccidentes.common;

/**
 * La lista de chequeo solo permite 2 modificaciones
 * al año por cliente.
 * Traducida a HTTP 409 por GlobalExceptionHandler
 */

public class LimiteModificacionesExcedidoException extends RuntimeException {
    public LimiteModificacionesExcedidoException(String mensaje) {
        super(mensaje);
    }
}