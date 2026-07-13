package com.barbertime.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barbertime.model.Servicio;
import com.barbertime.repository.ServicioRepository;

@RestController
@RequestMapping("/api/servicios")
public class ServicioRestController {

    private final ServicioRepository servicioRepository;

    public ServicioRestController(ServicioRepository servicioRepository) {

        this.servicioRepository = servicioRepository;
    }

    @GetMapping
    public ResponseEntity<List<Servicio>> listar() {
        return ResponseEntity.ok(servicioRepository.findByActivoTrueOrderByNombreAsc());
    }

    @PostMapping
    public ResponseEntity<Servicio> crear(@RequestBody Servicio servicio) {

        if (servicio.getNombre() == null || servicio.getNombre().isBlank()) {

            return ResponseEntity.badRequest().build();
        }

        if (servicio.getPrecio() == null || servicio.getPrecio() <= 0) {

            return ResponseEntity.badRequest().build();
        }

        servicio.setId(null);
        servicio.setNombre(servicio.getNombre().trim());
        servicio.setActivo(true);

        Servicio guardado = servicioRepository.save(servicio);

        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }
}