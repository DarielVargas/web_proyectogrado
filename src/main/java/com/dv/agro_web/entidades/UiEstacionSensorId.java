package com.dv.agro_web.entidades;

import java.io.Serializable;
import java.util.Objects;

public class UiEstacionSensorId implements Serializable {

    private String estacionCodigo;
    private String tipoSensor;

    public UiEstacionSensorId() {
    }

    public UiEstacionSensorId(String estacionCodigo, String tipoSensor) {
        this.estacionCodigo = estacionCodigo;
        this.tipoSensor = tipoSensor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UiEstacionSensorId that = (UiEstacionSensorId) o;
        return Objects.equals(estacionCodigo, that.estacionCodigo)
                && Objects.equals(tipoSensor, that.tipoSensor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(estacionCodigo, tipoSensor);
    }
}
