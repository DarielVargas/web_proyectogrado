package com.dv.agro_web.controllers;

import com.dv.agro_web.servicios.SatelitalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalisisSatelitalController {

    private final SatelitalService satelitalService;

    public AnalisisSatelitalController(SatelitalService satelitalService) {
        this.satelitalService = satelitalService;
    }

    @GetMapping("/analisis-satelital")
    public String verAnalisisSatelital(Model model) {
        model.addAttribute("historialIndices", satelitalService.listarHistorialPresentacion());
        model.addAttribute("resumenSatelital", satelitalService.obtenerResumenActual());
        return "analisis-satelital";
    }
}