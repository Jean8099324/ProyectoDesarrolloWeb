package com.barbertime.controller;

import com.barbertime.model.Reserva;
import com.barbertime.model.Servicio;
import com.barbertime.model.Usuario;
import com.barbertime.repository.ReservaRepository;
import com.barbertime.repository.ServicioRepository;
import com.barbertime.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/nueva")
    public String nuevaReserva(Model model) {
        model.addAttribute("servicios", servicioRepository.findAll());
        return "nueva-reserva";
    }

    @PostMapping("/guardar")
    public String guardarReserva(@RequestParam Long servicioId,
                                 @RequestParam String fechaHora,
                                 Principal principal,
                                 Model model) {

        LocalDateTime fecha = LocalDateTime.parse(fechaHora);

        if (reservaRepository.existsByFechaHoraAndEstadoNot(fecha, "CANCELADA")) {
            model.addAttribute("error", "Ese horario ya está ocupado");
            model.addAttribute("servicios", servicioRepository.findAll());
            return "nueva-reserva";
        }

        Usuario usuario = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
        Servicio servicio = servicioRepository.findById(servicioId).orElseThrow();

        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setServicio(servicio);
        reserva.setFechaHora(fecha);
        reserva.setEstado("PENDIENTE");

        reservaRepository.save(reserva);

        return "redirect:/reservas/mis-reservas?exito";
    }

    @GetMapping("/mis-reservas")
    public String misReservas(Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("reservas", reservaRepository.findByUsuarioOrderByFechaHoraDesc(usuario));
        return "mis-reservas";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id) {
        Reserva reserva = reservaRepository.findById(id).orElseThrow();
        reserva.setEstado("CANCELADA");
        reservaRepository.save(reserva);
        return "redirect:/reservas/mis-reservas";
    }
}