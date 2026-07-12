package com.barbertime.controller;

import com.barbertime.model.Rol;
import com.barbertime.model.Usuario;
import com.barbertime.repository.RolRepository;
import com.barbertime.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String nombre, @RequestParam String correo,
            @RequestParam(required = false) String telefono, @RequestParam String password,
            @RequestParam String confirmarPassword, Model model) {
        String nombreNormalizado = nombre.trim();
        String correoNormalizado = correo.trim().toLowerCase();
        String telefonoNormalizado = telefono != null ? telefono.trim() : "";
        if (nombreNormalizado.isBlank()) {
            model.addAttribute("error", "El nombre es obligatorio.");
            return "register";
        }
        if (correoNormalizado.isBlank()) {
            model.addAttribute("error", "El correo es obligatorio.");
            return "register";
        }
        if (password.isBlank()) {
            model.addAttribute("error", "La contraseña es obligatoria.");
            return "register";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "register";
        }
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "register";
        }
        if (usuarioRepository.findByCorreo(correoNormalizado).isPresent()) {
            model.addAttribute("error", "El correo ya está registrado.");
            return "register";
        }
        if (usuarioRepository.findByUsername(correoNormalizado).isPresent()) {
            model.addAttribute("error", "Ya existe un usuario con ese correo.");
            return "register";
        }
        Rol rolCliente = rolRepository.findByNombre("CLIENTE").orElseGet(() -> {
            Rol nuevoRol = new Rol();
            nuevoRol.setNombre("CLIENTE");
            return rolRepository.save(nuevoRol);
        });
        String passwordCifrada = passwordEncoder.encode(password);
        Usuario usuario = new Usuario();
        usuario.setNombre(nombreNormalizado);
        usuario.setCorreo(correoNormalizado);
        usuario.setUsername(correoNormalizado);
        usuario.setTelefono(telefonoNormalizado);
        usuario.setPassword(passwordCifrada);
        usuario.setRoles(Set.of(rolCliente));
        usuarioRepository.save(usuario);
        return "redirect:/login?registro=true";
    }

}