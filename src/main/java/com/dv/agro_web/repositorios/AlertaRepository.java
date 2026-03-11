package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findAllByOrderByFechaCreacionDescIdAlertaDesc();

    List<Alerta> findAllByActivaTrueOrderByFechaCreacionDescIdAlertaDesc();

    List<Alerta> findAllByOrderByFechaCreacionAscIdAlertaAsc();

    long countByActivaTrue();
}
