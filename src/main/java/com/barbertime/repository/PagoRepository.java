package com.barbertime.repository;

import com.barbertime.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByReservaId(Long reservaId);

    List<Pago> findByFechaPagoBetween(LocalDateTime inicio, LocalDateTime fin);
}