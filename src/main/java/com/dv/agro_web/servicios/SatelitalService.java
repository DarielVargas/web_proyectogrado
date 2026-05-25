package com.dv.agro_web.servicios;

import com.dv.agro_web.controllers.IndiceSatelitalRequest;
import com.dv.agro_web.entidades.IndiceSatelital;
import com.dv.agro_web.repositorios.IndiceSatelitalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SatelitalService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final IndiceSatelitalRepository indiceSatelitalRepository;

    public SatelitalService(IndiceSatelitalRepository indiceSatelitalRepository) {
        this.indiceSatelitalRepository = indiceSatelitalRepository;
    }

    public IndiceSatelital guardarIndiceSatelital(IndiceSatelitalRequest request) {
        IndiceSatelital indiceSatelital = new IndiceSatelital();
        indiceSatelital.setFecha(request.getFecha() != null ? request.getFecha() : LocalDateTime.now());
        indiceSatelital.setNdvi(request.getNdvi());
        indiceSatelital.setNdwi(request.getNdwi());
        indiceSatelital.setEstadoVegetacion(request.getEstadoVegetacion());
        indiceSatelital.setEstadoHidrico(request.getEstadoHidrico());
        return indiceSatelitalRepository.save(indiceSatelital);
    }

    public List<IndiceSatelital> listarHistorial() {
        return indiceSatelitalRepository.findAllByOrderByFechaDescIdDesc();
    }

    public List<HistorialSatelitalFilaDto> listarHistorialParaVista() {
        return listarHistorial().stream()
                .map(indice -> new HistorialSatelitalFilaDto(
                        indice.getId(),
                        formatearFecha(indice.getFecha()),
                        formatearDecimal(indice.getNdvi()),
                        formatearDecimal(indice.getNdwi()),
                        textoSeguro(indice.getEstadoVegetacion()),
                        claseEstado(indice.getEstadoVegetacion()),
                        textoSeguro(indice.getEstadoHidrico()),
                        claseEstado(indice.getEstadoHidrico()),
                        claseNdvi(indice.getNdvi())
                ))
                .toList();
    }

    public ResumenSatelitalDto obtenerResumenActual() {
        IndiceSatelital ultimo = indiceSatelitalRepository.findTopByOrderByFechaDescIdDesc().orElse(null);
        if (ultimo == null) {
            return new ResumenSatelitalDto("N/A", "Sin dato", "N/A", "Sin dato", "badge-moderado", "Sin dato", "badge-moderado", "N/A");
        }

        return new ResumenSatelitalDto(
                formatearDecimal(ultimo.getNdvi()),
                clasificacionNdvi(ultimo.getNdvi()),
                formatearDecimal(ultimo.getNdwi()),
                textoSeguro(ultimo.getEstadoVegetacion()),
                claseEstado(ultimo.getEstadoVegetacion()),
                textoSeguro(ultimo.getEstadoHidrico()),
                claseEstado(ultimo.getEstadoHidrico()),
                formatearFecha(ultimo.getFecha())
        );
    }

    private String formatearDecimal(Double valor) {
        return valor == null ? "N/A" : String.format("%.3f", valor);
    }

    private String formatearFecha(LocalDateTime fecha) {
        return fecha == null ? "N/A" : fecha.format(FORMATO_FECHA);
    }

    private String clasificacionNdvi(Double ndvi) {
        if (ndvi == null) return "Sin dato";
        if (ndvi >= 0.6) return "NDVI alto";
        if (ndvi >= 0.3) return "NDVI medio";
        return "NDVI bajo";
    }

    private String textoSeguro(String valor) {
        return (valor == null || valor.isBlank()) ? "Sin dato" : valor;
    }

    private String claseNdvi(Double ndvi) {
        if (ndvi == null) return "badge-moderado";
        if (ndvi >= 0.6) return "badge-saludable";
        if (ndvi >= 0.3) return "badge-moderado";
        return "badge-critico";
    }

    private String claseEstado(String estado) {
        if (estado == null) return "badge-moderado";
        String e = estado.trim().toLowerCase();
        if (e.contains("óptimo") || e.contains("optimo")) return "badge-optimo";
        if (e.contains("saludable")) return "badge-saludable";
        if (e.contains("critic")) return "badge-critico";
        if (e.contains("moderad")) return "badge-moderado";
        return "badge-moderado";
    }

    public record ResumenSatelitalDto(String ndviTexto,
                                      String ndviNivel,
                                      String ndwiTexto,
                                      String estadoVegetacion,
                                      String estadoVegetacionClase,
                                      String estadoHidrico,
                                      String estadoHidricoClase,
                                      String fechaTexto) {
    }

    public record HistorialSatelitalFilaDto(Long id,
                                            String fechaTexto,
                                            String ndviTexto,
                                            String ndwiTexto,
                                            String estadoVegetacion,
                                            String estadoVegetacionClase,
                                            String estadoHidrico,
                                            String estadoHidricoClase,
                                            String ndviClase) {
    }
}
