package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.servicios.EstacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Comparator;
import java.util.List;

@Controller
public class EstacionesController {

    private final EstacionService estacionService;

    public EstacionesController(EstacionService estacionService) {
        this.estacionService = estacionService;
    }

    @GetMapping("/estaciones")
    public String verEstaciones(Model model) {
        List<Estacion> estaciones = estacionService.obtenerEstaciones()
                .stream()
                .sorted(Comparator.comparing(Estacion::getId))
                .toList();

        model.addAttribute("estaciones", estaciones);
        return "estaciones";
    }

    @GetMapping("/estaciones/{id}/configurar")
    public String configurarEstacion(@PathVariable Long id, Model model) {
        model.addAttribute("estacionId", id);
        return "estaciones-configurar";
    }
}