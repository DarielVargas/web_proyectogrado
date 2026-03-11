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
@Table(name = "alertas_historial")
public class AlertaHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long idHistorial;

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

    @Column(name = "fecha_activacion")
    private LocalDateTime fechaActivacion;

    public Long getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(Long idHistorial) {
        this.idHistorial = idHistorial;
    }

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

    public LocalDateTime getFechaActivacion() {
        return fechaActivacion;
    }

    public void setFechaActivacion(LocalDateTime fechaActivacion) {
        this.fechaActivacion = fechaActivacion;
    }
}
