package com.barbertime.controller.admin;

import com.barbertime.model.Pago;
import com.barbertime.model.Reserva;
import com.barbertime.repository.PagoRepository;
import com.barbertime.repository.ReservaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Controller
@RequestMapping("/admin/pagos")
public class AdminPagoController {

    private static final Set<String> METODOS_VALIDOS = Set.of("EFECTIVO", "TARJETA", "TRANSFERENCIA");

    private final PagoRepository pagoRepository;
    private final ReservaRepository reservaRepository;

    public AdminPagoController(PagoRepository pagoRepository, ReservaRepository reservaRepository) {
        this.pagoRepository = pagoRepository;
        this.reservaRepository = reservaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("pagos", pagoRepository.findAll());
        model.addAttribute("reservas", reservaRepository.findAllByOrderByFechaHoraDesc());

        return "admin/pagos";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam Long reservaId, @RequestParam BigDecimal monto, @RequestParam String metodoPago,
            RedirectAttributes redirectAttributes) {
        String metodo = metodoPago.toUpperCase();

        if (!METODOS_VALIDOS.contains(metodo)) {
            redirectAttributes.addFlashAttribute("error", "El método de pago no es válido.");

            return "redirect:/admin/pagos";
        }

        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("error", "El monto debe ser mayor que cero.");

            return "redirect:/admin/pagos";
        }

        if (pagoRepository.existsByReservaId(reservaId)) {
            redirectAttributes.addFlashAttribute("error", "Esta reserva ya tiene un pago registrado.");

            return "redirect:/admin/pagos";
        }

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new IllegalArgumentException("Reserva no encontrada"));

        Pago pago = new Pago();
        pago.setReserva(reserva);
        pago.setMonto(monto);
        pago.setMetodoPago(metodo);
        pago.setFechaPago(LocalDateTime.now());

        pagoRepository.save(pago);

        redirectAttributes.addFlashAttribute("mensaje", "Pago registrado correctamente.");

        return "redirect:/admin/pagos";
    }
}