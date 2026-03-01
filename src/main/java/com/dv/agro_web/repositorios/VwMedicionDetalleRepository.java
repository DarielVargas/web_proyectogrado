package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.VwMedicionDetalle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VwMedicionDetalleRepository extends JpaRepository<VwMedicionDetalle, Long> {

    // Para traer los últimos N ordenados por fecha DESC usando paginación
    Page<VwMedicionDetalle> findAllByOrderByFechaMedicionDesc(Pageable pageable);
}