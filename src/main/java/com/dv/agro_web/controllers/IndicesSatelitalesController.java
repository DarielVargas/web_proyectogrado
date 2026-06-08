package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.IndiceSatelital;
import com.dv.agro_web.servicios.SatelitalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/indices-satelitales")
public class IndicesSatelitalesController {

    private final SatelitalService satelitalService;

    public IndicesSatelitalesController(SatelitalService satelitalService) {
        this.satelitalService = satelitalService;
    }

    @GetMapping("/ultimo")
    public ResponseEntity<UltimoAnalisisResponse> obtenerUltimoAnalisis() {
        return satelitalService.obtenerFechaUltimoAnalisis()
                .map(fecha -> ResponseEntity.ok(new UltimoAnalisisResponse(fecha)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IndiceSatelital registrarIndiceSatelital(@RequestBody IndiceSatelitalRequest request) {
        return satelitalService.guardarIndiceSatelital(request);
    }

    public record UltimoAnalisisResponse(LocalDateTime fecha) {
    }
}