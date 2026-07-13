package com.barbertime.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.barbertime.repository.ServicioRepository;

@Controller
public class PageController {

    private final ServicioRepository servicioRepository;

    public PageController(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        
        model.addAttribute("servicios", servicioRepository.findByActivoTrueOrderByNombreAsc());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "register";
    }
}
