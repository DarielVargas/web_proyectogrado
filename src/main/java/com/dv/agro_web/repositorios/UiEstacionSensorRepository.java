package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.UiEstacionSensor;
import com.dv.agro_web.entidades.UiEstacionSensorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UiEstacionSensorRepository extends JpaRepository<UiEstacionSensor, UiEstacionSensorId> {

    List<UiEstacionSensor> findByIdEstacionCodigoOrderByIdTipoSensor(String estacionCodigo);

    List<UiEstacionSensor> findByIdEstacionCodigoAndActivoFalse(String estacionCodigo);

    @Modifying
    @Query(value = """
          INSERT INTO ui_estacion_sensor (estacion_codigo, tipo_sensor, activo, updated_at)
          SELECT DISTINCT e.codigo_estacion, ts.tipo_sensor, 1, NOW()
          FROM mediciones m
          JOIN sensores s ON s.id = m.sensor_id
          JOIN estaciones e ON e.id = s.estacion_id
          JOIN tipo_sensor ts ON ts.id_tipo_sensor = s.id_tipo_sensor
          LEFT JOIN ui_estacion_sensor u
            ON u.estacion_codigo = e.codigo_estacion AND u.tipo_sensor = ts.tipo_sensor
          WHERE e.codigo_estacion = :estacionCodigo
            AND u.estacion_codigo IS NULL
        """, nativeQuery = true)
    int ensureDefaultsExist(@Param("estacionCodigo") String estacionCodigo);
}
