package com.universidad.reservaslabs.web;

import com.universidad.reservaslabs.exception.RecursoNoEncontradoException;
import com.universidad.reservaslabs.exception.ReservaConflictException;
import com.universidad.reservaslabs.exception.ReservaInvalidaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Presentación HTML de las MISMAS excepciones de dominio que traduce
 * GlobalRestExceptionHandler a JSON. Restringido con assignableTypes a
 * ReservaWebController para no competir con el manejador REST.
 */
@ControllerAdvice(assignableTypes = ReservaWebController.class)
public class ReservaWebExceptionHandler {

    @ExceptionHandler(ReservaConflictException.class)
    public String conflicto(ReservaConflictException ex, HttpServletRequest request,
                            RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", ex.getMessage());
        // Un conflicto al cancelar (reserva ya iniciada o ya cancelada) nace
        // en la lista; mandarlo al formulario de nueva reserva confundiría al
        // usuario. Un conflicto al crear (solapamiento) vuelve al formulario.
        return esCancelacion(request) ? "redirect:/reservas" : "redirect:/reservas/nueva";
    }

    @ExceptionHandler(ReservaInvalidaException.class)
    public String invalida(ReservaInvalidaException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", ex.getMessage());
        return "redirect:/reservas/nueva";
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public String noEncontrado(RecursoNoEncontradoException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", ex.getMessage());
        return "redirect:/reservas";
    }

    private boolean esCancelacion(HttpServletRequest request) {
        return request.getRequestURI().endsWith("/cancelar");
    }
}
