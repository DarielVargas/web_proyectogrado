package com.dv.agro_web.controllers;

import com.dv.agro_web.servicios.AlertaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertasNotificacionController {

    private final AlertaService alertaService;

    public AlertasNotificacionController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping("/notificaciones")
    public List<AlertaService.NotificacionAlertaDto> obtenerNotificaciones(HttpSession session) {
        return alertaService.obtenerAlertasDisparadas(session);
    }
}
