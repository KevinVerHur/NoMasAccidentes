package com.example.NoMasAccidentes.common;

/**
 * Se lanza cuando un recurso solicitado por id ( o identificador unico) no existe.
 * Traducida por HTTP 404 por GlobalExceptionHandler.
 */

public class RecursoNoEncontradoException extends RuntimeException{

    public RecursoNoEncontradoException(String mensaje){
        super(mensaje);
    }

    public RecursoNoEncontradoException(String recurso, Object id){
        super("%s no encontrado con id=%s".formatted(recurso, id));
    }
}