package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.entidades.VwMedicionDetalle;
import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import com.dv.agro_web.servicios.EstacionService;
import com.dv.agro_web.servicios.UiEstacionSensorService;
import com.dv.agro_web.servicios.UiEstacionService;
import org.springframework.data.domain.Page;
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
    private final EstacionService estacionService;
    private final UiEstacionService uiEstacionService;
    private final UiEstacionSensorService uiEstacionSensorService;

    public MedicionesController(VwMedicionDetalleRepository repo,
                                EstacionService estacionService,
                                UiEstacionService uiEstacionService,
                                UiEstacionSensorService uiEstacionSensorService) {
        this.repo = repo;
        this.estacionService = estacionService;
        this.uiEstacionService = uiEstacionService;
        this.uiEstacionSensorService = uiEstacionSensorService;
    }

    @GetMapping("/mediciones")
    public String verMediciones(
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {

        cargarDashboard(limit, page, model);

        return "mediciones";
    }

    @GetMapping("/historial")
    public String verHistorial(
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {

        cargarHistorial(limit, page, model);

        return "historial";
    }

    private void cargarDashboard(int limit, int page, Model model) {
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;
        if (page < 0) page = 0;

        List<String> codigosEstacionesActivas = estacionService.obtenerEstacionesActivas().stream()
                .map(estacion -> estacion.getCodigo())
                .toList();

        List<String> codigosActivos = uiEstacionService.obtenerCodigosActivos().stream()
                .filter(codigosEstacionesActivas::contains)
                .toList();
        uiEstacionSensorService.asegurarSensoresRegistrados(codigosEstacionesActivas);

        BigDecimal tempAvg = codigosActivos.isEmpty()
                ? null
                : repo.avgUltimas40TempAmbientalSoloActivas(codigosActivos);

        BigDecimal humAvg = codigosActivos.isEmpty()
                ? null
                : repo.avgUltimas40HumedadAmbientalSoloActivas(codigosActivos);

        String tempPromedioTxt = (tempAvg == null)
                ? "--"
                : tempAvg.setScale(1, RoundingMode.HALF_UP).toPlainString() + "°C";

        String humPromedioTxt = (humAvg == null)
                ? "--"
                : humAvg.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";

        long sensoresActivos = uiEstacionSensorService.contarSensoresActivos();
        long sensoresRegistrados = uiEstacionSensorService.contarSensoresRegistrados();

        model.addAttribute("sensoresActivos", sensoresActivos);
        model.addAttribute("sensoresRegistrados", sensoresRegistrados);
        model.addAttribute("tempPromedioTxt", tempPromedioTxt);
        model.addAttribute("humPromedioTxt", humPromedioTxt);
        model.addAttribute("limit", limit);
        model.addAttribute("page", Page.empty(PageRequest.of(page, limit)));
        model.addAttribute("mediciones", List.of());
        model.addAttribute("estacionesDashboard", construirCardsPorEstacion());
    }

    private void cargarHistorial(int limit, int page, Model model) {
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;
        if (page < 0) page = 0;

        List<String> codigosEstacionesActivas = estacionService.obtenerEstacionesActivas().stream()
                .map(estacion -> estacion.getCodigo())
                .toList();

        List<String> codigosActivos = uiEstacionService.obtenerCodigosActivos().stream()
                .filter(codigosEstacionesActivas::contains)
                .toList();

        Page<VwMedicionDetalle> pageResult = codigosActivos.isEmpty()
                ? Page.empty(PageRequest.of(page, limit))
                : repo.findByEstacionCodigoInAndSensoresActivosOrderByFechaMedicionDesc(codigosActivos, PageRequest.of(page, limit));

        model.addAttribute("tempPromedioTxt", "--");
        model.addAttribute("humPromedioTxt", "--");
        model.addAttribute("limit", limit);
        model.addAttribute("page", pageResult);
        model.addAttribute("mediciones", pageResult.getContent());
        model.addAttribute("estacionesDashboard", List.of());
    }

    private List<EstacionDashboardDto> construirCardsPorEstacion() {
        List<Estacion> estacionesActivas = estacionService.obtenerEstacionesActivas();
        List<String> codigosEstacionesActivas = estacionesActivas.stream()
                .map(Estacion::getCodigo)
                .toList();

        List<VwMedicionDetalle> ultimas = repo.findUltimasMedicionesPorEstacionYTipoSensor();
        Map<String, Boolean> estadosUi = uiEstacionService.obtenerEstadosPorCodigo();
        Map<String, Map<String, Boolean>> estadosSensoresPorEstacion =
                uiEstacionSensorService.obtenerEstadosPorEstaciones(codigosEstacionesActivas);

        Map<String, Map<String, VwMedicionDetalle>> ultimasPorEstacion = new LinkedHashMap<>();
        for (VwMedicionDetalle medicion : ultimas) {
            ultimasPorEstacion
                    .computeIfAbsent(medicion.getEstacionCodigo(), key -> new LinkedHashMap<>())
                    .put(medicion.getTipoSensor(), medicion);
        }

        List<EstacionDashboardDto> cards = new ArrayList<>();

        for (Estacion estacion : estacionesActivas) {
            String codigoEstacion = estacion.getCodigo();
            Map<String, VwMedicionDetalle> porTipo =
                    ultimasPorEstacion.getOrDefault(codigoEstacion, Map.of());
            Map<String, Boolean> estadosSensores =
                    estadosSensoresPorEstacion.getOrDefault(codigoEstacion, Map.of());

            List<SensorValorDto> sensores = new ArrayList<>();
            for (String tipoSensor : TIPOS_SENSOR_DASHBOARD) {
                VwMedicionDetalle medicion = porTipo.get(tipoSensor);
                boolean activo = estadosSensores.getOrDefault(tipoSensor, true);
                sensores.add(new SensorValorDto(tipoSensor, formatearValor(medicion), activo));
            }

            String descripcion = (estacion.getDescripcion() == null || estacion.getDescripcion().isBlank())
                    ? codigoEstacion
                    : estacion.getDescripcion();

            cards.add(new EstacionDashboardDto(
                    codigoEstacion,
                    descripcion,
                    estadosUi.getOrDefault(codigoEstacion, true),
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

    public record SensorValorDto(String tipoSensor, String valor, boolean activo) {}

    public record EstacionDashboardDto(
            String estacionCodigo,
            String estacionDescripcion,
            Boolean activa,
            List<SensorValorDto> sensores
    ) {}
}