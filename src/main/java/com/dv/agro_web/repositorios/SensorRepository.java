package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    boolean existsByEstacionIdAndIdTipoSensor(Long estacionId, Long idTipoSensor);
}