package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.AlertaHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaHistorialRepository extends JpaRepository<AlertaHistorial, Long> {
    List<AlertaHistorial> findAllByOrderByFechaActivacionDescIdHistorialDesc();
}
