package com.dv.agro_web.controllers;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.entidades.Reporte;
import com.dv.agro_web.entidades.VwMedicionDetalle;
import com.dv.agro_web.repositorios.AlertaRepository;
import com.dv.agro_web.repositorios.ReporteRepository;
import com.dv.agro_web.repositorios.SensorRepository;
import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import com.dv.agro_web.servicios.EstacionService;
import com.dv.agro_web.servicios.ReporteService;
import com.dv.agro_web.servicios.UiEstacionSensorService;
import com.dv.agro_web.servicios.UiEstacionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class MedicionesController {

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

    private final VwMedicionDetalleRepository repo;
    private final SensorRepository sensorRepository;
    private final AlertaRepository alertaRepository;
    private final EstacionService estacionService;
    private final ReporteService reporteService;
    private final UiEstacionService uiEstacionService;
    private final UiEstacionSensorService uiEstacionSensorService;

    public MedicionesController(VwMedicionDetalleRepository repo,
                                SensorRepository sensorRepository,
                                AlertaRepository alertaRepository,
                                EstacionService estacionService,
                                ReporteService reporteService,
                                UiEstacionService uiEstacionService,
                                UiEstacionSensorService uiEstacionSensorService) {
        this.repo = repo;
        this.sensorRepository = sensorRepository;
        this.alertaRepository = alertaRepository;
        this.estacionService = estacionService;
        this.reporteService = reporteService;
        this.uiEstacionService = uiEstacionService;
        this.uiEstacionSensorService = uiEstacionSensorService;
    }

    @GetMapping("/mediciones")
    public String verMediciones(
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model
    ) {

        cargarDashboard(limit, page, model);

        return "mediciones";
    }

    @GetMapping("/historial")
    public String verHistorial(Model model) {
        cargarPantallaReportesBase(model);
        model.addAttribute("filtroAplicado", false);

        return "historial";
    }

    @GetMapping("/reportes/{reporteId}")
    public String verDetalleReporte(@PathVariable("reporteId") Long reporteId,
                                    @RequestParam(name = "page", defaultValue = "0") int page,
                                    @RequestParam(name = "limit", defaultValue = "10") int limit,
                                    Model model) {
        cargarPantallaReportesBase(model);
        cargarVistaPreviaReporte(model, reporteId, page, limit);
        return "reporte-detalle";
    }

    @GetMapping("/reportes/{reporteId}/descargar")
    public ResponseEntity<byte[]> descargarReportePdf(@PathVariable("reporteId") Long reporteId) {
        try {
            byte[] pdf = reporteService.generarReportePdf(reporteId);

            ContentDisposition contentDisposition = ContentDisposition.attachment()
                    .filename("reporte-" + reporteId + ".pdf")
                    .build();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .body(pdf);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @PostMapping("/reportes/{reporteId}/eliminar")
    public String eliminarReporte(@PathVariable("reporteId") Long reporteId,
                                  RedirectAttributes redirectAttributes) {
        boolean eliminado = reporteService.eliminarReportePorId(reporteId);

        if (!eliminado) {
            redirectAttributes.addFlashAttribute("mensajeReporte", "El reporte ya no existe o no pudo eliminarse.");
            return "redirect:/historial";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Reporte eliminado correctamente.");
        return "redirect:/historial";
    }

    @PostMapping("/historial")
    public String generarReporte(@RequestParam("estacionId") Long estacionId,
                                 @RequestParam("fechaSeleccion") String fechaSeleccion,
                                 RedirectAttributes redirectAttributes) {

        Estacion estacion = estacionService.obtenerEstacionActivaPorId(estacionId).orElse(null);
        if (estacion == null) {
            redirectAttributes.addFlashAttribute("mensajeReporte", "Seleccione una estación activa válida.");
            return "redirect:/historial";
        }

        RangoFechaSeleccionado rango = parsearFechaSeleccion(fechaSeleccion);
        if (rango == null) {
            redirectAttributes.addFlashAttribute("mensajeReporte", "Seleccione una fecha válida en el calendario.");
            return "redirect:/historial";
        }

        List<VwMedicionDetalle> mediciones = repo.findReportePorRangoByEstacionCodigo(estacion.getCodigo(), rango.fechaInicio(), rango.fechaFin());
        if (mediciones.isEmpty()) {
            redirectAttributes.addFlashAttribute("mensajeReporte", "No existen mediciones para los filtros seleccionados.");
            return "redirect:/historial";
        }

        String tipoNormalizado = inferirTipoReporte(rango.fechaInicio(), rango.fechaFin());
        Reporte reporte = reporteService.guardarReporteGenerado(estacion.getId(), rango.fechaInicio(), rango.fechaFin(), tipoNormalizado);
        redirectAttributes.addFlashAttribute("mensajeExito", "Reporte generado correctamente.");
        return "redirect:/reportes/" + reporte.getIdReporte();
    }

    private String inferirTipoReporte(LocalDate fechaInicio, LocalDate fechaFin) {
        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;

        if (dias <= 1) return "DIARIO";
        if (dias <= 7) return "SEMANAL";
        return "MENSUAL";
    }

    private RangoFechaSeleccionado parsearFechaSeleccion(String fechaSeleccion) {
        if (fechaSeleccion == null || fechaSeleccion.isBlank()) {
            return null;
        }

        String[] partes = fechaSeleccion.trim().split("\\s+to\\s+");
        try {
            if (partes.length == 1) {
                LocalDate dia = LocalDate.parse(partes[0].trim());
                return new RangoFechaSeleccionado(dia, dia);
            }

            if (partes.length == 2) {
                LocalDate inicio = LocalDate.parse(partes[0].trim());
                LocalDate fin = LocalDate.parse(partes[1].trim());
                if (inicio.isAfter(fin)) {
                    return null;
                }
                return new RangoFechaSeleccionado(inicio, fin);
            }
        } catch (Exception ignored) {
            return null;
        }

        return null;
    }

    private void cargarPantallaReportesBase(Model model) {
        List<Estacion> estacionesActivas = estacionService.obtenerEstacionesActivas();
        List<EstacionOpcionDto> estacionesReporte = estacionesActivas.stream()
                .map(estacion -> new EstacionOpcionDto(
                        estacion.getId(),
                        estacion.getCodigo(),
                        (estacion.getDescripcion() == null || estacion.getDescripcion().isBlank())
                                ? estacion.getCodigo()
                                : estacion.getDescripcion()
                ))
                .toList();

        List<ReporteRepository.ReporteRecienteView> recientes = reporteService.listarReportesRecientes();

        model.addAttribute("estacionesReporte", estacionesReporte);
        model.addAttribute("reportesRecientes", recientes);
    }

    private void cargarVistaPreviaReporte(Model model, Long reporteId, int page, int limit) {
        ReporteRepository.ReporteRecienteView reporte = reporteService.obtenerDetalleReporte(reporteId).orElse(null);
        if (reporte == null) {
            model.addAttribute("mensajeReporte", "El reporte seleccionado no existe.");
            return;
        }

        if (reporte.getEstacionCodigo() == null) {
            model.addAttribute("mensajeReporte", "No se encontró la estación del reporte seleccionado.");
            return;
        }

        int limiteNormalizado = Math.max(1, Math.min(100, limit));
        int paginaNormalizada = Math.max(0, page);

        List<VwMedicionDetalle> detalleCompleto = repo.findReportePorRangoByEstacionCodigo(
                reporte.getEstacionCodigo(),
                reporte.getFechaInicio(),
                reporte.getFechaFin()
        );

        Map<String, List<VwMedicionDetalle>> agrupadoPorClave = detalleCompleto.stream()
                .collect(Collectors.groupingBy(
                        medicion -> normalizarTipoSensor(medicion.getTipoSensor()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        int inicioPorSensor = paginaNormalizada * limiteNormalizado;

        List<DetalleSensorDto> detalleAgrupadoPorSensor = agrupadoPorClave.entrySet().stream()
                .sorted((a, b) -> {
                    int idxA = TIPOS_SENSOR_DASHBOARD.indexOf(a.getValue().get(0).getTipoSensor());
                    int idxB = TIPOS_SENSOR_DASHBOARD.indexOf(b.getValue().get(0).getTipoSensor());

                    if (idxA == -1 && idxB == -1) {
                        return a.getKey().compareToIgnoreCase(b.getKey());
                    }
                    if (idxA == -1) return 1;
                    if (idxB == -1) return -1;
                    return Integer.compare(idxA, idxB);
                })
                .map(entry -> {
                    String tipoMostrar = entry.getValue().get(0).getTipoSensor();
                    if (tipoMostrar == null || tipoMostrar.isBlank()) {
                        tipoMostrar = "Sin tipo de sensor";
                    }
                    List<VwMedicionDetalle> medicionesSensor = entry.getValue();
                    if (inicioPorSensor >= medicionesSensor.size()) {
                        return new DetalleSensorDto(tipoMostrar, List.of(), "--", "--", "--", "--", "", "", "");
                    }

                    int finPorSensor = Math.min(inicioPorSensor + limiteNormalizado, medicionesSensor.size());
                    List<VwMedicionDetalle> medicionesPaginaSensor = medicionesSensor.subList(inicioPorSensor, finPorSensor);
                    ResumenSensorDto resumen = calcularResumenSensor(medicionesPaginaSensor);
                    SensorChartDto chart = construirSerieGrafica(medicionesSensor);
                    return new DetalleSensorDto(
                            tipoMostrar,
                            medicionesPaginaSensor,
                            resumen.promedio(),
                            resumen.minimo(),
                            resumen.maximo(),
                            resumen.ultimaMedicion(),
                            chart.labelsCsv(),
                            chart.valuesCsv(),
                            chart.unidad()
                    );
                })
                .filter(detalle -> !detalle.mediciones().isEmpty())
                .toList();

        int totalPaginas = agrupadoPorClave.values().stream()
                .mapToInt(medicionesSensor -> (int) Math.ceil((double) medicionesSensor.size() / limiteNormalizado))
                .max()
                .orElse(0);

        int totalElementosPaginacion = totalPaginas <= 0 ? 0 : totalPaginas * limiteNormalizado;
        Page<VwMedicionDetalle> detallePage = new PageImpl<>(
                List.of(),
                PageRequest.of(paginaNormalizada, limiteNormalizado),
                totalElementosPaginacion
        );

        model.addAttribute("reporteSeleccionado", reporte);
        model.addAttribute("detalleReportePage", detallePage);
        model.addAttribute("detalleAgrupadoPorSensor", detalleAgrupadoPorSensor);
        model.addAttribute("detalleLimit", limiteNormalizado);
    }


    private String normalizarTipoSensor(String tipoSensor) {
        if (tipoSensor == null || tipoSensor.isBlank()) {
            return "sin_tipo_de_sensor";
        }
        return tipoSensor.trim().toLowerCase();
    }

    private SensorChartDto construirSerieGrafica(List<VwMedicionDetalle> medicionesSensor) {
        List<VwMedicionDetalle> ordenadas = medicionesSensor.stream()
                .sorted((a, b) -> {
                    if (a.getFechaMedicion() == null && b.getFechaMedicion() == null) return 0;
                    if (a.getFechaMedicion() == null) return -1;
                    if (b.getFechaMedicion() == null) return 1;
                    return a.getFechaMedicion().compareTo(b.getFechaMedicion());
                })
                .toList();

        String labelsCsv = ordenadas.stream()
                .map(m -> m.getFechaMedicion() != null ? m.getFechaMedicion().toString() : "")
                .collect(Collectors.joining("||"));

        String valuesCsv = ordenadas.stream()
                .map(m -> m.getValor() != null ? m.getValor().stripTrailingZeros().toPlainString() : "")
                .collect(Collectors.joining("||"));

        String unidad = ordenadas.stream()
                .map(VwMedicionDetalle::getUnidadMedida)
                .filter(u -> u != null && !u.isBlank())
                .findFirst()
                .orElse("");

        return new SensorChartDto(labelsCsv, valuesCsv, unidad);
    }

    private ResumenSensorDto calcularResumenSensor(List<VwMedicionDetalle> medicionesPaginaSensor) {
        List<VwMedicionDetalle> medicionesConValor = medicionesPaginaSensor.stream()
                .filter(m -> m.getValor() != null)
                .toList();

        if (medicionesConValor.isEmpty()) {
            return new ResumenSensorDto("--", "--", "--", "--");
        }

        BigDecimal suma = medicionesConValor.stream()
                .map(VwMedicionDetalle::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal promedio = suma.divide(
                BigDecimal.valueOf(medicionesConValor.size()),
                2,
                RoundingMode.HALF_UP
        );

        BigDecimal minimo = medicionesConValor.stream()
                .map(VwMedicionDetalle::getValor)
                .min(BigDecimal::compareTo)
                .orElse(null);

        BigDecimal maximo = medicionesConValor.stream()
                .map(VwMedicionDetalle::getValor)
                .max(BigDecimal::compareTo)
                .orElse(null);

        VwMedicionDetalle ultima = medicionesConValor.stream()
                .filter(m -> m.getFechaMedicion() != null)
                .max((a, b) -> a.getFechaMedicion().compareTo(b.getFechaMedicion()))
                .orElse(medicionesConValor.get(medicionesConValor.size() - 1));

        String unidad = medicionesPaginaSensor.stream()
                .map(VwMedicionDetalle::getUnidadMedida)
                .filter(u -> u != null && !u.isBlank())
                .findFirst()
                .orElse("");

        return new ResumenSensorDto(
                formatearValorResumen(promedio, unidad),
                formatearValorResumen(minimo, unidad),
                formatearValorResumen(maximo, unidad),
                formatearUltimaMedicion(ultima, unidad)
        );
    }

    private String formatearValorResumen(BigDecimal valor, String unidad) {
        if (valor == null) {
            return "--";
        }

        String valorTxt = valor.stripTrailingZeros().toPlainString();
        if (unidad == null || unidad.isBlank()) {
            return valorTxt;
        }

        return valorTxt + " " + unidad;
    }

    private String formatearUltimaMedicion(VwMedicionDetalle medicion, String unidadPorDefecto) {
        if (medicion == null || medicion.getValor() == null) {
            return "--";
        }

        String unidad = (medicion.getUnidadMedida() != null && !medicion.getUnidadMedida().isBlank())
                ? medicion.getUnidadMedida()
                : unidadPorDefecto;

        String valorTxt = formatearValorResumen(medicion.getValor(), unidad);
        Timestamp fecha = medicion.getFechaMedicion();
        if (fecha == null) {
            return valorTxt;
        }

        return valorTxt + " · " + fecha;
    }

    private void cargarDashboard(int limit, int page, Model model) {
        if (limit < 1) limit = 1;
        if (limit > 100) limit = 100;
        if (page < 0) page = 0;

        List<String> codigosEstacionesActivas = estacionService.obtenerEstacionesActivas().stream()
                .map(estacion -> estacion.getCodigo())
                .toList();

        List<String> codigosActivos = uiEstacionService.obtenerCodigosActivos().stream()
                .filter(codigosEstacionesActivas::contains)
                .toList();
        uiEstacionSensorService.asegurarSensoresRegistrados(codigosEstacionesActivas);

        BigDecimal tempAvg = codigosActivos.isEmpty()
                ? null
                : repo.avgUltimas40TempAmbientalSoloActivas(codigosActivos);

        BigDecimal humAvg = codigosActivos.isEmpty()
                ? null
                : repo.avgUltimas40HumedadAmbientalSoloActivas(codigosActivos);

        String tempPromedioTxt = (tempAvg == null)
                ? "--"
                : tempAvg.setScale(1, RoundingMode.HALF_UP).toPlainString() + "°C";

        String humPromedioTxt = (humAvg == null)
                ? "--"
                : humAvg.setScale(1, RoundingMode.HALF_UP).toPlainString() + "%";

        long sensoresActivos = sensorRepository.contarSensoresActivosDeEstacionesActivas();
        long sensoresRegistrados = sensorRepository.contarSensoresRegistradosDeEstacionesActivas();
        long totalAlertasConfiguradas = alertaRepository.countByActivaTrue();

        model.addAttribute("sensoresActivos", sensoresActivos);
        model.addAttribute("sensoresRegistrados", sensoresRegistrados);
        model.addAttribute("tempPromedioTxt", tempPromedioTxt);
        model.addAttribute("humPromedioTxt", humPromedioTxt);
        model.addAttribute("totalAlertasConfiguradas", totalAlertasConfiguradas);
        model.addAttribute("limit", limit);
        model.addAttribute("page", Page.empty(PageRequest.of(page, limit)));
        model.addAttribute("mediciones", List.of());
        model.addAttribute("estacionesDashboard", construirCardsPorEstacion());
    }

    private List<EstacionDashboardDto> construirCardsPorEstacion() {
        List<Estacion> estacionesActivas = estacionService.obtenerEstacionesActivas();
        List<String> codigosEstacionesActivas = estacionesActivas.stream()
                .map(Estacion::getCodigo)
                .toList();

        List<VwMedicionDetalle> ultimas = repo.findUltimasMedicionesPorEstacionYTipoSensor();
        Map<String, Boolean> estadosUi = uiEstacionService.obtenerEstadosPorCodigo();
        Map<String, Map<String, Boolean>> estadosSensoresPorEstacion =
                uiEstacionSensorService.obtenerEstadosPorEstaciones(codigosEstacionesActivas);

        Map<String, Map<String, VwMedicionDetalle>> ultimasPorEstacion = new LinkedHashMap<>();
        for (VwMedicionDetalle medicion : ultimas) {
            ultimasPorEstacion
                    .computeIfAbsent(medicion.getEstacionCodigo(), key -> new LinkedHashMap<>())
                    .put(medicion.getTipoSensor(), medicion);
        }

        List<EstacionDashboardDto> cards = new ArrayList<>();

        for (Estacion estacion : estacionesActivas) {
            String codigoEstacion = estacion.getCodigo();
            Map<String, VwMedicionDetalle> porTipo =
                    ultimasPorEstacion.getOrDefault(codigoEstacion, Map.of());
            Map<String, Boolean> estadosSensores =
                    estadosSensoresPorEstacion.getOrDefault(codigoEstacion, Map.of());

            List<SensorValorDto> sensores = new ArrayList<>();
            for (String tipoSensor : TIPOS_SENSOR_DASHBOARD) {
                VwMedicionDetalle medicion = porTipo.get(tipoSensor);
                boolean activo = estadosSensores.getOrDefault(tipoSensor, true);
                sensores.add(new SensorValorDto(tipoSensor, formatearValor(medicion), activo));
            }

            String descripcion = (estacion.getDescripcion() == null || estacion.getDescripcion().isBlank())
                    ? codigoEstacion
                    : estacion.getDescripcion();

            cards.add(new EstacionDashboardDto(
                    codigoEstacion,
                    descripcion,
                    estadosUi.getOrDefault(codigoEstacion, true),
                    sensores
            ));
        }

        return cards;
    }

    private String formatearValor(VwMedicionDetalle medicion) {
        if (medicion == null || medicion.getValor() == null) {
            return "--";
        }

        String valor = medicion.getValor()
                .stripTrailingZeros()
                .toPlainString();

        if (medicion.getUnidadMedida() == null || medicion.getUnidadMedida().isBlank()) {
            return valor;
        }

        return valor + " " + medicion.getUnidadMedida();
    }

    public record RangoFechaSeleccionado(LocalDate fechaInicio, LocalDate fechaFin) {}

    public record SensorValorDto(String tipoSensor, String valor, boolean activo) {}

    public record EstacionOpcionDto(Long id, String codigo, String descripcion) {}

    public record DetalleSensorDto(String tipoSensor, List<VwMedicionDetalle> mediciones, String promedio, String minimo, String maximo, String ultimaMedicion, String chartLabelsCsv, String chartValuesCsv, String chartUnidad) {}

    public record ResumenSensorDto(String promedio, String minimo, String maximo, String ultimaMedicion) {}

    public record SensorChartDto(String labelsCsv, String valuesCsv, String unidad) {}

    public record EstacionDashboardDto(
            String estacionCodigo,
            String estacionDescripcion,
            Boolean activa,
            List<SensorValorDto> sensores
    ) {}
}