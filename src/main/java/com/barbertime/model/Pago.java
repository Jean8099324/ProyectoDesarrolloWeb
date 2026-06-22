package com.barbertime.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pagos")
@Data
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double monto;

    private String metodoPago; // Efectivo, Tarjeta, Transferencia

    @OneToOne
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;
}