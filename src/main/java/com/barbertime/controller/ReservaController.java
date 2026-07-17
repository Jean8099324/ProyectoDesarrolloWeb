package com.barbertime.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barbertime.model.Reserva;
import com.barbertime.model.Servicio;
import com.barbertime.model.Usuario;
import com.barbertime.repository.ReservaRepository;
import com.barbertime.repository.ServicioRepository;
import com.barbertime.repository.UsuarioRepository;

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
    public String guardarReserva(@RequestParam long servicioId, @RequestParam long barberoId,
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

        boolean esBarbero = barbero.getRoles().stream().anyMatch(rol -> "BARBERO".equalsIgnoreCase(rol.getNombre()));

        if (!esBarbero) {
            model.addAttribute("error", "El usuario seleccionado no es un barbero.");

            cargarFormulario(model);
            return "nueva-reserva";
        }

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

        
        boolean esBarbero = usuario.getRoles().stream()
                .anyMatch(rol -> "BARBERO".equalsIgnoreCase(rol.getNombre()));

        if (esBarbero) {
           
            model.addAttribute("reservas", reservaRepository.findByBarberoOrderByFechaHoraDesc(usuario));
        } else {
            
            model.addAttribute("reservas", reservaRepository.findByUsuarioOrderByFechaHoraDesc(usuario));
        }

        return "mis-reservas";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable long id, Principal principal, RedirectAttributes redirectAttributes) {

        Usuario usuario = obtenerUsuarioAutenticado(principal);

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        if (!Objects.equals(reserva.getUsuario().getId(), usuario.getId())) {

            redirectAttributes.addFlashAttribute("error", "No tienes autorización para cancelar esta reserva.");

            return "redirect:/reservas/mis-reservas";
        }

        if ("COMPLETADA".equalsIgnoreCase(reserva.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Una cita completada no puede cancelarse.");

            return "redirect:/reservas/mis-reservas";
        }

        if ("CANCELADA".equalsIgnoreCase(reserva.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "La reserva ya se encuentra cancelada.");

            return "redirect:/reservas/mis-reservas";
        }

        reserva.setEstado("CANCELADA");
        reservaRepository.save(reserva);

        redirectAttributes.addFlashAttribute("mensaje", "Reserva cancelada correctamente.");

        return "redirect:/reservas/mis-reservas";
    }

    private Usuario obtenerUsuarioAutenticado(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("No existe un usuario autenticado.");
        }

        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    private void cargarFormulario(Model model) {
        model.addAttribute("servicios", servicioRepository.findByActivoTrueOrderByNombreAsc());

        model.addAttribute("barberos", usuarioRepository.findDistinctByRolesNombre("BARBERO"));
    }
}