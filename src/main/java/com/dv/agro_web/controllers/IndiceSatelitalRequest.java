package com.dv.agro_web.controllers;

import java.time.LocalDateTime;

public class IndiceSatelitalRequest {

    private LocalDateTime fecha;
    private Double ndvi;
    private Double ndwi;
    private String estadoVegetacion;
    private String estadoHidrico;

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