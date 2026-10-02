package com.universidad.reservaslabs.service;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.exception.ReservaConflictException;
import com.universidad.reservaslabs.exception.ReservaInvalidaException;
import com.universidad.reservaslabs.model.EstadoReserva;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Concentra las reglas de negocio de las reservas. Es la única puerta de
 * entrada a esas reglas: la usan tanto ReservaController (REST) como
 * ReservaWebController (Thymeleaf), por lo que ninguna regla se duplica.
 */
@Service
@Transactional
public class ReservaService {

    private static final LocalTime APERTURA = LocalTime.of(7, 0);
    private static final LocalTime CIERRE = LocalTime.of(21, 0);
    private static final Duration DURACION_MINIMA = Duration.ofMinutes(30);
    private static final Duration DURACION_MAXIMA = Duration.ofHours(3);

    private final ReservaRepository reservaRepo;
    private final LaboratorioRepository laboratorioRepo;

    public ReservaService(ReservaRepository reservaRepo, LaboratorioRepository laboratorioRepo) {
        this.reservaRepo = reservaRepo;
        this.laboratorioRepo = laboratorioRepo;
    }

    @Transactional(readOnly = true)
    public List<Reserva> findAll() {
        return reservaRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Reserva> findById(Long id) {
        return reservaRepo.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Reserva> findByLaboratorio(Long laboratorioId) {
        return reservaRepo.findByLaboratorioId(laboratorioId);
    }

    public Reserva crear(Reserva reserva) {
        if (reserva.getLaboratorio() == null || reserva.getLaboratorio().getId() == null) {
            throw new ReservaInvalidaException("Debe indicar el laboratorio a reservar");
        }
        Long laboratorioId = reserva.getLaboratorio().getId();
        Laboratorio laboratorio = laboratorioRepo.findById(laboratorioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Laboratorio no encontrado: " + laboratorioId));
        reserva.setLaboratorio(laboratorio);

        // Regla 1: depende solo de los campos de esta reserva. Java puro,
        // sin Repository (Punto de decisión 2).
        validarHorarioYDuracion(reserva.getInicio(), reserva.getFin());

        // Regla 2: necesita conocer las demás reservas. El Repository filtra
        // en la base de datos; la decisión de rechazar se toma aquí
        // (Punto de decisión 1).
        List<Reserva> solapamientos = reservaRepo.buscarSolapamientos(
                laboratorio.getId(), reserva.getInicio(), reserva.getFin());
        if (!solapamientos.isEmpty()) {
            throw new ReservaConflictException(
                    "El laboratorio " + laboratorio.getNombre() + " ya tiene una reserva en ese horario");
        }

        reserva.setId(null); // una reserva nueva nunca debe sobrescribir una existente
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        return reservaRepo.save(reserva);
    }

    public void cancelar(Long id) {
        Reserva reserva = reservaRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada: " + id));

        // Regla 3: solo se cancelan reservas activas cuyo horario no ha empezado.
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ReservaConflictException("La reserva ya está cancelada");
        }
        if (reserva.getInicio().isBefore(LocalDateTime.now())) {
            throw new ReservaConflictException(
                    "No se puede cancelar una reserva cuyo horario de inicio ya pasó");
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        reservaRepo.save(reserva);
    }

    private void validarHorarioYDuracion(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new ReservaInvalidaException("El rango de fecha y hora de la reserva es inválido");
        }
        if (inicio.isBefore(LocalDateTime.now())) {
            throw new ReservaInvalidaException("No se puede reservar en una fecha y hora que ya pasó");
        }
        // Sin esta comprobación, 22:30 a 01:00 del día siguiente pasaba la
        // validación: el inicio no es anterior a las 07:00 y el fin no es
        // posterior a las 21:00 si solo se miran las horas.
        if (!inicio.toLocalDate().equals(fin.toLocalDate())) {
            throw new ReservaInvalidaException("La reserva debe iniciar y terminar el mismo día");
        }
        Duration duracion = Duration.between(inicio, fin);
        if (duracion.compareTo(DURACION_MINIMA) < 0 || duracion.compareTo(DURACION_MAXIMA) > 0) {
            throw new ReservaInvalidaException(
                    "La duración de la reserva debe estar entre 30 minutos y 3 horas");
        }
        if (inicio.toLocalTime().isBefore(APERTURA) || fin.toLocalTime().isAfter(CIERRE)) {
            throw new ReservaInvalidaException(
                    "La reserva debe estar dentro del horario de atención (07:00 - 21:00)");
        }
    }
}
