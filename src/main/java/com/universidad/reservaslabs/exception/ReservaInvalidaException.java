package com.universidad.reservaslabs.exception;

/**
 * La reserva viola una regla que depende solo de sus propios datos (horario de
 * atención, duración, rango de fechas), sin importar qué más exista en la
 * base de datos. REST: 400.
 *
 * Se separa de ReservaConflictException porque la guía exige 400 para una
 * reserva fuera de horario y 409 para un solapamiento: con una sola
 * excepción, ambas terminaban en el mismo código HTTP.
 */
public class ReservaInvalidaException extends RuntimeException {
    public ReservaInvalidaException(String mensaje) { super(mensaje); }
}
