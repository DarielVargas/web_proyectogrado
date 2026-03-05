package com.dv.agro_web.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UiEstacionSensorId implements Serializable {

    @Column(name = "estacion_codigo")
    private String estacionCodigo;

    @Column(name = "tipo_sensor")
    private String tipoSensor;

    public UiEstacionSensorId() {
    }

    public UiEstacionSensorId(String estacionCodigo, String tipoSensor) {
        this.estacionCodigo = estacionCodigo;
        this.tipoSensor = tipoSensor;
    }

    public String getEstacionCodigo() {
        return estacionCodigo;
    }

    public String getTipoSensor() {
        return tipoSensor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UiEstacionSensorId that = (UiEstacionSensorId) o;
        return Objects.equals(estacionCodigo, that.estacionCodigo) && Objects.equals(tipoSensor, that.tipoSensor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estacionCodigo, tipoSensor);
    }
}
