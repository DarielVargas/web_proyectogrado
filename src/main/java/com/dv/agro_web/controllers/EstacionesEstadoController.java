package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.servicios.EstacionService;
import com.dv.agro_web.servicios.UiEstacionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estaciones")
public class EstacionesEstadoController {

    private final EstacionService estacionService;
    private final UiEstacionService uiEstacionService;

    public EstacionesEstadoController(EstacionService estacionService,
                                      UiEstacionService uiEstacionService) {
        this.estacionService = estacionService;
        this.uiEstacionService = uiEstacionService;
    }

    @GetMapping("/estados")
    public List<EstadoEstacionDto> listarEstados() {
        Map<String, Boolean> estados = uiEstacionService.obtenerEstadosPorCodigo();

        return estacionService.obtenerEstacionesActivas().stream()
                .map(Estacion::getCodigo)
                .map(codigo -> new EstadoEstacionDto(codigo, estados.getOrDefault(codigo, true)))
                .toList();
    }

    public record EstadoEstacionDto(String estacionCodigo, boolean activa) {
    }
}
