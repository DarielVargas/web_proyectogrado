package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Reporte;
import com.dv.agro_web.repositorios.ReporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;

    public ReporteService(ReporteRepository reporteRepository) {
        this.reporteRepository = reporteRepository;
    }

    @Transactional
    public Reporte guardarReporteGenerado(Long estacionId,
                                          LocalDate fechaInicio,
                                          LocalDate fechaFin,
                                          String tipoReporte) {
        Reporte reporte = new Reporte();
        reporte.setEstacionId(estacionId);
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setTipoReporte(tipoReporte);
        reporte.setNombreReporte(generarNombreReporte(tipoReporte, fechaInicio, fechaFin));
        reporte.setFechaGenerado(LocalDateTime.now());
        return reporteRepository.save(reporte);
    }

    public List<ReporteRepository.ReporteRecienteView> listarReportesRecientes() {
        return reporteRepository.findTop10Recientes();
    }

    public Optional<ReporteRepository.ReporteRecienteView> obtenerDetalleReporte(Long idReporte) {
        return reporteRepository.findDetalleById(idReporte);
    }

    private String generarNombreReporte(String tipoReporte, LocalDate fechaInicio, LocalDate fechaFin) {
        return switch (tipoReporte) {
            case "DIARIO" -> "Reporte Diario - " + fechaInicio;
            case "SEMANAL" -> "Reporte Semanal - " + fechaInicio + " al " + fechaFin;
            case "MENSUAL" -> "Reporte Mensual - " + fechaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM", new Locale("es", "ES")));
            default -> "Reporte - " + fechaInicio + " al " + fechaFin;
        };
    }
}
