package com.dv.agro_web.controllers;

import com.dv.agro_web.controllers.forms.InformacionCultivoForm;
import com.dv.agro_web.entidades.InformacionCultivo;
import com.dv.agro_web.servicios.EstacionService;
import com.dv.agro_web.servicios.InformacionCultivoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
public class InformacionCultivoController {

    private final InformacionCultivoService informacionCultivoService;
    private final EstacionService estacionService;

    public InformacionCultivoController(InformacionCultivoService informacionCultivoService,
                                        EstacionService estacionService) {
        this.informacionCultivoService = informacionCultivoService;
        this.estacionService = estacionService;
    }

    @GetMapping("/informacion-cultivo")
    public String verInformacionCultivo(Model model) {
        InformacionCultivoForm form = informacionCultivoService.obtenerFormularioActual();
        model.addAttribute("informacionCultivoForm", form);
        agregarAtributosVista(model, form);
        return "informacion-cultivo";
    }

    @PostMapping("/informacion-cultivo")
    public String guardarInformacionCultivo(@Valid @ModelAttribute("informacionCultivoForm") InformacionCultivoForm form,
                                            BindingResult bindingResult,
                                            Model model,
                                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            agregarAtributosVista(model, form);
            return "informacion-cultivo";
        }

        try {
            informacionCultivoService.guardar(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("estacionId", "estacion.invalida", ex.getMessage());
            agregarAtributosVista(model, form);
            return "informacion-cultivo";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Información del cultivo guardada correctamente.");
        return "redirect:/informacion-cultivo";
    }

    private void agregarAtributosVista(Model model, InformacionCultivoForm form) {
        LocalDate fechaInicio = form.getFechaInicio();
        LocalDate fechaCosecha = form.getFechaCosechaEstimada();
        if (fechaCosecha == null && fechaInicio != null) {
            fechaCosecha = fechaInicio.plusMonths(10);
            form.setFechaCosechaEstimada(fechaCosecha);
        }

        LocalDateTime ultimaSupervision = informacionCultivoService.obtenerInformacionPrincipal()
                .map(InformacionCultivo::getUltimaSupervision)
                .orElse(null);

        model.addAttribute("estacionesCultivo", estacionService.obtenerEstacionesRegistradas());
        model.addAttribute("lotesCultivo", informacionCultivoService.obtenerLotesDisponibles(form.getLoteArea()));
        model.addAttribute("ultimaSupervisionTexto", formatearUltimaSupervision(ultimaSupervision));
    }

    private String formatearUltimaSupervision(LocalDateTime fecha) {
        if (fecha == null) {
            return "Sin supervisión registrada";
        }

        String hora = fecha.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
        LocalDate hoy = LocalDate.now();
        if (fecha.toLocalDate().isEqual(hoy)) {
            return "Hoy • " + hora;
        }
        if (fecha.toLocalDate().isEqual(hoy.minusDays(1))) {
            return "Ayer • " + hora;
        }

        String dia = fecha.format(DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "ES")));
        return dia + " • " + hora;
    }
}