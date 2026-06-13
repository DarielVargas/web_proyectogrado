package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.ImagenSatelital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImagenSatelitalRepository extends JpaRepository<ImagenSatelital, Long> {

    Optional<ImagenSatelital> findTopByOrderByFechaSubidaDescIdDesc();
}