package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.entidades.VwMedicionDetalle;
import com.dv.agro_web.repositorios.AlertaRepository;
import com.dv.agro_web.repositorios.SensorRepository;
import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardTiempoRealService {

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

    private final VwMedicionDetalleRepository medicionDetalleRepository;
    private final SensorRepository sensorRepository;
    private final AlertaRepository alertaRepository;
    private final EstacionService estacionService;
    private final UiEstacionService uiEstacionService;
    private final UiEstacionSensorService uiEstacionSensorService;

    public DashboardTiempoRealService(VwMedicionDetalleRepository medicionDetalleRepository,
                                      SensorRepository sensorRepository,
                                      AlertaRepository alertaRepository,
                                      EstacionService estacionService,
                                      UiEstacionService uiEstacionService,
                                      UiEstacionSensorService uiEstacionSensorService) {
        this.medicionDetalleRepository = medicionDetalleRepository;
        this.sensorRepository = sensorRepository;
        this.alertaRepository = alertaRepository;
        this.estacionService = estacionService;
        this.uiEstacionService = uiEstacionService;
        this.uiEstacionSensorService = uiEstacionSensorService;
    }

    public DashboardSnapshotDto obtenerSnapshot() {
        List<String> codigosEstacionesActivas = estacionService.obtenerEstacionesActivas().stream()
                .map(Estacion::getCodigo)
                .toList();

        uiEstacionSensorService.asegurarSensoresRegistrados(codigosEstacionesActivas);

        List<String> codigosActivos = uiEstacionService.obtenerCodigosActivos().stream()
                .filter(codigosEstacionesActivas::contains)
                .toList();

        BigDecimal tempAvg = codigosActivos.isEmpty()
                ? null
                : medicionDetalleRepository.avgUltimas40TempAmbientalSoloActivas(codigosActivos);

        BigDecimal humAvg = codigosActivos.isEmpty()
                ? null
                : medicionDetalleRepository.avgUltimas40HumedadAmbientalSoloActivas(codigosActivos);

        long sensoresActivos = codigosActivos.isEmpty()
                ? 0
                : sensorRepository.contarSensoresActivosDeEstacionesActivas(codigosActivos);

        long sensoresRegistrados = sensorRepository.contarSensoresRegistradosDeEstacionesActivas();
        long totalAlertasConfiguradas = alertaRepository.countByActivaTrue();

        return new DashboardSnapshotDto(
                sensoresActivos,
                sensoresRegistrados,
                formatearPromedio(tempAvg, "°C"),
                formatearPromedio(humAvg, "%"),
                totalAlertasConfiguradas,
                construirCardsPorEstacion(codigosEstacionesActivas)
        );
    }

    private List<EstacionDashboardDto> construirCardsPorEstacion(List<String> codigosEstacionesActivas) {
        List<Estacion> estacionesActivas = estacionService.obtenerEstacionesActivas();
        List<VwMedicionDetalle> ultimas = medicionDetalleRepository.findUltimasMedicionesPorEstacionYTipoSensor();
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
            Map<String, VwMedicionDetalle> porTipo = ultimasPorEstacion.getOrDefault(codigoEstacion, Map.of());
            Map<String, Boolean> estadosSensores = estadosSensoresPorEstacion.getOrDefault(codigoEstacion, Map.of());

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

    private String formatearPromedio(BigDecimal valor, String unidad) {
        if (valor == null) {
            return "--";
        }
        return valor.setScale(1, RoundingMode.HALF_UP).toPlainString() + unidad;
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

    public record DashboardSnapshotDto(
            long sensoresActivos,
            long sensoresRegistrados,
            String tempPromedioTxt,
            String humPromedioTxt,
            long totalAlertasConfiguradas,
            List<EstacionDashboardDto> estaciones
    ) {}

    public record EstacionDashboardDto(
            String estacionCodigo,
            String estacionDescripcion,
            Boolean activa,
            List<SensorValorDto> sensores
    ) {}

    public record SensorValorDto(String tipoSensor, String valor, boolean activo) {}
}
