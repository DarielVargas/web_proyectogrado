package com.dv.agro_web.controllers;

import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MedicionesController {

    private final VwMedicionDetalleRepository repo;

    public MedicionesController(VwMedicionDetalleRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/mediciones")
    public String verMediciones(
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            Model model
    ) {

        if (limit < 1) limit = 1;
        if (limit > 500) limit = 500;

        var page = repo.findAllByOrderByFechaMedicionDesc(PageRequest.of(0, limit));

        model.addAttribute("limit", limit);
        model.addAttribute("mediciones", page.getContent());

        return "mediciones";
    }
}