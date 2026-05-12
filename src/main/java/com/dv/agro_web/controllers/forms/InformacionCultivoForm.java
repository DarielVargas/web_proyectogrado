package com.dv.agro_web.controllers.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class InformacionCultivoForm {

    private Long id;

    @NotBlank(message = "El nombre del cultivo es obligatorio")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
    private String nombreCultivo;

    @NotBlank(message = "El encargado es obligatorio")
    @Size(max = 120, message = "El encargado no puede superar 120 caracteres")
    private String encargado;

    @NotBlank(message = "Seleccione un lote o área")
    @Size(max = 120, message = "El lote/área no puede superar 120 caracteres")
    private String loteArea;

    @NotNull(message = "Seleccione una estación asociada")
    private Long estacionId;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaCosechaEstimada;

    private String observaciones;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCultivo() {
        return nombreCultivo;
    }

    public void setNombreCultivo(String nombreCultivo) {
        this.nombreCultivo = limpiar(nombreCultivo);
    }

    public String getEncargado() {
        return encargado;
    }

    public void setEncargado(String encargado) {
        this.encargado = limpiar(encargado);
    }

    public String getLoteArea() {
        return loteArea;
    }

    public void setLoteArea(String loteArea) {
        this.loteArea = limpiar(loteArea);
    }

    public Long getEstacionId() {
        return estacionId;
    }

    public void setEstacionId(Long estacionId) {
        this.estacionId = estacionId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaCosechaEstimada() {
        return fechaCosechaEstimada;
    }

    public void setFechaCosechaEstimada(LocalDate fechaCosechaEstimada) {
        this.fechaCosechaEstimada = fechaCosechaEstimada;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = limpiar(observaciones);
    }

    private String limpiar(String valor) {
        return valor != null ? valor.trim() : null;
    }
}