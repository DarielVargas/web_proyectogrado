package com.dv.agro_web.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ui_estacion")
public class UiEstacion {

    @Id
    @Column(name = "estacion_codigo")
    private String estacionCodigo;

    @Column(name = "activo")
    private Boolean activo;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getEstacionCodigo() {
        return estacionCodigo;
    }

    public void setEstacionCodigo(String estacionCodigo) {
        this.estacionCodigo = estacionCodigo;
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
