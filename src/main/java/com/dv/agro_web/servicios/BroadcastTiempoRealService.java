package com.dv.agro_web.servicios;

import com.dv.agro_web.websocket.TiempoRealWebSocketHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class BroadcastTiempoRealService {

    private final DashboardTiempoRealService dashboardTiempoRealService;
    private final TiempoRealWebSocketHandler tiempoRealWebSocketHandler;
    private final ObjectMapper objectMapper;

    private String ultimoSnapshotSerializado;
    private Map<String, Boolean> ultimosEstados = Map.of();

    public BroadcastTiempoRealService(DashboardTiempoRealService dashboardTiempoRealService,
                                      TiempoRealWebSocketHandler tiempoRealWebSocketHandler,
                                      ObjectMapper objectMapper) {
        this.dashboardTiempoRealService = dashboardTiempoRealService;
        this.tiempoRealWebSocketHandler = tiempoRealWebSocketHandler;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.tiempo-real.intervalo-ms:5000}")
    public void publicarCambios() {
        DashboardTiempoRealService.DashboardSnapshotDto snapshot = dashboardTiempoRealService.obtenerSnapshot();
        String snapshotSerializado = serializar(snapshot);
        Map<String, Boolean> estadosActuales = extraerEstados(snapshot);

        if (ultimoSnapshotSerializado == null) {
            ultimoSnapshotSerializado = snapshotSerializado;
            ultimosEstados = estadosActuales;
            return;
        }

        boolean snapshotCambio = !Objects.equals(ultimoSnapshotSerializado, snapshotSerializado);
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

        ultimoSnapshotSerializado = snapshotSerializado;
        ultimosEstados = estadosActuales;
    }

    private Map<String, Boolean> extraerEstados(DashboardTiempoRealService.DashboardSnapshotDto snapshot) {
        Map<String, Boolean> estados = new LinkedHashMap<>();
        snapshot.estaciones().forEach(estacion -> estados.put(estacion.estacionCodigo(), Boolean.TRUE.equals(estacion.activa())));
        return estados;
    }

    private String serializar(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar el snapshot de dashboard", ex);
        }
    }

    public record CambioEstadoDto(String estacionCodigo, boolean activa, String mensaje) {}
}
