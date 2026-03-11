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
@Table(name = "historial_alertas")
public class HistorialAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "alerta_id")
    private Long alertaId;

    @Column(name = "estacion_codigo")
    private String estacionCodigo;

    @Column(name = "sensor_tipo")
    private String sensorTipo;

    @Column(name = "operador")
    private String operador;

    @Column(name = "umbral")
    private BigDecimal umbral;

    @Column(name = "valor_detectado")
    private BigDecimal valorDetectado;

    @Column(name = "fecha_activacion")
    private LocalDateTime fechaActivacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlertaId() {
        return alertaId;
    }

    public void setAlertaId(Long alertaId) {
        this.alertaId = alertaId;
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

    public BigDecimal getValorDetectado() {
        return valorDetectado;
    }

    public void setValorDetectado(BigDecimal valorDetectado) {
        this.valorDetectado = valorDetectado;
    }

    public LocalDateTime getFechaActivacion() {
        return fechaActivacion;
    }

    public void setFechaActivacion(LocalDateTime fechaActivacion) {
        this.fechaActivacion = fechaActivacion;
    }
}