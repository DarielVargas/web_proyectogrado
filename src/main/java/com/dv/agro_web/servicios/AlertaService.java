package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Alerta;
import com.dv.agro_web.entidades.AlertaHistorial;
import com.dv.agro_web.entidades.VwMedicionDetalle;
import com.dv.agro_web.repositorios.AlertaHistorialRepository;
import com.dv.agro_web.repositorios.AlertaRepository;
import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertaService {

    private static final String SESSION_ALERTAS_PENDIENTES = "alertasPendientes";

    private final AlertaRepository alertaRepository;
    private final AlertaHistorialRepository alertaHistorialRepository;
    private final VwMedicionDetalleRepository medicionDetalleRepository;

    public AlertaService(AlertaRepository alertaRepository,
                         AlertaHistorialRepository alertaHistorialRepository,
                         VwMedicionDetalleRepository medicionDetalleRepository) {
        this.alertaRepository = alertaRepository;
        this.alertaHistorialRepository = alertaHistorialRepository;
        this.medicionDetalleRepository = medicionDetalleRepository;
    }

    public List<Alerta> listarAlertasConfiguradas() {
        return alertaRepository.findAllByOrderByFechaCreacionDescIdAlertaDesc();
    }

    public Alerta guardarNuevaAlerta(String estacionCodigo, String sensorTipo, String operador, BigDecimal umbral) {
        Alerta alerta = new Alerta();
        alerta.setEstacionCodigo(estacionCodigo);
        alerta.setSensorTipo(sensorTipo);
        alerta.setOperador(operador);
        alerta.setUmbral(umbral);
        alerta.setActiva(true);
        return alertaRepository.save(alerta);
    }

    public void eliminarAlertaPorId(Long idAlerta) {
        if (alertaRepository.existsById(idAlerta)) {
            alertaRepository.deleteById(idAlerta);
        }
    }

    public void eliminarTodasLasAlertas() {
        alertaRepository.deleteAll();
    }



    public List<AlertaHistorial> listarHistorialAlertas() {
        return alertaHistorialRepository.findAllByOrderByFechaActivacionDescIdHistorialDesc();
    }

    public void eliminarHistorialPorId(Long idHistorial) {
        if (alertaHistorialRepository.existsById(idHistorial)) {
            alertaHistorialRepository.deleteById(idHistorial);
        }
    }

    public void eliminarTodoElHistorial() {
        alertaHistorialRepository.deleteAll();
    }
    public List<NotificacionAlertaDto> obtenerAlertasDisparadas(HttpSession session) {
        Map<Long, Long> pendientes = obtenerPendientes(session);
        List<Alerta> alertas = alertaRepository.findAllByOrderByFechaCreacionAscIdAlertaAsc();
        List<NotificacionAlertaDto> notificaciones = new ArrayList<>();

        for (Alerta alerta : alertas) {
            if (alerta.getIdAlerta() == null) {
                continue;
            }

            LocalDateTime fechaCreacion = alerta.getFechaCreacion() != null ? alerta.getFechaCreacion() : LocalDateTime.now();
            VwMedicionDetalle ultimaMedicion = medicionDetalleRepository.findUltimaMedicionPorEstacionYSensorDesde(
                    alerta.getEstacionCodigo(),
                    alerta.getSensorTipo(),
                    Timestamp.valueOf(fechaCreacion)
            );

            if (ultimaMedicion == null || ultimaMedicion.getValor() == null || ultimaMedicion.getMedicionId() == null) {
                continue;
            }

            boolean condicionCumplida = evaluarCondicion(ultimaMedicion.getValor(), alerta.getOperador(), alerta.getUmbral());
            boolean alertaArmada = Boolean.TRUE.equals(alerta.getActiva());
            Long medicionPendiente = pendientes.get(alerta.getIdAlerta());

            if (!alertaArmada && !condicionCumplida) {
                alerta.setActiva(true);
                alertaRepository.save(alerta);
                pendientes.remove(alerta.getIdAlerta());
                continue;
            }

            if (!alertaArmada || !condicionCumplida) {
                continue;
            }

            if (medicionPendiente != null && medicionPendiente.equals(ultimaMedicion.getMedicionId())) {
                continue;
            }

            pendientes.put(alerta.getIdAlerta(), ultimaMedicion.getMedicionId());
            notificaciones.add(new NotificacionAlertaDto(
                    alerta.getIdAlerta(),
                    ultimaMedicion.getMedicionId(),
                    construirMensaje(alerta)
            ));
        }

        session.setAttribute(SESSION_ALERTAS_PENDIENTES, pendientes);
        return notificaciones;
    }

    public void marcarAlertaAtendida(Long alertaId, Long medicionId, HttpSession session) {
        if (alertaId == null || medicionId == null) {
            return;
        }

        Map<Long, Long> pendientes = obtenerPendientes(session);
        Long medicionPendiente = pendientes.get(alertaId);

        if (medicionPendiente == null || !medicionPendiente.equals(medicionId)) {
            return;
        }

        alertaRepository.findById(alertaId).ifPresent(alerta -> {
            alerta.setActiva(false);
            alertaRepository.save(alerta);

            AlertaHistorial historial = new AlertaHistorial();
            historial.setIdAlerta(alerta.getIdAlerta());
            historial.setEstacionCodigo(alerta.getEstacionCodigo());
            historial.setSensorTipo(alerta.getSensorTipo());
            historial.setOperador(alerta.getOperador());
            historial.setUmbral(alerta.getUmbral());
            historial.setFechaActivacion(LocalDateTime.now());
            alertaHistorialRepository.save(historial);
        });

        pendientes.remove(alertaId);
        session.setAttribute(SESSION_ALERTAS_PENDIENTES, pendientes);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Long> obtenerPendientes(HttpSession session) {
        Object valor = session.getAttribute(SESSION_ALERTAS_PENDIENTES);
        if (valor instanceof Map<?, ?> map) {
            return (Map<Long, Long>) map;
        }
        Map<Long, Long> nuevo = new HashMap<>();
        session.setAttribute(SESSION_ALERTAS_PENDIENTES, nuevo);
        return nuevo;
    }

    private boolean evaluarCondicion(BigDecimal valor, String operador, BigDecimal umbral) {
        if (valor == null || umbral == null || operador == null) {
            return false;
        }

        return switch (operador) {
            case ">" -> valor.compareTo(umbral) > 0;
            case "<" -> valor.compareTo(umbral) < 0;
            case "=" -> valor.compareTo(umbral) == 0;
            default -> false;
        };
    }

    private String construirMensaje(Alerta alerta) {
        return "Alerta: " + alerta.getSensorTipo() + " en " + alerta.getEstacionCodigo() + " "
                + descripcionOperador(alerta.getOperador()) + " " + alerta.getUmbral().stripTrailingZeros().toPlainString();
    }

    private String descripcionOperador(String operador) {
        return switch (operador) {
            case ">" -> "está por encima de";
            case "<" -> "está por debajo de";
            default -> "es igual a";
        };
    }

    public record NotificacionAlertaDto(Long alertaId, Long medicionId, String mensaje) {
    }
}
