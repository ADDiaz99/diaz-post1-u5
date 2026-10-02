package com.universidad.reservaslabs.controller;

import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica los códigos HTTP que exigen los checkpoints del Paso 9:
 * 201 (horario libre), 409 (solapamiento) y 400 (horario o duración).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservaRestApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private LaboratorioRepository laboratorioRepo;

    private Long labId;
    private LocalDate dia;

    @BeforeEach
    void preparar() {
        labId = laboratorioRepo.save(new Laboratorio(null, "Lab. Redes 1", "Bloque A, piso 1", 20, "REDES")).getId();
        dia = LocalDate.now().plusDays(7);
    }

    private String json(String nombre, String inicio, String fin) {
        return """
                {"laboratorio":{"id":%d},"nombreSolicitante":"%s","correoSolicitante":"test@udes.edu.co",
                 "inicio":"%s","fin":"%s","motivo":"Práctica"}
                """.formatted(labId, nombre, inicio, fin);
    }

    @Test
    void horarioLibreRetorna201() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ana Torres", dia + "T09:00:00", dia + "T11:00:00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    void solapamientoRetorna409ConMensaje() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Ana Torres", dia + "T09:00:00", dia + "T11:00:00")))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Luis Gómez", dia + "T10:00:00", dia + "T12:00:00")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("El laboratorio Lab. Redes 1 ya tiene una reserva en ese horario"));
    }

    @Test
    void fueraDeHorarioRetorna400() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Carlos Ruiz", dia + "T20:00:00", dia + "T22:00:00")))
                .andExpect(status().isBadRequest())
                // containsString evita depender de cómo se decodifican las tildes de "atención"
                .andExpect(jsonPath("$.error").value(containsString("dentro del horario")));
    }

    @Test
    void campoObligatorioVacioRetorna400ConErrores() throws Exception {
        mvc.perform(post("/api/reservas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("", dia + "T09:00:00", dia + "T10:00:00")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores").isArray());
    }
}
