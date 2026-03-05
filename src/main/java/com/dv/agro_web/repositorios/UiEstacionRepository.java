package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.UiEstacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UiEstacionRepository extends JpaRepository<UiEstacion, String> {

    List<UiEstacion> findByActivoTrue();
}