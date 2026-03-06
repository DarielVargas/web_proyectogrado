package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.UiEstacionSensor;
import com.dv.agro_web.entidades.UiEstacionSensorId;
import com.dv.agro_web.repositorios.UiEstacionSensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UiEstacionSensorService {

    private final UiEstacionSensorRepository uiEstacionSensorRepository;

    public UiEstacionSensorService(UiEstacionSensorRepository uiEstacionSensorRepository) {
        this.uiEstacionSensorRepository = uiEstacionSensorRepository;
    }

    public Map<String, Boolean> obtenerEstadosPorEstacion(String estacionCodigo) {
        Map<String, Boolean> estados = new HashMap<>();
        for (UiEstacionSensor item : uiEstacionSensorRepository.findByEstacionCodigo(estacionCodigo)) {
            estados.put(item.getTipoSensor(), Boolean.TRUE.equals(item.getActivo()));
        }
        return estados;
    }

    public Map<String, Map<String, Boolean>> obtenerEstadosPorEstaciones(Collection<String> codigosEstacion) {
        Map<String, Map<String, Boolean>> estados = new HashMap<>();
        for (UiEstacionSensor item : uiEstacionSensorRepository.findByEstacionCodigoIn(codigosEstacion)) {
            estados.computeIfAbsent(item.getEstacionCodigo(), key -> new HashMap<>())
                    .put(item.getTipoSensor(), Boolean.TRUE.equals(item.getActivo()));
        }
        return estados;
    }

    @Transactional
    public void actualizarEstado(String estacionCodigo, String tipoSensor, boolean activo) {
        UiEstacionSensor estado = uiEstacionSensorRepository
                .findById(new UiEstacionSensorId(estacionCodigo, tipoSensor))
                .orElseGet(() -> {
                    UiEstacionSensor nuevo = new UiEstacionSensor();
                    nuevo.setEstacionCodigo(estacionCodigo);
                    nuevo.setTipoSensor(tipoSensor);
                    return nuevo;
                });

        estado.setActivo(activo);
        estado.setUpdatedAt(LocalDateTime.now());
        uiEstacionSensorRepository.save(estado);
    }
}
