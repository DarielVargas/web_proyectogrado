package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.IndiceSatelital;
import com.dv.agro_web.servicios.SatelitalService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/indices-satelitales")
public class IndicesSatelitalesController {

    private final SatelitalService satelitalService;

    public IndicesSatelitalesController(SatelitalService satelitalService) {
        this.satelitalService = satelitalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IndiceSatelital registrarIndiceSatelital(@RequestBody IndiceSatelitalRequest request) {
        return satelitalService.guardarIndiceSatelital(request);
    }
}