package com.dv.agro_web.servicios;

import com.dv.agro_web.websocket.TiempoRealWebSocketHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

@Service
public class BroadcastTiempoRealService {

    private final DashboardTiempoRealService dashboardTiempoRealService;
    private final TiempoRealWebSocketHandler tiempoRealWebSocketHandler;

    private String ultimoSnapshotFingerprint;
    private Map<String, Boolean> ultimosEstados = Map.of();

    public BroadcastTiempoRealService(DashboardTiempoRealService dashboardTiempoRealService,
                                      TiempoRealWebSocketHandler tiempoRealWebSocketHandler) {
        this.dashboardTiempoRealService = dashboardTiempoRealService;
        this.tiempoRealWebSocketHandler = tiempoRealWebSocketHandler;
    }

    @Scheduled(fixedDelayString = "${app.tiempo-real.intervalo-ms:5000}")
    public void publicarCambios() {
        DashboardTiempoRealService.DashboardSnapshotDto snapshot = dashboardTiempoRealService.obtenerSnapshot();
        String snapshotFingerprint = construirFingerprint(snapshot);
        Map<String, Boolean> estadosActuales = extraerEstados(snapshot);

        if (ultimoSnapshotFingerprint == null) {
            ultimoSnapshotFingerprint = snapshotFingerprint;
            ultimosEstados = estadosActuales;
            return;
        }

        boolean snapshotCambio = !Objects.equals(ultimoSnapshotFingerprint, snapshotFingerprint);
        if (snapshotCambio) {
            tiempoRealWebSocketHandler.broadcast(new TiempoRealWebSocketHandler.EventoTiempoRealDto(
                    "dashboard-update",
                    snapshot
            ));
        }

        for (Map.Entry<String, Boolean> entry : estadosActuales.entrySet()) {
            String codigo = entry.getKey();
            Boolean estadoAnterior = ultimosEstados.get(codigo);
            Boolean estadoActual = entry.getValue();

            if (estadoAnterior == null || Objects.equals(estadoAnterior, estadoActual)) {
                continue;
            }

            tiempoRealWebSocketHandler.broadcast(new TiempoRealWebSocketHandler.EventoTiempoRealDto(
                    "station-status-changed",
                    new CambioEstadoDto(
                            codigo,
                            estadoActual,
                            estadoActual
                                    ? "La estación " + codigo + " pasó a estado operativa"
                                    : "La estación " + codigo + " pasó a estado inactiva"
                    )
            ));
        }

        if (snapshotCambio) {
            tiempoRealWebSocketHandler.broadcast(new TiempoRealWebSocketHandler.EventoTiempoRealDto(
                    "alerts-refresh",
                    null
            ));
        }

        ultimoSnapshotFingerprint = snapshotFingerprint;
        ultimosEstados = estadosActuales;
    }

    private Map<String, Boolean> extraerEstados(DashboardTiempoRealService.DashboardSnapshotDto snapshot) {
        Map<String, Boolean> estados = new LinkedHashMap<>();
        snapshot.estaciones().forEach(estacion -> estados.put(estacion.estacionCodigo(), Boolean.TRUE.equals(estacion.activa())));
        return estados;
    }

    private String construirFingerprint(DashboardTiempoRealService.DashboardSnapshotDto snapshot) {
        StringJoiner joiner = new StringJoiner("|");
        joiner.add(String.valueOf(snapshot.sensoresActivos()));
        joiner.add(String.valueOf(snapshot.sensoresRegistrados()));
        joiner.add(snapshot.tempPromedioTxt());
        joiner.add(snapshot.humPromedioTxt());
        joiner.add(String.valueOf(snapshot.totalAlertasConfiguradas()));

        snapshot.estaciones().forEach(estacion -> {
            joiner.add(estacion.estacionCodigo());
            joiner.add(estacion.estacionDescripcion());
            joiner.add(String.valueOf(Boolean.TRUE.equals(estacion.activa())));
            estacion.sensores().forEach(sensor -> {
                joiner.add(sensor.tipoSensor());
                joiner.add(sensor.valor());
                joiner.add(String.valueOf(sensor.activo()));
            });
        });

        return joiner.toString();
    }

    public record CambioEstadoDto(String estacionCodigo, boolean activa, String mensaje) {}
}