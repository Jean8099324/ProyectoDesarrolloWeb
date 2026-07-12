package com.barbertime.controller;

import com.barbertime.model.Usuario;
import com.barbertime.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioRepository usuarioRepository;

    public PerfilController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String mostrarPerfil(Model model, Principal principal) {
        Usuario usuario = buscarUsuario(principal);

        model.addAttribute("usuario", usuario);

        return "perfil";
    }

    @PostMapping("/actualizar")
    public String actualizarPerfil(@RequestParam String nombre, @RequestParam String telefono, Principal principal,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = buscarUsuario(principal);

        usuario.setNombre(nombre.trim());
        usuario.setTelefono(telefono.trim());

        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado correctamente.");

        return "redirect:/perfil";
    }

    private Usuario buscarUsuario(Principal principal) {
        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }
}