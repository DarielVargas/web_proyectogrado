package com.dv.agro_web.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "indices_satelitales")
public class IndiceSatelital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "ndvi")
    private Double ndvi;

    @Column(name = "ndwi")
    private Double ndwi;

    @Column(name = "estado_vegetacion")
    private String estadoVegetacion;

    @Column(name = "estado_hidrico")
    private String estadoHidrico;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getNdvi() {
        return ndvi;
    }

    public void setNdvi(Double ndvi) {
        this.ndvi = ndvi;
    }

    public Double getNdwi() {
        return ndwi;
    }

    public void setNdwi(Double ndwi) {
        this.ndwi = ndwi;
    }

    public String getEstadoVegetacion() {
        return estadoVegetacion;
    }

    public void setEstadoVegetacion(String estadoVegetacion) {
        this.estadoVegetacion = estadoVegetacion;
    }

    public String getEstadoHidrico() {
        return estadoHidrico;
    }

    public void setEstadoHidrico(String estadoHidrico) {
        this.estadoHidrico = estadoHidrico;
    }
}
