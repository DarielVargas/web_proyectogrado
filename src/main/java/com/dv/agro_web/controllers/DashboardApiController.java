package com.dv.agro_web.controllers;

import com.dv.agro_web.servicios.DashboardTiempoRealService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final DashboardTiempoRealService dashboardTiempoRealService;

    public DashboardApiController(DashboardTiempoRealService dashboardTiempoRealService) {
        this.dashboardTiempoRealService = dashboardTiempoRealService;
    }

    @GetMapping("/resumen")
    public DashboardTiempoRealService.DashboardSnapshotDto obtenerResumen() {
        return dashboardTiempoRealService.obtenerSnapshot();
    }
}