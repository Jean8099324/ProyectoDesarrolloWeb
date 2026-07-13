package com.barbertime.controller.admin;

import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barbertime.model.Reserva;
import com.barbertime.repository.ReservaRepository;

@Controller
@RequestMapping("/admin/reservas")
public class AdminReservaController {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("PENDIENTE", "CONFIRMADA", "COMPLETADA", "CANCELADA");

    private final ReservaRepository reservaRepository;

    public AdminReservaController(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("reservas", reservaRepository.findAllByOrderByFechaHoraDesc());

        model.addAttribute("estados", ESTADOS_VALIDOS);

        return "admin/reservas";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable long id, @RequestParam String estado,
            RedirectAttributes redirectAttributes) {

        String estadoNormalizado = estado.toUpperCase();

        if (!ESTADOS_VALIDOS.contains(estadoNormalizado)) {
            redirectAttributes.addFlashAttribute("error", "El estado seleccionado no es válido.");

            return "redirect:/admin/reservas";
        }

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        reserva.setEstado(estadoNormalizado);
        reservaRepository.save(reserva);

        redirectAttributes.addFlashAttribute("mensaje", "Estado de la reserva actualizado.");

        return "redirect:/admin/reservas";
    }
}