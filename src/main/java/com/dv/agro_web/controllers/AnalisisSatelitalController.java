package com.dv.agro_web.controllers;

import com.dv.agro_web.servicios.ImagenSatelitalService;
import com.dv.agro_web.servicios.SatelitalService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Controller
public class AnalisisSatelitalController {

    private final SatelitalService satelitalService;
    private final ImagenSatelitalService imagenSatelitalService;

    public AnalisisSatelitalController(SatelitalService satelitalService,
                                      ImagenSatelitalService imagenSatelitalService) {
        this.satelitalService = satelitalService;
        this.imagenSatelitalService = imagenSatelitalService;
    }

    @GetMapping("/analisis-satelital")
    public String verAnalisisSatelital(@RequestParam(name = "page", defaultValue = "0") int page,
                                      @RequestParam(name = "limit", defaultValue = "10") int limit,
                                      @RequestParam(name = "filtro", defaultValue = "todo") String filtro,
                                      @RequestParam(name = "fecha", required = false) LocalDate fecha,
                                      @RequestParam(name = "fechaInicio", required = false) LocalDate fechaInicio,
                                      @RequestParam(name = "fechaFin", required = false) LocalDate fechaFin,
                                      Model model) {
        Page<SatelitalService.HistorialIndiceDto> historialPage = satelitalService.listarHistorialPresentacionPaginado(page, limit, filtro, fecha, fechaInicio, fechaFin);

        model.addAttribute("historialIndices", historialPage.getContent());
        model.addAttribute("historialPage", historialPage);
        model.addAttribute("historialLimit", historialPage.getSize());
        model.addAttribute("filtroHistorial", filtro);
        model.addAttribute("fechaFiltro", fecha);
        model.addAttribute("fechaInicioFiltro", fechaInicio);
        model.addAttribute("fechaFinFiltro", fechaFin);
        model.addAttribute("resumenSatelital", satelitalService.obtenerResumenActual());
        model.addAttribute("imagenSatelitalReciente", imagenSatelitalService.obtenerImagenMasReciente().orElse(null));
        return "analisis-satelital";
    }

    @GetMapping(value = "/analisis-satelital/historial/descargar", produces = "text/csv")
    public ResponseEntity<byte[]> descargarHistorial(@RequestParam(name = "filtro", defaultValue = "todo") String filtro,
                                                     @RequestParam(name = "fecha", required = false) LocalDate fecha,
                                                     @RequestParam(name = "fechaInicio", required = false) LocalDate fechaInicio,
                                                     @RequestParam(name = "fechaFin", required = false) LocalDate fechaFin) {
        List<SatelitalService.HistorialIndiceDto> datos = satelitalService.listarHistorialFiltradoParaDescarga(filtro, fecha, fechaInicio, fechaFin);

        StringBuilder csv = new StringBuilder("ID,Fecha,NDVI,NDWI,Estado Vegetación,Estado Hídrico\n");
        for (SatelitalService.HistorialIndiceDto i : datos) {
            csv.append(i.id()).append(',')
                    .append(i.fechaTexto()).append(',')
                    .append(i.ndvi() != null ? String.format("%.3f", i.ndvi()) : "N/A").append(',')
                    .append(i.ndwi() != null ? String.format("%.3f", i.ndwi()) : "N/A").append(',')
                    .append(i.estadoVegetacion()).append(',')
                    .append(i.estadoHidrico()).append("\n");
        }

        byte[] body = csv.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=historial_indices_satelitales.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }
}