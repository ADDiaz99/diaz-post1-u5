package com.universidad.reservaslabs.exception;

/** El laboratorio o la reserva referenciados no existen. REST: 404. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) { super(mensaje); }
}
