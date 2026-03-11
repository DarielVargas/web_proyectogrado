package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.Alerta;
import com.dv.agro_web.entidades.TipoSensor;
import com.dv.agro_web.repositorios.TipoSensorRepository;
import com.dv.agro_web.servicios.AlertaService;
import com.dv.agro_web.servicios.EstacionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class AlertasController {

    private final AlertaService alertaService;
    private final EstacionService estacionService;
    private final TipoSensorRepository tipoSensorRepository;

    public AlertasController(AlertaService alertaService,
                             EstacionService estacionService,
                             TipoSensorRepository tipoSensorRepository) {
        this.alertaService = alertaService;
        this.estacionService = estacionService;
        this.tipoSensorRepository = tipoSensorRepository;
    }

    @GetMapping("/alertas")
    public String verAlertas(Model model) {
        cargarPantallaAlertas(model);
        return "alertas";
    }

    @PostMapping("/alertas")
    public String guardarAlerta(@RequestParam("estacionCodigo") String estacionCodigo,
                                @RequestParam("sensorTipo") String sensorTipo,
                                @RequestParam("operador") String operador,
                                @RequestParam("umbral") BigDecimal umbral,
                                RedirectAttributes redirectAttributes) {

        if (estacionCodigo == null || estacionCodigo.isBlank()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Seleccione una estación activa.");
            redirectAttributes.addFlashAttribute("abrirModalAlerta", true);
            return "redirect:/alertas";
        }

        if (sensorTipo == null || sensorTipo.isBlank()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Seleccione un sensor.");
            redirectAttributes.addFlashAttribute("abrirModalAlerta", true);
            return "redirect:/alertas";
        }

        if (!List.of(">", "<", "=").contains(operador)) {
            redirectAttributes.addFlashAttribute("mensajeError", "Seleccione un tipo de alarma válido.");
            redirectAttributes.addFlashAttribute("abrirModalAlerta", true);
            return "redirect:/alertas";
        }

        alertaService.guardarNuevaAlerta(estacionCodigo, sensorTipo, operador, umbral);
        redirectAttributes.addFlashAttribute("mensajeExito", "Alarma guardada correctamente.");
        return "redirect:/alertas";
    }


    @PostMapping("/alertas/eliminar/{id}")
    public String eliminarAlerta(@PathVariable("id") Long idAlerta,
                                 RedirectAttributes redirectAttributes) {
        alertaService.eliminarAlertaPorId(idAlerta);
        redirectAttributes.addFlashAttribute("mensajeExito", "Alerta eliminada correctamente");
        return "redirect:/alertas";
    }

    @PostMapping("/alertas/eliminar-todas")
    public String eliminarTodasLasAlertas(RedirectAttributes redirectAttributes) {
        alertaService.eliminarTodasLasAlertas();
        redirectAttributes.addFlashAttribute("mensajeExito", "Todas las alertas eliminadas");
        return "redirect:/alertas";
    }

    @PostMapping("/alertas/historial/eliminar/{id}")
    public String eliminarHistorial(@PathVariable("id") Long idHistorial,
                                    RedirectAttributes redirectAttributes) {
        alertaService.eliminarHistorialPorId(idHistorial);
        redirectAttributes.addFlashAttribute("mensajeExito", "Registro del historial eliminado correctamente");
        return "redirect:/alertas";
    }

    @PostMapping("/alertas/historial/eliminar-todas")
    public String eliminarTodoElHistorial(RedirectAttributes redirectAttributes) {
        alertaService.eliminarTodoElHistorial();
        redirectAttributes.addFlashAttribute("mensajeExito", "Historial de alertas eliminado");
        return "redirect:/alertas";
    }

    private void cargarPantallaAlertas(Model model) {
        List<Alerta> alertas = alertaService.listarAlertasConfiguradas();

        List<EstacionOpcionDto> estaciones = estacionService.obtenerEstacionesActivas().stream()
                .map(est -> new EstacionOpcionDto(
                        est.getCodigo(),
                        (est.getDescripcion() == null || est.getDescripcion().isBlank())
                                ? est.getCodigo()
                                : est.getDescripcion()
                ))
                .toList();

        List<String> sensores = tipoSensorRepository.findAll().stream()
                .map(TipoSensor::getNombre)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .toList();

        model.addAttribute("alertasConfiguradas", alertas);
        model.addAttribute("totalAlertas", alertas.size());
        model.addAttribute("historialAlertas", alertaService.listarHistorialAlertas());
        model.addAttribute("estacionesAlerta", estaciones);
        model.addAttribute("sensoresAlerta", sensores);
    }

    public record EstacionOpcionDto(String codigo, String descripcion) {}
}