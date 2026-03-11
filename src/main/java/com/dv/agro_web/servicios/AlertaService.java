package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Alerta;
import com.dv.agro_web.entidades.VwMedicionDetalle;
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

    private static final String SESSION_ALERTAS_CURSOR = "alertasCursores";

    private final AlertaRepository alertaRepository;
    private final VwMedicionDetalleRepository medicionDetalleRepository;

    public AlertaService(AlertaRepository alertaRepository,
                         VwMedicionDetalleRepository medicionDetalleRepository) {
        this.alertaRepository = alertaRepository;
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

    public List<NotificacionAlertaDto> obtenerAlertasDisparadas(HttpSession session) {
        Map<Long, Long> cursores = obtenerCursores(session);
        List<Alerta> alertasActivas = alertaRepository.findByActivaTrueOrderByFechaCreacionAscIdAlertaAsc();
        List<NotificacionAlertaDto> notificaciones = new ArrayList<>();

        Map<Long, Long> cursoresLimpios = new HashMap<>();

        for (Alerta alerta : alertasActivas) {
            Long ultimoIdNotificado = cursores.getOrDefault(alerta.getIdAlerta(), 0L);
            LocalDateTime fechaCreacion = alerta.getFechaCreacion() != null ? alerta.getFechaCreacion() : LocalDateTime.now();

            VwMedicionDetalle medicion = medicionDetalleRepository.findUltimaMedicionQueCumpleAlerta(
                    alerta.getEstacionCodigo(),
                    alerta.getSensorTipo(),
                    alerta.getOperador(),
                    alerta.getUmbral(),
                    Timestamp.valueOf(fechaCreacion),
                    ultimoIdNotificado
            );

            if (medicion != null && medicion.getMedicionId() != null) {
                ultimoIdNotificado = medicion.getMedicionId();
                notificaciones.add(new NotificacionAlertaDto(
                        alerta.getIdAlerta(),
                        medicion.getMedicionId(),
                        construirMensaje(alerta)
                ));
            }

            cursoresLimpios.put(alerta.getIdAlerta(), ultimoIdNotificado);
        }

        session.setAttribute(SESSION_ALERTAS_CURSOR, cursoresLimpios);
        return notificaciones;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Long> obtenerCursores(HttpSession session) {
        Object valor = session.getAttribute(SESSION_ALERTAS_CURSOR);
        if (valor instanceof Map<?, ?> map) {
            return (Map<Long, Long>) map;
        }
        Map<Long, Long> nuevo = new HashMap<>();
        session.setAttribute(SESSION_ALERTAS_CURSOR, nuevo);
        return nuevo;
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
