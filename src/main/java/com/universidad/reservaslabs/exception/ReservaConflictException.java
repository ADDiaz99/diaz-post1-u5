package com.universidad.reservaslabs.exception;

/**
 * La solicitud es válida en sí misma, pero choca con el estado actual del
 * sistema: horario ya ocupado, reserva ya cancelada o ya iniciada. REST: 409.
 */
public class ReservaConflictException extends RuntimeException {
    public ReservaConflictException(String mensaje) { super(mensaje); }
}
