package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.HistorialAlerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialAlertaRepository extends JpaRepository<HistorialAlerta, Long> {
    List<HistorialAlerta> findAllByOrderByFechaActivacionDescIdDesc();
}