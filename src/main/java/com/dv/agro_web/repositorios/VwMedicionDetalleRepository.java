package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.VwMedicionDetalle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface VwMedicionDetalleRepository extends JpaRepository<VwMedicionDetalle, Long> {

    interface EstacionResumen {
        String getEstacionCodigo();
        String getEstacionDescripcion();
    }

    @Query(value = """
        SELECT *
        FROM vw_mediciones_detalle v
        WHERE EXISTS (
            SELECT 1
            FROM ui_estacion ue
            WHERE ue.estacion_codigo = v.estacion_codigo
              AND ue.activo = 1
        )
          AND EXISTS (
            SELECT 1
            FROM ui_estacion_sensor us
            WHERE us.estacion_codigo = v.estacion_codigo
              AND us.tipo_sensor = v.tipo_sensor
              AND us.activo = 1
        )
        ORDER BY v.fecha_medicion DESC, v.medicion_id DESC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM vw_mediciones_detalle v
        WHERE EXISTS (
            SELECT 1
            FROM ui_estacion ue
            WHERE ue.estacion_codigo = v.estacion_codigo
              AND ue.activo = 1
        )
          AND EXISTS (
            SELECT 1
            FROM ui_estacion_sensor us
            WHERE us.estacion_codigo = v.estacion_codigo
              AND us.tipo_sensor = v.tipo_sensor
              AND us.activo = 1
        )
        """, nativeQuery = true)
    Page<VwMedicionDetalle> findHistorialSoloActivos(Pageable pageable);

    @Query(value = """
        SELECT AVG(t.valor)
        FROM (
            SELECT v.valor
            FROM vw_mediciones_detalle v
            WHERE v.tipo_sensor = 'Temperatura Ambiental'
              AND EXISTS (
                  SELECT 1
                  FROM ui_estacion ue
                  WHERE ue.estacion_codigo = v.estacion_codigo
                    AND ue.activo = 1
              )
              AND EXISTS (
                  SELECT 1
                  FROM ui_estacion_sensor us
                  WHERE us.estacion_codigo = v.estacion_codigo
                    AND us.tipo_sensor = v.tipo_sensor
                    AND us.activo = 1
              )
            ORDER BY v.fecha_medicion DESC, v.medicion_id DESC
            LIMIT 40
        ) t
        """, nativeQuery = true)
    BigDecimal avgUltimas40TempAmbientalSoloActivos();

    @Query(value = """
        SELECT AVG(h.valor)
        FROM (
            SELECT v.valor
            FROM vw_mediciones_detalle v
            WHERE v.tipo_sensor = 'Humedad Ambiental'
              AND EXISTS (
                  SELECT 1
                  FROM ui_estacion ue
                  WHERE ue.estacion_codigo = v.estacion_codigo
                    AND ue.activo = 1
              )
              AND EXISTS (
                  SELECT 1
                  FROM ui_estacion_sensor us
                  WHERE us.estacion_codigo = v.estacion_codigo
                    AND us.tipo_sensor = v.tipo_sensor
                    AND us.activo = 1
              )
            ORDER BY v.fecha_medicion DESC, v.medicion_id DESC
            LIMIT 40
        ) h
        """, nativeQuery = true)
    BigDecimal avgUltimas40HumedadAmbientalSoloActivos();

    @Query(value = """
        SELECT codigo AS estacionCodigo,
               MAX(descripcion) AS estacionDescripcion
        FROM (
            SELECT estacion_codigo AS codigo,
                   COALESCE(estacion_descripcion, estacion_codigo) AS descripcion
            FROM vw_mediciones_detalle
        ) e
        GROUP BY codigo
        ORDER BY codigo
        """, nativeQuery = true)
    List<EstacionResumen> findEstacionesConDatos();

    @Query(value = """
        SELECT *
        FROM (
            SELECT v.*,
                   ROW_NUMBER() OVER (
                       PARTITION BY v.estacion_codigo, v.tipo_sensor
                       ORDER BY v.fecha_medicion DESC, v.medicion_id DESC
                   ) AS rn
            FROM vw_mediciones_detalle v
        ) ultimas
        WHERE ultimas.rn = 1
        ORDER BY ultimas.estacion_codigo, ultimas.tipo_sensor
        """, nativeQuery = true)
    List<VwMedicionDetalle> findUltimasMedicionesPorEstacionYTipoSensor();
}
