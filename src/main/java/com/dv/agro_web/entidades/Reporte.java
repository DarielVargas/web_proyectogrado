package com.dv.agro_web.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long idReporte;

    @Column(name = "estacion_id")
    private Long estacionId;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "tipo_reporte")
    private String tipoReporte;

    @Column(name = "nombre_reporte")
    private String nombreReporte;

    @Column(name = "fecha_generado")
    private LocalDateTime fechaGenerado;

    public Long getIdReporte() { return idReporte; }
    public void setIdReporte(Long idReporte) { this.idReporte = idReporte; }
    public Long getEstacionId() { return estacionId; }
    public void setEstacionId(Long estacionId) { this.estacionId = estacionId; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getTipoReporte() { return tipoReporte; }
    public void setTipoReporte(String tipoReporte) { this.tipoReporte = tipoReporte; }
    public String getNombreReporte() { return nombreReporte; }
    public void setNombreReporte(String nombreReporte) { this.nombreReporte = nombreReporte; }
    public LocalDateTime getFechaGenerado() { return fechaGenerado; }
    public void setFechaGenerado(LocalDateTime fechaGenerado) { this.fechaGenerado = fechaGenerado; }
}