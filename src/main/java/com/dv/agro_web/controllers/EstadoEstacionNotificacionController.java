package com.dv.agro_web.controllers;

import com.dv.agro_web.servicios.UiEstacionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estaciones")
public class EstadoEstacionNotificacionController {

    private final UiEstacionService uiEstacionService;

    public EstadoEstacionNotificacionController(UiEstacionService uiEstacionService) {
        this.uiEstacionService = uiEstacionService;
    }

    @GetMapping("/estados")
    public List<EstadoEstacionDto> obtenerEstados() {
        return uiEstacionService.obtenerEstadosPorCodigo().entrySet().stream()
                .map(entry -> new EstadoEstacionDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    public record EstadoEstacionDto(String estacionCodigo, boolean activa) {}
}