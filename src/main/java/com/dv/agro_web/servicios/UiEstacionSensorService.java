package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.UiEstacionSensor;
import com.dv.agro_web.entidades.UiEstacionSensorId;
import com.dv.agro_web.repositorios.UiEstacionSensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UiEstacionSensorService {

    private final UiEstacionSensorRepository repository;

    public UiEstacionSensorService(UiEstacionSensorRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void ensureDefaultsExist(String estacionCodigo) {
        repository.ensureDefaultsExist(estacionCodigo);
    }

    @Transactional
    public List<UiEstacionSensor> listSensoresConfig(String estacionCodigo) {
        ensureDefaultsExist(estacionCodigo);
        return repository.findByIdEstacionCodigoOrderByIdTipoSensor(estacionCodigo);
    }

    @Transactional
    public void toggleSensor(String estacionCodigo, String tipoSensor) {
        UiEstacionSensorId id = new UiEstacionSensorId(estacionCodigo, tipoSensor);

        UiEstacionSensor sensor = repository.findById(id)
                .orElseGet(() -> {
                    UiEstacionSensor nuevo = new UiEstacionSensor();
                    nuevo.setId(id);
                    nuevo.setActivo(true);
                    return nuevo;
                });

        sensor.setActivo(!Boolean.TRUE.equals(sensor.getActivo()));
        sensor.setUpdatedAt(LocalDateTime.now());
        repository.save(sensor);
    }

    public Map<String, Set<String>> sensoresDesactivadosPorEstacion() {
        return repository.findAll().stream()
                .filter(s -> !Boolean.TRUE.equals(s.getActivo()))
                .collect(Collectors.groupingBy(
                        s -> s.getId().getEstacionCodigo(),
                        Collectors.mapping(s -> s.getId().getTipoSensor(), Collectors.toSet())
                ));
    }
}
