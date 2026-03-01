package com.dv.agro_web.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Immutable
@Table(name = "vw_mediciones_detalle")
public class VwMedicionDetalle {

    @Id
    @Column(name = "medicion_id")
    private Long medicionId;

    @Column(name = "fecha_medicion")
    private Timestamp fechaMedicion;

    @Column(name = "valor")
    private BigDecimal valor;

    @Column(name = "sensor_id")
    private Integer sensorId;

    @Column(name = "codigo_sensor")
    private String codigoSensor;

    @Column(name = "tipo_sensor_id")
    private Integer tipoSensorId;

    @Column(name = "tipo_sensor")
    private String tipoSensor;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "estacion_id")
    private Integer estacionId;

    @Column(name = "estacion_codigo")
    private String estacionCodigo;

    @Column(name = "estacion_descripcion")
    private String estacionDescripcion;

    @Column(name = "parcela_id")
    private Integer parcelaId;

    @Column(name = "parcela_nombre")
    private String parcelaNombre;

    @Column(name = "parcela_ubicacion")
    private String parcelaUbicacion;

    // ===== Getters (Thymeleaf los usa para mostrar datos) =====

    public Long getMedicionId() { return medicionId; }
    public Timestamp getFechaMedicion() { return fechaMedicion; }
    public BigDecimal getValor() { return valor; }
    public Integer getSensorId() { return sensorId; }
    public String getCodigoSensor() { return codigoSensor; }
    public Integer getTipoSensorId() { return tipoSensorId; }
    public String getTipoSensor() { return tipoSensor; }
    public String getUnidadMedida() { return unidadMedida; }
    public String getModelo() { return modelo; }
    public Integer getEstacionId() { return estacionId; }
    public String getEstacionCodigo() { return estacionCodigo; }
    public String getEstacionDescripcion() { return estacionDescripcion; }
    public Integer getParcelaId() { return parcelaId; }
    public String getParcelaNombre() { return parcelaNombre; }
    public String getParcelaUbicacion() { return parcelaUbicacion; }
}