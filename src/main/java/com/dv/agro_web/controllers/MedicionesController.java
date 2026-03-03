package com.dv.agro_web.controllers;

import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;

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

        return "mediciones";
    }
}