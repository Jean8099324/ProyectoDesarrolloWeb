package com.barbertime.config;

import com.barbertime.model.Rol;
import com.barbertime.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner crearRoles(RolRepository rolRepository) {
        return args -> {
            List<String> nombres = List.of("ADMIN", "BARBERO", "CLIENTE");

            for (String nombre : nombres) {
                if (rolRepository.findByNombre(nombre).isEmpty()) {
                    Rol rol = new Rol();
                    rol.setNombre(nombre);
                    rolRepository.save(rol);
                }
            }
        };
    }
}