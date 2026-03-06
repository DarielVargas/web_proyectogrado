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

    private static final List<String> TIPOS_SENSOR = List.of(
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
    public void asegurarSensoresRegistrados(Collection<String> codigosEstacion) {
        LocalDateTime ahora = LocalDateTime.now();
        for (String codigo : codigosEstacion) {
            for (String tipoSensor : TIPOS_SENSOR) {
                UiEstacionSensorId id = new UiEstacionSensorId(codigo, tipoSensor);
                if (uiEstacionSensorRepository.existsById(id)) {
                    continue;
                }

                UiEstacionSensor nuevo = new UiEstacionSensor();
                nuevo.setEstacionCodigo(codigo);
                nuevo.setTipoSensor(tipoSensor);
                nuevo.setActivo(true);
                nuevo.setUpdatedAt(ahora);
                uiEstacionSensorRepository.save(nuevo);
            }
        }
    }

    public long contarSensoresActivos() {
        return uiEstacionSensorRepository.countByActivoTrue();
    }

    public long contarSensoresRegistrados() {
        return uiEstacionSensorRepository.count();
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