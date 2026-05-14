package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.CalendarioAgricola;
import com.dv.agro_web.servicios.CalendarioAgricolaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class CalendarioAgricolaController {

    private final CalendarioAgricolaService calendarioAgricolaService;

    public CalendarioAgricolaController(CalendarioAgricolaService calendarioAgricolaService) {
        this.calendarioAgricolaService = calendarioAgricolaService;
    }

    @GetMapping("/calendario")
    public String verCalendario(@RequestParam(value = "estado", required = false) String estado, Model model) {
        String estadoActivo = calendarioAgricolaService.normalizarEstado(estado);
        List<CalendarioAgricola> tareas = calendarioAgricolaService.listarPorEstado(estadoActivo);

        model.addAttribute("tareasCalendario", tareas);
        model.addAttribute("estadoActivo", estadoActivo);
        model.addAttribute("conteoTareas", calendarioAgricolaService.contarPorEstado());
        model.addAttribute("tiposTarea", CalendarioAgricolaService.TIPOS_TAREA);
        model.addAttribute("calendarioService", calendarioAgricolaService);
        return "calendario";
    }

    @PostMapping("/calendario")
    public String crearTarea(@RequestParam("titulo") String titulo,
                             @RequestParam(value = "descripcion", required = false) String descripcion,
                             @RequestParam("tipo") String tipo,
                             @RequestParam("fechaHora") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
                             @RequestParam(value = "asignadoA", required = false) String asignadoA,
                             @RequestParam(value = "duracionMinutos", required = false) Integer duracionMinutos,
                             RedirectAttributes redirectAttributes) {
        try {
            calendarioAgricolaService.crearTarea(titulo, descripcion, tipo, fechaHora, asignadoA, duracionMinutos);
            redirectAttributes.addFlashAttribute("mensajeExito", "Tarea agrícola creada correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
            redirectAttributes.addFlashAttribute("abrirModalTarea", true);
        }
        return "redirect:/calendario";
    }

    @PostMapping("/calendario/completar/{id}")
    public String completarTarea(@PathVariable("id") Long id,
                                 @RequestParam(value = "estado", required = false) String estado,
                                 RedirectAttributes redirectAttributes) {
        calendarioAgricolaService.marcarComoCompletada(id);
        redirectAttributes.addFlashAttribute("mensajeExito", "Tarea marcada como completada.");
        return "redirect:/calendario?estado=" + calendarioAgricolaService.normalizarEstado(estado);
    }
}
