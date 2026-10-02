package com.universidad.reservaslabs.service;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.exception.ReservaConflictException;
import com.universidad.reservaslabs.exception.ReservaInvalidaException;
import com.universidad.reservaslabs.model.EstadoReserva;
import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba las reglas de negocio directamente sobre ReservaService, sin HTTP:
 * demuestra que la capa de servicio no es un passthrough del Repository.
 */
@SpringBootTest
@Transactional
class ReservaServiceTest {

    @Autowired
    private ReservaService service;

    @Autowired
    private LaboratorioRepository laboratorioRepo;

    @Autowired
    private ReservaRepository reservaRepo;

    private Laboratorio lab;
    private LocalDate dia; // siempre una fecha futura, para que las pruebas no caduquen

    @BeforeEach
    void preparar() {
        lab = laboratorioRepo.save(new Laboratorio(null, "Lab. Cómputo 3", "Bloque B, piso 2", 30, "COMPUTO"));
        dia = LocalDate.now().plusDays(7);
    }

    private Reserva nueva(int horaInicio, int minInicio, int horaFin, int minFin) {
        Reserva r = new Reserva();
        r.setLaboratorio(new Laboratorio(lab.getId(), null, null, null, null));
        r.setNombreSolicitante("Ana Torres");
        r.setCorreoSolicitante("ana@udes.edu.co");
        r.setInicio(dia.atTime(horaInicio, minInicio));
        r.setFin(dia.atTime(horaFin, minFin));
        r.setMotivo("Práctica");
        return r;
    }

    @Test
    void reservaValidaQuedaConfirmada() {
        Reserva creada = service.crear(nueva(9, 0, 11, 0));
        assertNotNull(creada.getId());
        assertEquals(EstadoReserva.CONFIRMADA, creada.getEstado());
    }

    @Test
    void reservaSolapadaSeRechazaConConflicto() {
        service.crear(nueva(9, 0, 11, 0));
        ReservaConflictException ex = assertThrows(ReservaConflictException.class,
                () -> service.crear(nueva(10, 0, 12, 0)));
        assertEquals("El laboratorio Lab. Cómputo 3 ya tiene una reserva en ese horario", ex.getMessage());
    }

    @Test
    void reservasContiguasNoSeConsideranSolapadas() {
        service.crear(nueva(9, 0, 10, 0));
        assertDoesNotThrow(() -> service.crear(nueva(10, 0, 11, 0)));
    }

    @Test
    void unaReservaCanceladaLiberaElHorario() {
        Reserva primera = service.crear(nueva(9, 0, 11, 0));
        service.cancelar(primera.getId());
        assertDoesNotThrow(() -> service.crear(nueva(9, 0, 11, 0)));
    }

    @Test
    void fueraDelHorarioDeAtencionEsInvalida() {
        ReservaInvalidaException ex = assertThrows(ReservaInvalidaException.class,
                () -> service.crear(nueva(6, 0, 7, 30)));
        assertTrue(ex.getMessage().contains("horario de atención"));
    }

    @Test
    void duracionFueraDeRangoEsInvalida() {
        assertThrows(ReservaInvalidaException.class, () -> service.crear(nueva(9, 0, 9, 15)));
        assertThrows(ReservaInvalidaException.class, () -> service.crear(nueva(9, 0, 13, 0)));
    }

    @Test
    void reservaQueCruzaLaMedianocheEsInvalida() {
        Reserva r = nueva(20, 0, 21, 0);
        r.setInicio(dia.atTime(22, 30));
        r.setFin(dia.plusDays(1).atTime(1, 0));
        assertThrows(ReservaInvalidaException.class, () -> service.crear(r));
    }

    @Test
    void reservaEnElPasadoEsInvalida() {
        Reserva r = nueva(9, 0, 10, 0);
        r.setInicio(LocalDate.now().minusDays(1).atTime(9, 0));
        r.setFin(LocalDate.now().minusDays(1).atTime(10, 0));
        assertThrows(ReservaInvalidaException.class, () -> service.crear(r));
    }

    @Test
    void laboratorioInexistenteNoSeEncuentra() {
        Reserva r = nueva(9, 0, 10, 0);
        r.setLaboratorio(new Laboratorio(999_999L, null, null, null, null));
        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(r));
    }

    @Test
    void noSePuedeCancelarUnaReservaQueYaInicio() {
        Reserva pasada = new Reserva(null, lab, "Luis Gómez", "luis@udes.edu.co",
                LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1),
                "Proyecto", EstadoReserva.CONFIRMADA);
        pasada = reservaRepo.save(pasada); // se guarda directo: el Service no permitiría crearla
        Long id = pasada.getId();
        assertThrows(ReservaConflictException.class, () -> service.cancelar(id));
    }

    @Test
    void noSePuedeCancelarDosVeces() {
        Reserva r = service.crear(nueva(9, 0, 10, 0));
        service.cancelar(r.getId());
        assertThrows(ReservaConflictException.class, () -> service.cancelar(r.getId()));
    }
}
