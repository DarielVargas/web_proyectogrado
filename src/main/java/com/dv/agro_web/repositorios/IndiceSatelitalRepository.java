package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.IndiceSatelital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IndiceSatelitalRepository extends JpaRepository<IndiceSatelital, Long> {

    List<IndiceSatelital> findAllByOrderByFechaDescIdDesc();

    Optional<IndiceSatelital> findTopByOrderByFechaDescIdDesc();

    List<IndiceSatelital> findAllByFechaBetweenOrderByFechaDescIdDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}