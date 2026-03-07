package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    boolean existsByEstacionIdAndIdTipoSensor(Long estacionId, Long idTipoSensor);

    @Query(value = """
            SELECT COUNT(*)
            FROM sensores s
            INNER JOIN estaciones e ON e.id = s.estacion_id
            WHERE e.activa = 1
            """, nativeQuery = true)
    long contarSensoresRegistradosDeEstacionesActivas();

    @Query(value = """
            SELECT COUNT(*)
            FROM sensores s
            INNER JOIN estaciones e ON e.id = s.estacion_id
            INNER JOIN tipo_sensor ts ON ts.id_tipo_sensor = s.id_tipo_sensor
            LEFT JOIN ui_estacion_sensor ues
                ON ues.estacion_codigo = e.codigo
               AND ues.tipo_sensor = ts.tipo_sensor
            WHERE e.activa = 1
              AND (ues.activo = 1 OR ues.activo IS NULL)
            """, nativeQuery = true)
    long contarSensoresActivosDeEstacionesActivas();
}