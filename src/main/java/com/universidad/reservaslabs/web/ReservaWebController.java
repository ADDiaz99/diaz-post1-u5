package com.universidad.reservaslabs.web;

import com.universidad.reservaslabs.model.Reserva;
import com.universidad.reservaslabs.repository.LaboratorioRepository;
import com.universidad.reservaslabs.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Vista MVC clásica (Thymeleaf). Retorna nombres de vista, no JSON, por eso
 * es @Controller y no @RestController. Recibe el MISMO ReservaService que
 * ReservaController: ninguna regla de negocio se reimplementa aquí.
 */
@Controller
@RequestMapping("/reservas")
public class ReservaWebController {

    private final ReservaService service; // misma instancia singleton que usa la API REST
    private final LaboratorioRepository laboratorioRepo;

    public ReservaWebController(ReservaService service, LaboratorioRepository laboratorioRepo) {
        this.service = service;
        this.laboratorioRepo = laboratorioRepo;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("reservas", service.findAll());
        return "reservas/lista";
    }

    @GetMapping("/nueva")
    public String formularioNueva(Model model) {
        model.addAttribute("reserva", new Reserva());
        model.addAttribute("laboratorios", laboratorioRepo.findAll());
        return "reservas/nueva";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("reserva") Reserva reserva, BindingResult resultado,
                        Model model, RedirectAttributes redirect) {
        // Errores de formato del formulario (campos vacíos, correo mal
        // escrito): se vuelve a mostrar el formulario con lo que el usuario
        // ya había digitado. Sin este paso terminaban en un error 500 al
        // guardar, porque las mismas anotaciones se validan en el persist.
        if (resultado.hasErrors()) {
            model.addAttribute("laboratorios", laboratorioRepo.findAll());
            return "reservas/nueva";
        }
        service.crear(reserva); // MISMAS reglas de solapamiento, horario y duración que la API REST
        redirect.addFlashAttribute("mensaje", "Reserva creada correctamente");
        return "redirect:/reservas";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirect) {
        service.cancelar(id);
        redirect.addFlashAttribute("mensaje", "Reserva cancelada correctamente");
        return "redirect:/reservas";
    }
}
