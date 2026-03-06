package com.dv.agro_web.controllers;

import com.dv.agro_web.controllers.forms.EstacionForm;
import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.servicios.EstacionService;
import com.dv.agro_web.servicios.UiEstacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class EstacionesController {

    private final EstacionService estacionService;
    private final UiEstacionService uiEstacionService;

    public EstacionesController(EstacionService estacionService, UiEstacionService uiEstacionService) {
        this.estacionService = estacionService;
        this.uiEstacionService = uiEstacionService;
    }

    @GetMapping("/estaciones")
    public String verEstaciones(Model model) {
        Map<String, Boolean> estadosUi = uiEstacionService.obtenerEstadosPorCodigo();

        List<EstacionItemDto> estaciones = estacionService.obtenerEstaciones()
                .stream()
                .sorted(Comparator.comparing(Estacion::getId))
                .map(estacion -> new EstacionItemDto(
                        estacion,
                        estadosUi.getOrDefault(estacion.getCodigo(), true)
                ))
                .toList();

        model.addAttribute("estaciones", estaciones);
        return "estaciones";
    }

    @GetMapping("/estaciones/nueva")
    public String verFormularioNuevaEstacion(Model model) {
        if (!model.containsAttribute("estacionForm")) {
            model.addAttribute("estacionForm", new EstacionForm());
        }
        return "estaciones-nueva";
    }

    @PostMapping("/estaciones")
    public String crearEstacion(@Valid @ModelAttribute("estacionForm") EstacionForm estacionForm,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasFieldErrors("codigo") && estacionService.existeCodigo(estacionForm.getCodigo())) {
            bindingResult.rejectValue("codigo", "duplicado", "Ese código ya existe");
        }

        if (bindingResult.hasErrors()) {
            return "estaciones-nueva";
        }

        Estacion estacion = new Estacion();
        estacion.setCodigo(estacionForm.getCodigo());
        estacion.setParcelaId(estacionForm.getParcelaId());
        estacion.setDescripcion(estacionForm.getDescripcion());
        estacion.setFechaInstalacion(estacionForm.getFechaInstalacion());
        estacionService.crearEstacion(estacion);

        redirectAttributes.addFlashAttribute("mensajeExito", "Estación creada correctamente");
        return "redirect:/estaciones";
    }

    @PostMapping("/estaciones/{id}/toggle")
    public String alternarEstado(@PathVariable Long id) {
        estacionService.obtenerEstacionPorId(id)
                .ifPresent(estacion -> uiEstacionService.alternarEstado(estacion.getCodigo()));

        return "redirect:/estaciones";
    }

    @GetMapping("/estaciones/{id}/configurar")
    public String configurarEstacion(@PathVariable Long id, Model model) {
        model.addAttribute("estacionId", id);
        return "estaciones-configurar";
    }


    @GetMapping("/estaciones/{codigo}/info")
    public String verInformacionEstacion(@PathVariable String codigo, Model model) {
        Estacion estacion = estacionService.obtenerEstacionPorCodigo(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estación no encontrada"));

        boolean activa = uiEstacionService.obtenerEstadosPorCodigo().getOrDefault(estacion.getCodigo(), true);
        String fechaInstalacion = estacion.getFechaInstalacion() != null
                ? estacion.getFechaInstalacion().format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES")))
                : "Sin fecha registrada";

        model.addAttribute("estacion", estacion);
        model.addAttribute("estadoEstacion", activa ? "Operativa" : "Inactiva");
        model.addAttribute("fechaInstalacionFormateada", fechaInstalacion);
        return "estacion-info";
    }

    public record EstacionItemDto(Estacion estacion, boolean activa) {}
}