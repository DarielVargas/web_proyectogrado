package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.InformacionCultivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InformacionCultivoRepository extends JpaRepository<InformacionCultivo, Long> {

    Optional<InformacionCultivo> findFirstByOrderByIdAsc();

    List<InformacionCultivo> findAllByOrderByIdDesc();
}