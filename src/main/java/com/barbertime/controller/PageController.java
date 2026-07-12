package com.barbertime.controller;

import com.barbertime.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @Autowired
    private ServicioRepository servicioRepository;

    @GetMapping("/")
    public String home(Model model) {
        // Trae los servicios reales desde MySQL.
        // Si la tabla esta vacia, la vista muestra servicios de ejemplo.
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
