package com.barbertime.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.barbertime.model.Servicio;
import com.barbertime.repository.ServicioRepository;

@Controller
@RequestMapping("/admin/servicios")
public class AdminServicioController {

    private final ServicioRepository servicioRepository;

    public AdminServicioController(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicios", servicioRepository.findAll());
        return "admin/servicios";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam String nombre, @RequestParam Double precio,
            @RequestParam(required = false) String descripcion,
            @RequestParam(defaultValue = "30") Integer duracionMinutos, RedirectAttributes redirectAttributes) {
        if (precio <= 0) {
            redirectAttributes.addFlashAttribute("error", "El precio debe ser mayor que cero.");

            return "redirect:/admin/servicios";
        }

        Servicio servicio = new Servicio();
        servicio.setNombre(nombre.trim());
        servicio.setPrecio(precio);
        servicio.setDescripcion(descripcion);
        servicio.setDuracionMinutos(duracionMinutos);
        servicio.setActivo(true);

        servicioRepository.save(servicio);

        redirectAttributes.addFlashAttribute("mensaje", "Servicio registrado correctamente.");

        return "redirect:/admin/servicios";
    }

    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable long id, RedirectAttributes redirectAttributes) {

        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        servicio.setActivo(!Boolean.TRUE.equals(servicio.getActivo()));
        servicioRepository.save(servicio);

        redirectAttributes.addFlashAttribute("mensaje", "Estado del servicio actualizado.");

        return "redirect:/admin/servicios";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable long id, RedirectAttributes redirectAttributes) {

        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        servicio.setActivo(false);
        servicioRepository.save(servicio);

        redirectAttributes.addFlashAttribute("mensaje", "Servicio eliminado del catálogo.");

        return "redirect:/admin/servicios";
    }
}