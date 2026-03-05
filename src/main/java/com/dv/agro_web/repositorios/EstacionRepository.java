package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.Estacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstacionRepository extends JpaRepository<Estacion, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
}
