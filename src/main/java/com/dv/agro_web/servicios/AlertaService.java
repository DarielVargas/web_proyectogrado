package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Alerta;
import com.dv.agro_web.repositorios.AlertaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;

    public AlertaService(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
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
}
