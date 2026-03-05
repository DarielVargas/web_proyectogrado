package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.entidades.UiEstacionSensor;
import com.dv.agro_web.servicios.EstacionService;
import com.dv.agro_web.servicios.UiEstacionSensorService;
import com.dv.agro_web.servicios.UiEstacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Controller
public class EstacionesController {

    private final EstacionService estacionService;
    private final UiEstacionService uiEstacionService;
    private final UiEstacionSensorService uiEstacionSensorService;

    public EstacionesController(
            EstacionService estacionService,
            UiEstacionService uiEstacionService,
            UiEstacionSensorService uiEstacionSensorService
    ) {
        this.estacionService = estacionService;
        this.uiEstacionService = uiEstacionService;
        this.uiEstacionSensorService = uiEstacionSensorService;
    }

    @GetMapping("/estaciones")
    public String verEstaciones(Model model) {
        Map<String, Boolean> estadosUi = uiEstacionService.obtenerEstadosPorCodigo();

        List<EstacionItemDto> estaciones = estacionService.obtenerEstaciones()
                .stream()
                .sorted(Comparator.comparing(Estacion::getId))
                .map(estacion -> new EstacionItemDto(
                        estacion,
                        estadosUi.getOrDefault(estacion.getCodigo(), true)
                ))
                .toList();

        model.addAttribute("estaciones", estaciones);
        return "estaciones";
    }

    @PostMapping("/estaciones/{id}/toggle")
    public String alternarEstado(@PathVariable Long id) {
        estacionService.obtenerEstacionPorId(id)
                .ifPresent(estacion -> uiEstacionService.alternarEstado(estacion.getCodigo()));

        return "redirect:/estaciones";
    }

    @GetMapping("/estaciones/{codigo}/config")
    public String configurarEstacion(@PathVariable String codigo, Model model) {
        List<SensorConfigDto> sensores = uiEstacionSensorService.listSensoresConfig(codigo)
                .stream()
                .map(sensor -> new SensorConfigDto(
                        sensor.getId().getTipoSensor(),
                        Boolean.TRUE.equals(sensor.getActivo())
                ))
                .toList();

        model.addAttribute("estacionCodigo", codigo);
        model.addAttribute("sensores", sensores);
        return "estaciones/config";
    }

    @PostMapping("/estaciones/{codigo}/sensores/{tipoSensor}/toggle")
    public String toggleSensor(
            @PathVariable String codigo,
            @PathVariable String tipoSensor
    ) {
        uiEstacionSensorService.toggleSensor(codigo, tipoSensor);
        return "redirect:/estaciones/{codigo}/config";
    }

    public record EstacionItemDto(Estacion estacion, boolean activa) {}

    public record SensorConfigDto(String tipoSensor, boolean activo) {}
}
