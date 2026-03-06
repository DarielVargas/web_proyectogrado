package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.UiEstacionSensor;
import com.dv.agro_web.entidades.UiEstacionSensorId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UiEstacionSensorRepository extends JpaRepository<UiEstacionSensor, UiEstacionSensorId> {

    List<UiEstacionSensor> findByEstacionCodigo(String estacionCodigo);

    List<UiEstacionSensor> findByEstacionCodigoIn(Collection<String> estacionCodigos);
}