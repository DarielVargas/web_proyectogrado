package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.CalendarioAgricola;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarioAgricolaRepository extends JpaRepository<CalendarioAgricola, Long> {

    List<CalendarioAgricola> findAllByOrderByFechaHoraAscIdAsc();

    List<CalendarioAgricola> findAllByCompletadaFalseAndFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAscIdAsc(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    List<CalendarioAgricola> findAllByCompletadaFalseAndFechaHoraBeforeOrderByFechaHoraAscIdAsc(LocalDateTime fechaHora);

    List<CalendarioAgricola> findAllByCompletadaTrueOrderByFechaCompletadaDescFechaHoraDescIdDesc();

    long countByCompletadaTrue();

    long countByCompletadaFalseAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    long countByCompletadaFalseAndFechaHoraBefore(LocalDateTime fechaHora);
}
