package com.dv.agro_web.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;

    @Column(name = "estacion_codigo")
    private String estacionCodigo;

    @Column(name = "sensor_tipo")
    private String sensorTipo;

    @Column(name = "operador")
    private String operador;

    @Column(name = "umbral")
    private BigDecimal umbral;

    @Column(name = "activa")
    private Boolean activa = true;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public Long getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(Long idAlerta) {
        this.idAlerta = idAlerta;
    }

    public String getEstacionCodigo() {
        return estacionCodigo;
    }

    public void setEstacionCodigo(String estacionCodigo) {
        this.estacionCodigo = estacionCodigo;
    }

    public String getSensorTipo() {
        return sensorTipo;
    }

    public void setSensorTipo(String sensorTipo) {
        this.sensorTipo = sensorTipo;
    }

    public String getOperador() {
        return operador;
    }

    public void setOperador(String operador) {
        this.operador = operador;
    }

    public BigDecimal getUmbral() {
        return umbral;
    }

    public void setUmbral(BigDecimal umbral) {
        this.umbral = umbral;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
