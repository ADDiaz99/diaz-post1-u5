package com.universidad.reservaslabs.web;

import com.universidad.reservaslabs.model.Laboratorio;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifica la Parte 2: la vista Thymeleaf usa el mismo ReservaService que la
 * API REST, y un solapamiento produce exactamente el mismo mensaje de negocio,
 * presentado como redirección con mensaje en vez de JSON (Punto de decisión 4).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservaWebControllerTest {

    private static final String MENSAJE_SOLAPAMIENTO =
            "El laboratorio Lab. Multimedia ya tiene una reserva en ese horario";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private LaboratorioRepository laboratorioRepo;

    private Long labId;
    private LocalDate dia;

    @BeforeEach
    void preparar() {
        labId = laboratorioRepo.save(new Laboratorio(null, "Lab. Multimedia", "Bloque C, piso 3", 25, "MULTIMEDIA")).getId();
        dia = LocalDate.now().plusDays(7);
    }

    // El input datetime-local del navegador envía "yyyy-MM-ddTHH:mm", sin segundos.
    private MockHttpServletRequestBuilder formulario(String nombre, String inicio, String fin) {
        return post("/reservas")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("laboratorio.id", labId.toString())
                .param("nombreSolicitante", nombre)
                .param("correoSolicitante", "test@udes.edu.co")
                .param("inicio", inicio)
                .param("fin", fin)
                .param("motivo", "Proyecto");
    }

    @Test
    void listaYFormularioSeRenderizan() throws Exception {
        mvc.perform(get("/reservas")).andExpect(status().isOk()).andExpect(view().name("reservas/lista"));
        mvc.perform(get("/reservas/nueva")).andExpect(status().isOk()).andExpect(view().name("reservas/nueva"));
    }

    @Test
    void reservaValidaRedirigeALaListaConMensaje() throws Exception {
        mvc.perform(formulario("Ana Torres", dia + "T09:00", dia + "T11:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas"))
                .andExpect(flash().attribute("mensaje", "Reserva creada correctamente"));
    }

    @Test
    void solapamientoMuestraElMismoMensajeQueLaApiRest() throws Exception {
        mvc.perform(formulario("Ana Torres", dia + "T09:00", dia + "T11:00"))
                .andExpect(redirectedUrl("/reservas"));

        mvc.perform(formulario("Luis Gómez", dia + "T10:00", dia + "T12:00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reservas/nueva"))
                .andExpect(flash().attribute("error", MENSAJE_SOLAPAMIENTO));
    }

    @Test
    void campoVacioVuelveAlFormularioSinErrorDeServidor() throws Exception {
        mvc.perform(formulario("", dia + "T09:00", dia + "T10:00"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservas/nueva"))
                .andExpect(model().attributeHasFieldErrors("reserva", "nombreSolicitante"));
    }
}
