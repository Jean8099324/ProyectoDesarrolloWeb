package com.barbertime.controller.admin;

import com.barbertime.model.Rol;
import com.barbertime.model.Usuario;
import com.barbertime.repository.RolRepository;
import com.barbertime.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequestMapping("/admin/barberos")
public class AdminBarberoController {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBarberoController(UsuarioRepository usuarioRepository, RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("barberos", usuarioRepository.findDistinctByRolesNombre("BARBERO"));

        return "admin/barberos";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam String nombre, @RequestParam String correo,
            @RequestParam(required = false) String telefono, @RequestParam String password,
            RedirectAttributes redirectAttributes) {

        String nombreNormalizado = nombre.trim();
        String correoNormalizado = correo.trim().toLowerCase();
        String telefonoNormalizado = telefono == null ? "" : telefono.trim();

        if (nombreNormalizado.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "El nombre es obligatorio.");
            return "redirect:/admin/barberos";
        }

        if (correoNormalizado.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "El correo es obligatorio.");
            return "redirect:/admin/barberos";
        }

        if (password == null || password.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "redirect:/admin/barberos";
        }

        if (usuarioRepository.findByCorreo(correoNormalizado).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El correo ya se encuentra registrado.");
            return "redirect:/admin/barberos";
        }

        Rol rolBarbero = rolRepository.findByNombre("BARBERO")
                .orElseThrow(() -> new IllegalStateException("El rol BARBERO no existe."));

        Usuario barbero = new Usuario();
        barbero.setNombre(nombreNormalizado);
        barbero.setCorreo(correoNormalizado);
        barbero.setUsername(correoNormalizado);
        barbero.setTelefono(telefonoNormalizado);
        barbero.setPassword(passwordEncoder.encode(password));
        barbero.setRoles(Set.of(rolBarbero));

        usuarioRepository.save(barbero);

        redirectAttributes.addFlashAttribute("mensaje", "Barbero registrado correctamente.");

        return "redirect:/admin/barberos";
    }
}