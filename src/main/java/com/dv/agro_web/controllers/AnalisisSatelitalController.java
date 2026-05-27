package com.dv.agro_web.controllers;

import com.dv.agro_web.servicios.SatelitalService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AnalisisSatelitalController {

    private final SatelitalService satelitalService;

    public AnalisisSatelitalController(SatelitalService satelitalService) {
        this.satelitalService = satelitalService;
    }

    @GetMapping("/analisis-satelital")
    public String verAnalisisSatelital(@RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "limit", defaultValue = "10") int limit,
                                      Model model) {
        Page<SatelitalService.HistorialIndiceDto> historialPage = satelitalService.listarHistorialPresentacionPaginado(page, limit);

        model.addAttribute("historialIndices", historialPage.getContent());
        model.addAttribute("historialPage", historialPage);
        model.addAttribute("historialLimit", historialPage.getSize());
        model.addAttribute("resumenSatelital", satelitalService.obtenerResumenActual());
        return "analisis-satelital";
    }
}