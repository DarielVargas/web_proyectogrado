package com.dv.agro_web.controllers;

import com.dv.agro_web.controllers.forms.EstacionForm;
import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.entidades.VwMedicionDetalle;
import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import com.dv.agro_web.servicios.EstacionService;
import com.dv.agro_web.servicios.UiEstacionSensorService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class EstacionesController {

    private static final List<String> TIPOS_SENSOR_DASHBOARD = List.of(
            "Temperatura Ambiental",
            "Humedad Ambiental",
            "Humedad del Suelo",
            "Intensidad de Luz Solar",
            "pH del Suelo",
            "Conductividad Eléctrica",
            "Nitrógeno (N)",
            "Fósforo (P)",
            "Potasio (K)"
    );

    private final EstacionService estacionService;
    private final UiEstacionService uiEstacionService;
    private final UiEstacionSensorService uiEstacionSensorService;
    private final VwMedicionDetalleRepository medicionDetalleRepository;

    public EstacionesController(EstacionService estacionService,
                                UiEstacionService uiEstacionService,
                                UiEstacionSensorService uiEstacionSensorService,
                                VwMedicionDetalleRepository medicionDetalleRepository) {
        this.estacionService = estacionService;
        this.uiEstacionService = uiEstacionService;
        this.uiEstacionSensorService = uiEstacionSensorService;
        this.medicionDetalleRepository = medicionDetalleRepository;
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
        uiEstacionSensorService.asegurarSensoresRegistrados(List.of(estacion.getCodigo()));

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
        Estacion estacion = estacionService.obtenerEstacionPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estación no encontrada"));
        uiEstacionSensorService.asegurarSensoresRegistrados(List.of(estacion.getCodigo()));

        Map<String, Map<String, VwMedicionDetalle>> ultimasPorEstacion = new LinkedHashMap<>();
        for (VwMedicionDetalle medicion : medicionDetalleRepository.findUltimasMedicionesPorEstacionYTipoSensor()) {
            ultimasPorEstacion
                    .computeIfAbsent(medicion.getEstacionCodigo(), key -> new LinkedHashMap<>())
                    .put(medicion.getTipoSensor(), medicion);
        }

        Map<String, VwMedicionDetalle> porTipo = ultimasPorEstacion.getOrDefault(estacion.getCodigo(), Map.of());
        Map<String, Boolean> estadosSensores = uiEstacionSensorService.obtenerEstadosPorEstacion(estacion.getCodigo());

        List<SensorValorConfigDto> sensores = new ArrayList<>();
        for (String tipoSensor : TIPOS_SENSOR_DASHBOARD) {
            sensores.add(new SensorValorConfigDto(
                    tipoSensor,
                    formatearValor(porTipo.get(tipoSensor)),
                    estadosSensores.getOrDefault(tipoSensor, true)
            ));
        }

        model.addAttribute("estacion", estacion);
        model.addAttribute("sensores", sensores);
        return "estaciones-configurar";
    }

    @PostMapping("/estaciones/{id}/configurar/sensores")
    public String actualizarEstadoSensor(@PathVariable Long id,
                                         @RequestParam("tipoSensor") String tipoSensor,
                                         @RequestParam("activo") boolean activo) {
        Estacion estacion = estacionService.obtenerEstacionPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estación no encontrada"));

        uiEstacionSensorService.actualizarEstado(estacion.getCodigo(), tipoSensor, activo);
        return "redirect:/estaciones/{id}/configurar";
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

    private String formatearValor(VwMedicionDetalle medicion) {
        if (medicion == null || medicion.getValor() == null) {
            return "--";
        }

        String valor = medicion.getValor().stripTrailingZeros().toPlainString();
        if (medicion.getUnidadMedida() == null || medicion.getUnidadMedida().isBlank()) {
            return valor;
        }

        return valor + " " + medicion.getUnidadMedida();
    }

    public record EstacionItemDto(Estacion estacion, boolean activa) {}

    public record SensorValorConfigDto(String tipoSensor, String valor, boolean activo) {}
}