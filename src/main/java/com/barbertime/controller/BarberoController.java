package com.barbertime.controller;

import com.barbertime.model.Usuario;
import com.barbertime.repository.ReservaRepository;
import com.barbertime.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/barbero")
public class BarberoController {

    private final UsuarioRepository usuarioRepository;
    private final ReservaRepository reservaRepository;

    public BarberoController(UsuarioRepository usuarioRepository, ReservaRepository reservaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.reservaRepository = reservaRepository;
    }

    @GetMapping("/citas")
    public String misCitas(Model model, Principal principal) {
        Usuario barbero = usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Barbero no encontrado"));

        model.addAttribute("reservas", reservaRepository.findByBarberoOrderByFechaHoraAsc(barbero));

        return "barbero/citas";
    }
}