package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.Estacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstacionRepository extends JpaRepository<Estacion, Long> {

    Optional<Estacion> findByCodigo(String codigo);
}
