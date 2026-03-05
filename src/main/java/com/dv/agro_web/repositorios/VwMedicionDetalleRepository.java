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

    Page<VwMedicionDetalle> findAllByOrderByFechaMedicionDesc(Pageable pageable);

    Page<VwMedicionDetalle> findByEstacionCodigoInOrderByFechaMedicionDesc(List<String> estacionesActivas, Pageable pageable);

    @Query(value = """
        SELECT AVG(t.valor)
        FROM (
            SELECT valor
            FROM vw_mediciones_detalle
            WHERE tipo_sensor = 'Temperatura Ambiental'
            ORDER BY fecha_medicion DESC, medicion_id DESC
            LIMIT 40
        ) t
        """, nativeQuery = true)
    BigDecimal avgUltimas40TempAmbiental();

    @Query(value = """
        SELECT AVG(t.valor)
        FROM (
            SELECT valor
            FROM vw_mediciones_detalle
            WHERE tipo_sensor = 'Temperatura Ambiental'
              AND estacion_codigo IN (:estacionesActivas)
            ORDER BY fecha_medicion DESC, medicion_id DESC
            LIMIT 40
        ) t
        """, nativeQuery = true)
    BigDecimal avgUltimas40TempAmbientalSoloActivas(List<String> estacionesActivas);

    @Query(value = """
        SELECT AVG(h.valor)
        FROM (
            SELECT valor
            FROM vw_mediciones_detalle
            WHERE tipo_sensor = 'Humedad Ambiental'
            ORDER BY fecha_medicion DESC, medicion_id DESC
            LIMIT 40
        ) h
        """, nativeQuery = true)
    BigDecimal avgUltimas40HumedadAmbiental();

    @Query(value = """
        SELECT AVG(h.valor)
        FROM (
            SELECT valor
            FROM vw_mediciones_detalle
            WHERE tipo_sensor = 'Humedad Ambiental'
              AND estacion_codigo IN (:estacionesActivas)
            ORDER BY fecha_medicion DESC, medicion_id DESC
            LIMIT 40
        ) h
        """, nativeQuery = true)
    BigDecimal avgUltimas40HumedadAmbientalSoloActivas(List<String> estacionesActivas);

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