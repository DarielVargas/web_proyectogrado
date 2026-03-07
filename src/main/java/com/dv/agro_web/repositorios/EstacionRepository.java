package com.dv.agro_web.repositorios;

import com.dv.agro_web.entidades.Estacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstacionRepository extends JpaRepository<Estacion, Long> {

    @Query("SELECT e FROM Estacion e WHERE e.activa = true ORDER BY e.id")
    List<Estacion> findAllActivas();

    @Query("SELECT e FROM Estacion e WHERE e.id = :id AND e.activa = true")
    Optional<Estacion> findActivaById(@Param("id") Long id);

    @Query("SELECT e FROM Estacion e WHERE lower(e.codigo) = lower(:codigo) AND e.activa = true")
    Optional<Estacion> findActivaByCodigoIgnoreCase(@Param("codigo") String codigo);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Estacion e WHERE lower(e.codigo) = lower(:codigo) AND e.activa = true")
    boolean existsActivaByCodigoIgnoreCase(@Param("codigo") String codigo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Estacion e SET e.activa = false WHERE e.id = :id AND e.activa = true")
    int desactivarPorId(@Param("id") Long id);
}