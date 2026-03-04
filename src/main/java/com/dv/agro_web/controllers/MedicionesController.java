package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.VwMedicionDetalle;
import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MedicionesController {

    private static final List<String> TIPOS_SENSOR_DASHBOARD = List.of(
            "Temperatura Ambiental",
            "Humedad Ambiental",
            "Humedad del Suelo",
            "Intensidad de Luz Solar",
            "pH del Suelo",
            "Conductividad Eléctrica",
            "Nitrógeno (N)",
            "Fósforo (P)",
            "Potasio (K)"
    );

    private final VwMedicionDetalleRepository repo;

    public MedicionesController(VwMedicionDetalleRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/mediciones")
    public String verMediciones(
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {

        cargarDatosPaginados(limit, page, model);

        return "mediciones";
    }

    @GetMapping("/historial")
    public String verHistorial(
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {

        cargarDatosPaginados(limit, page, model);

        return "historial";
    }

    private void cargarDatosPaginados(int limit, int page, Model model) {

        // Seguridad básica
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;
        if (page < 0) page = 0;

        var pageResult = repo.findAllByOrderByFechaMedicionDesc(
                PageRequest.of(page, limit)
        );

        // ================================
        // PROMEDIOS ÚLTIMAS 40 MEDICIONES
        // ================================
        BigDecimal tempAvg = repo.avgUltimas40TempAmbiental();
        BigDecimal humAvg  = repo.avgUltimas40HumedadAmbiental();

        String tempPromedioTxt = (tempAvg == null)
                ? "--"
                : tempAvg.setScale(1, RoundingMode.HALF_UP).toPlainString() + "°C";

        String humPromedioTxt = (humAvg == null)
                ? "--"
                : humAvg.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";

        model.addAttribute("tempPromedioTxt", tempPromedioTxt);
        model.addAttribute("humPromedioTxt", humPromedioTxt);

        model.addAttribute("limit", limit);
        model.addAttribute("page", pageResult);
        model.addAttribute("mediciones", pageResult.getContent());
        model.addAttribute("estacionesDashboard", construirCardsPorEstacion());

    }

    private List<EstacionDashboardDto> construirCardsPorEstacion() {
        List<VwMedicionDetalleRepository.EstacionResumen> estaciones = repo.findEstacionesConDatos();
        List<VwMedicionDetalle> ultimas = repo.findUltimasMedicionesPorEstacionYTipoSensor();

        Map<String, Map<String, VwMedicionDetalle>> ultimasPorEstacion = new LinkedHashMap<>();
        for (VwMedicionDetalle medicion : ultimas) {
            ultimasPorEstacion
                    .computeIfAbsent(medicion.getEstacionCodigo(), key -> new LinkedHashMap<>())
                    .put(medicion.getTipoSensor(), medicion);
        }

        List<EstacionDashboardDto> cards = new ArrayList<>();

        for (VwMedicionDetalleRepository.EstacionResumen estacion : estaciones) {
            Map<String, VwMedicionDetalle> porTipo =
                    ultimasPorEstacion.getOrDefault(estacion.getEstacionCodigo(), Map.of());

            List<SensorValorDto> sensores = new ArrayList<>();
            for (String tipoSensor : TIPOS_SENSOR_DASHBOARD) {
                VwMedicionDetalle medicion = porTipo.get(tipoSensor);
                sensores.add(new SensorValorDto(tipoSensor, formatearValor(medicion)));
            }

            cards.add(new EstacionDashboardDto(
                    estacion.getEstacionCodigo(),
                    estacion.getEstacionDescripcion(),
                    sensores
            ));
        }

        return cards;
    }

    private String formatearValor(VwMedicionDetalle medicion) {
        if (medicion == null || medicion.getValor() == null) {
            return "--";
        }

        String valor = medicion.getValor()
                .stripTrailingZeros()
                .toPlainString();

        if (medicion.getUnidadMedida() == null || medicion.getUnidadMedida().isBlank()) {
            return valor;
        }

        return valor + " " + medicion.getUnidadMedida();
    }

    public record SensorValorDto(String tipoSensor, String valor) {}

    public record EstacionDashboardDto(
            String estacionCodigo,
            String estacionDescripcion,
            List<SensorValorDto> sensores
    ) {}

}