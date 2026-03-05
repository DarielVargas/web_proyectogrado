package com.dv.agro_web.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ui_estacion_sensor")
public class UiEstacionSensor {

    @EmbeddedId
    private UiEstacionSensorId id;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UiEstacionSensorId getId() {
        return id;
    }

    public void setId(UiEstacionSensorId id) {
        this.id = id;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
