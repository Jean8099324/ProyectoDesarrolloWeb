package com.barbertime.controller;

import com.barbertime.model.Reserva;
import com.barbertime.model.Servicio;
import com.barbertime.model.Usuario;
import com.barbertime.repository.ReservaRepository;
import com.barbertime.repository.ServicioRepository;
import com.barbertime.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaRepository reservaRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;

    public ReservaController(ReservaRepository reservaRepository, ServicioRepository servicioRepository,
            UsuarioRepository usuarioRepository) {
        this.reservaRepository = reservaRepository;
        this.servicioRepository = servicioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/nueva")
    public String nuevaReserva(Model model) {
        cargarFormulario(model);
        return "nueva-reserva";
    }

    @PostMapping("/guardar")
    public String guardarReserva(@RequestParam Long servicioId, @RequestParam Long barberoId,
            @RequestParam String fechaHora, Principal principal, Model model) {
        LocalDateTime fecha;

        try {
            fecha = LocalDateTime.parse(fechaHora);
        } catch (Exception exception) {
            model.addAttribute("error", "La fecha y hora seleccionadas no son válidas.");
            cargarFormulario(model);
            return "nueva-reserva";
        }

        if (fecha.isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "No se puede reservar una cita en una fecha anterior.");
            cargarFormulario(model);
            return "nueva-reserva";
        }

        Usuario cliente = obtenerUsuarioAutenticado(principal);
        Usuario barbero = usuarioRepository.findById(barberoId)
                .orElseThrow(() -> new IllegalArgumentException("Barbero no encontrado"));

        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        if (!Boolean.TRUE.equals(servicio.getActivo())) {
            model.addAttribute("error", "El servicio seleccionado no está disponible.");
            cargarFormulario(model);
            return "nueva-reserva";
        }

        boolean ocupado = reservaRepository.existsByBarberoAndFechaHoraAndEstadoNot(barbero, fecha, "CANCELADA");

        if (ocupado) {
            model.addAttribute("error", "El barbero seleccionado ya tiene una cita en ese horario.");
            cargarFormulario(model);
            return "nueva-reserva";
        }

        Reserva reserva = new Reserva();
        reserva.setUsuario(cliente);
        reserva.setBarbero(barbero);
        reserva.setServicio(servicio);
        reserva.setFechaHora(fecha);
        reserva.setEstado("PENDIENTE");

        reservaRepository.save(reserva);

        return "redirect:/reservas/mis-reservas?exito=true";
    }

    @GetMapping("/mis-reservas")
    public String misReservas(Model model, Principal principal) {
        Usuario usuario = obtenerUsuarioAutenticado(principal);

        model.addAttribute("reservas", reservaRepository.findByUsuarioOrderByFechaHoraDesc(usuario));

        return "mis-reservas";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Usuario usuario = obtenerUsuarioAutenticado(principal);

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!reserva.getUsuario().getId().equals(usuario.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tienes autorización para cancelar esta reserva.");

            return "redirect:/reservas/mis-reservas";
        }

        if ("COMPLETADA".equals(reserva.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Una cita completada no puede cancelarse.");

            return "redirect:/reservas/mis-reservas";
        }

        reserva.setEstado("CANCELADA");
        reservaRepository.save(reserva);

        redirectAttributes.addFlashAttribute("mensaje", "Reserva cancelada correctamente.");

        return "redirect:/reservas/mis-reservas";
    }

    private Usuario obtenerUsuarioAutenticado(Principal principal) {
        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private void cargarFormulario(Model model) {
        model.addAttribute("servicios", servicioRepository.findByActivoTrueOrderByNombreAsc());

        model.addAttribute("barberos", usuarioRepository.findDistinctByRolesNombre("BARBERO"));
    }
}