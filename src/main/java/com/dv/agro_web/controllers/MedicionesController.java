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
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {

        // Seguridad básica
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;
        if (page < 0) page = 0;

        var pageResult = repo.findAllByOrderByFechaMedicionDesc(
                PageRequest.of(page, limit)
        );

        model.addAttribute("limit", limit);
        model.addAttribute("page", pageResult); // importante para paginación
        model.addAttribute("mediciones", pageResult.getContent());

        return "mediciones";
    }
}