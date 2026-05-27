package com.dv.agro_web.servicios;

import com.dv.agro_web.controllers.IndiceSatelitalRequest;
import com.dv.agro_web.entidades.IndiceSatelital;
import com.dv.agro_web.repositorios.IndiceSatelitalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SatelitalService {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IndiceSatelitalRepository indiceSatelitalRepository;

    public SatelitalService(IndiceSatelitalRepository indiceSatelitalRepository) {
        this.indiceSatelitalRepository = indiceSatelitalRepository;
    }

    public IndiceSatelital guardarIndiceSatelital(IndiceSatelitalRequest request) {

        IndiceSatelital indiceSatelital = new IndiceSatelital();

        indiceSatelital.setFecha(request.getFecha());
        indiceSatelital.setNdvi(request.getNdvi());
        indiceSatelital.setNdwi(request.getNdwi());
        indiceSatelital.setEstadoVegetacion(request.getEstadoVegetacion());
        indiceSatelital.setEstadoHidrico(request.getEstadoHidrico());

        return indiceSatelitalRepository.save(indiceSatelital);
    }

    public List<IndiceSatelital> listarHistorial() {
        return indiceSatelitalRepository.findAllByOrderByFechaDescIdDesc();
    }

    public List<HistorialIndiceDto> listarHistorialPresentacion() {
        List<HistorialIndiceDto> historial = new ArrayList<>();

        for (IndiceSatelital indice : listarHistorial()) {
            historial.add(new HistorialIndiceDto(
                    indice.getId(),
                    indice.getFecha() != null ? indice.getFecha().format(FORMATO_FECHA) : "N/A",
                    indice.getNdvi(),
                    indice.getNdwi(),
                    valorTexto(indice.getEstadoVegetacion()),
                    valorTexto(indice.getEstadoHidrico()),
                    claseNdviPorValor(indice.getNdvi()),
                    claseEstado(indice.getEstadoVegetacion()),
                    claseEstado(indice.getEstadoHidrico())
            ));
        }

        return historial;
    }


    public Page<HistorialIndiceDto> listarHistorialPresentacionPaginado(int page, int limit, String filtro, LocalDate fecha, LocalDate fechaInicio, LocalDate fechaFin) {
        int limiteNormalizado = List.of(5, 10, 25, 50).contains(limit) ? limit : 10;
        int paginaNormalizada = Math.max(page, 0);

        List<HistorialIndiceDto> base = switch (filtro != null ? filtro : "todo") {
            case "fecha" -> fecha != null ? listarHistorialPresentacionPorRango(fecha, fecha) : List.of();
            case "rango" -> (fechaInicio != null && fechaFin != null) ? listarHistorialPresentacionPorRango(fechaInicio, fechaFin) : List.of();
            default -> listarHistorialPresentacion();
        };

        int desde = Math.min(paginaNormalizada * limiteNormalizado, base.size());
        int hasta = Math.min(desde + limiteNormalizado, base.size());
        List<HistorialIndiceDto> pageContent = base.subList(desde, hasta);

        return new PageImpl<>(
                pageContent,
                PageRequest.of(paginaNormalizada, limiteNormalizado, Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id"))),
                base.size()
        );
    }

    public List<HistorialIndiceDto> listarHistorialFiltradoParaDescarga(String filtro, LocalDate fecha, LocalDate fechaInicio, LocalDate fechaFin) {
        return switch (filtro != null ? filtro : "todo") {
            case "fecha" -> fecha != null ? listarHistorialPresentacionPorRango(fecha, fecha) : List.of();
            case "rango" -> (fechaInicio != null && fechaFin != null) ? listarHistorialPresentacionPorRango(fechaInicio, fechaFin) : List.of();
            default -> listarHistorialPresentacion();
        };
    }

    public List<HistorialIndiceDto> listarHistorialPresentacionPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return List.of();
        }

        LocalDateTime desde = fechaInicio.atStartOfDay();
        LocalDateTime hasta = fechaFin.plusDays(1).atStartOfDay().minusNanos(1);

        List<HistorialIndiceDto> historial = new ArrayList<>();

        for (IndiceSatelital indice : indiceSatelitalRepository.findAllByFechaBetweenOrderByFechaDescIdDesc(desde, hasta)) {
            historial.add(new HistorialIndiceDto(
                    indice.getId(),
                    indice.getFecha() != null ? indice.getFecha().format(FORMATO_FECHA) : "N/A",
                    indice.getNdvi(),
                    indice.getNdwi(),
                    valorTexto(indice.getEstadoVegetacion()),
                    valorTexto(indice.getEstadoHidrico()),
                    claseNdviPorValor(indice.getNdvi()),
                    claseEstado(indice.getEstadoVegetacion()),
                    claseEstado(indice.getEstadoHidrico())
            ));
        }

        return historial;
    }

    public ResumenSatelitalDto obtenerResumenActual() {

        IndiceSatelital ultimo =
                indiceSatelitalRepository
                        .findTopByOrderByFechaDescIdDesc()
                        .orElse(null);

        if (ultimo == null) {

            return new ResumenSatelitalDto(
                    "N/A",
                    "Sin dato",
                    "N/A",
                    "Sin dato",
                    "estado-neutral",
                    "Sin dato",
                    "estado-neutral",
                    "N/A"
            );
        }

        String ndviTexto =
                ultimo.getNdvi() != null
                        ? String.format("%.3f", ultimo.getNdvi())
                        : "N/A";

        String ndwiTexto =
                ultimo.getNdwi() != null
                        ? String.format("%.3f", ultimo.getNdwi())
                        : "N/A";

        String fechaTexto =
                ultimo.getFecha() != null
                        ? ultimo.getFecha().format(FORMATO_FECHA)
                        : "N/A";

        return new ResumenSatelitalDto(
                ndviTexto,
                clasificacionNdvi(ultimo.getNdvi()),
                ndwiTexto,
                valorTexto(ultimo.getEstadoVegetacion()),
                claseEstado(ultimo.getEstadoVegetacion()),
                valorTexto(ultimo.getEstadoHidrico()),
                claseEstado(ultimo.getEstadoHidrico()),
                fechaTexto
        );
    }

    private String clasificacionNdvi(Double ndvi) {

        if (ndvi == null) {
            return "Sin dato";
        }

        if (ndvi >= 0.6) {
            return "NDVI alto";
        }

        if (ndvi >= 0.3) {
            return "NDVI medio";
        }

        return "NDVI bajo";
    }

    private String valorTexto(String valor) {

        if (valor == null || valor.isBlank()) {
            return "Sin dato";
        }

        return valor;
    }

    private String claseEstado(String estado) {

        if (estado == null) {
            return "estado-neutral";
        }

        String e = estado.trim().toLowerCase();

        if (e.contains("saludable") || e.contains("óptimo") || e.contains("optimo")) {
            return "estado-bueno";
        }

        if (e.contains("critic")) {
            return "estado-critico";
        }

        if (e.contains("moderad")) {
            return "estado-moderado";
        }

        return "estado-neutral";
    }

    private String claseNdviPorValor(Double ndvi) {
        if (ndvi == null) {
            return "estado-neutral";
        }
        if (ndvi >= 0.6) {
            return "estado-bueno";
        }
        if (ndvi >= 0.3) {
            return "estado-moderado";
        }
        return "estado-critico";
    }

    public record ResumenSatelitalDto(
            String ndviTexto,
            String ndviNivel,
            String ndwiTexto,
            String estadoVegetacion,
            String estadoVegetacionClase,
            String estadoHidrico,
            String estadoHidricoClase,
            String fechaTexto
    ) {
    }

    public record HistorialIndiceDto(
            Long id,
            String fechaTexto,
            Double ndvi,
            Double ndwi,
            String estadoVegetacion,
            String estadoHidrico,
            String ndviClase,
            String estadoVegetacionClase,
            String estadoHidricoClase
    ) {
    }
}