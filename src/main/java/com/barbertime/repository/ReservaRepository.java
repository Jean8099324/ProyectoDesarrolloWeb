package com.barbertime.repository;

import com.barbertime.model.Reserva;
import com.barbertime.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    boolean existsByBarberoAndFechaHoraAndEstadoNot(Usuario barbero, LocalDateTime fechaHora, String estado);

    List<Reserva> findByUsuarioOrderByFechaHoraDesc(Usuario usuario);

    List<Reserva> findByBarberoOrderByFechaHoraAsc(Usuario barbero);

    List<Reserva> findAllByOrderByFechaHoraDesc();

    List<Reserva> findByFechaHoraBetweenOrderByFechaHoraAsc(LocalDateTime inicio, LocalDateTime fin);
}