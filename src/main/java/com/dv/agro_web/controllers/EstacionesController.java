package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.servicios.EstacionService;
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

    public EstacionesController(EstacionService estacionService, UiEstacionService uiEstacionService) {
        this.estacionService = estacionService;
        this.uiEstacionService = uiEstacionService;
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

    @GetMapping("/estaciones/{id}/configurar")
    public String configurarEstacion(@PathVariable Long id, Model model) {
        model.addAttribute("estacionId", id);
        return "estaciones-configurar";
    }

    public record EstacionItemDto(Estacion estacion, boolean activa) {}
}
