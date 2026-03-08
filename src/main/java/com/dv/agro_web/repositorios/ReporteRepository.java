package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {

    interface ReporteRecienteView {
        Long getIdReporte();
        String getNombreReporte();
        String getEstacionCodigo();
        String getEstacionDescripcion();
        LocalDate getFechaInicio();
        LocalDate getFechaFin();
        String getTipoReporte();
        LocalDateTime getFechaGenerado();
    }

    @Query(value = """
        SELECT r.id_reporte AS idReporte,
               r.nombre_reporte AS nombreReporte,
               e.codigo AS estacionCodigo,
               e.descripcion AS estacionDescripcion,
               r.fecha_inicio AS fechaInicio,
               r.fecha_fin AS fechaFin,
               r.tipo_reporte AS tipoReporte,
               r.fecha_generado AS fechaGenerado
        FROM reportes r
        LEFT JOIN estaciones e ON e.id = r.estacion_id
        ORDER BY r.fecha_generado DESC
        LIMIT 10
        """, nativeQuery = true)
    List<ReporteRecienteView> findTop10Recientes();
}
