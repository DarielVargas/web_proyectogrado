package com.dv.agro_web.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "estaciones")
public class Estacion {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "codigo_estacion")
    private String codigo;

    @Column(name = "parcela_id")
    private Long parcelaId;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha_instalacion")
    private LocalDate fechaInstalacion;

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Long getParcelaId() {
        return parcelaId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDate getFechaInstalacion() {
        return fechaInstalacion;
    }
}
