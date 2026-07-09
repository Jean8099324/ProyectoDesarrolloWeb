package com.barbertime.controller;

import com.barbertime.model.Rol;
import com.barbertime.model.Usuario;
import com.barbertime.repository.RolRepository;
import com.barbertime.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/registro")
    public String registrarUsuario(@RequestParam String nombre, @RequestParam String correo,
            @RequestParam String telefono, @RequestParam String password, @RequestParam String confirmarPassword,
            Model model) {

        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            return "register";
        }

        if (usuarioRepository.findByCorreo(correo).isPresent()) {
            model.addAttribute("error", "El correo ya está registrado");
            return "register";
        }

        Rol rolUsuario = rolRepository.findByNombre("CLIENTE").orElseGet(() -> {
            Rol rol = new Rol();
            rol.setNombre("CLIENTE");
            return rolRepository.save(rol);
        });

        Usuario usuario = new Usuario();

        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);

        usuario.setUsername(correo);

        usuario.setPassword(passwordEncoder.encode(password));

        usuario.setRoles(Set.of(rolUsuario));

        usuarioRepository.save(usuario);

        return "redirect:/login";
    }

}