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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (!model.containsAttribute("informacionCultivoForm")) {
            model.addAttribute("informacionCultivoForm", new InformacionCultivoForm());
        }
        agregarAtributosVista(model);
        return "informacion-cultivo";
    }

    @PostMapping("/informacion-cultivo")
    public String guardarInformacionCultivo(@Valid @ModelAttribute("informacionCultivoForm") InformacionCultivoForm form,
                                            BindingResult bindingResult,
                                            Model model,
                                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modalAbierto", form.getId() == null ? "nuevo" : "editar-" + form.getId());
            model.addAttribute("mensajeFormulario", "Revisa los campos obligatorios antes de guardar.");
            agregarAtributosVista(model);
            return "informacion-cultivo";
        }

        try {
            informacionCultivoService.guardar(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("cultivo.invalido", ex.getMessage());
            model.addAttribute("modalAbierto", form.getId() == null ? "nuevo" : "editar-" + form.getId());
            model.addAttribute("mensajeFormulario", ex.getMessage());
            agregarAtributosVista(model);
            return "informacion-cultivo";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Información del cultivo guardada correctamente.");
        return "redirect:/informacion-cultivo";
    }

    @PostMapping("/informacion-cultivo/{id}/supervision")
    public String registrarSupervision(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            informacionCultivoService.registrarSupervision(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Última supervisión registrada correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/informacion-cultivo";
    }

    @PostMapping("/informacion-cultivo/{id}/eliminar")
    public String eliminarCultivo(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            informacionCultivoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Cultivo eliminado correctamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/informacion-cultivo";
    }

    private void agregarAtributosVista(Model model) {
        var cultivos = informacionCultivoService.obtenerCultivos();
        Map<Long, String> supervisionesTexto = cultivos.stream()
                .collect(Collectors.toMap(InformacionCultivo::getId, cultivo -> formatearUltimaSupervision(cultivo.getUltimaSupervision())));

        model.addAttribute("cultivos", cultivos);
        model.addAttribute("supervisionesTexto", supervisionesTexto);
        model.addAttribute("estacionesCultivo", estacionService.obtenerEstacionesRegistradas());
        model.addAttribute("lotesCultivo", informacionCultivoService.obtenerLotesDisponibles());
        model.addAttribute("fechaActual", LocalDate.now());
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
