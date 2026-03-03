package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.VwMedicionDetalle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface VwMedicionDetalleRepository extends JpaRepository<VwMedicionDetalle, Long> {

    // Paginación normal (la tabla)
    Page<VwMedicionDetalle> findAllByOrderByFechaMedicionDesc(Pageable pageable);


    // =========================================
    // PROMEDIO ÚLTIMAS 40 TEMPERATURAS AMBIENTALES
    // =========================================
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


    // =========================================
    // PROMEDIO ÚLTIMAS 40 HUMEDADES AMBIENTALES
    // =========================================
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
}