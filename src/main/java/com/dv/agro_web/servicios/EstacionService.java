package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.entidades.Sensor;
import com.dv.agro_web.entidades.TipoSensor;
import com.dv.agro_web.repositorios.EstacionRepository;
import com.dv.agro_web.repositorios.SensorRepository;
import com.dv.agro_web.repositorios.TipoSensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EstacionService {

    private final EstacionRepository estacionRepository;
    private final TipoSensorRepository tipoSensorRepository;
    private final SensorRepository sensorRepository;

    public EstacionService(EstacionRepository estacionRepository,
                           TipoSensorRepository tipoSensorRepository,
                           SensorRepository sensorRepository) {
        this.estacionRepository = estacionRepository;
        this.tipoSensorRepository = tipoSensorRepository;
        this.sensorRepository = sensorRepository;
    }

    public List<Estacion> obtenerEstacionesActivas() {
        return estacionRepository.findAllActivas();
    }

    public Optional<Estacion> obtenerEstacionActivaPorId(Long id) {
        return estacionRepository.findActivaById(id);
    }

    public Optional<Estacion> obtenerEstacionActivaPorCodigo(String codigo) {
        return estacionRepository.findActivaByCodigoIgnoreCase(codigo);
    }

    public boolean existeCodigoActivo(String codigo) {
        return estacionRepository.existsActivaByCodigoIgnoreCase(codigo);
    }

    @Transactional
    public Estacion crearEstacion(Estacion estacion) {
        if (estacion.getActiva() == null) {
            estacion.setActiva(true);
        }

        Estacion estacionGuardada = estacionRepository.save(estacion);
        crearSensoresBaseSiNoExisten(estacionGuardada);
        return estacionGuardada;
    }

    @Transactional
    public boolean desactivarEstacion(Long id) {
        return estacionRepository.desactivarPorId(id) > 0;
    }

    private void crearSensoresBaseSiNoExisten(Estacion estacion) {
        List<TipoSensor> tiposSensor = tipoSensorRepository.findAll();

        for (TipoSensor tipoSensor : tiposSensor) {
            Long tipoSensorId = tipoSensor.getId();
            if (tipoSensorId == null) {
                continue;
            }

            boolean yaExiste = sensorRepository.existsByEstacionIdAndIdTipoSensor(estacion.getId(), tipoSensorId);
            if (yaExiste) {
                continue;
            }

            Sensor sensor = new Sensor();
            sensor.setEstacionId(estacion.getId());
            sensor.setIdTipoSensor(tipoSensorId);
            sensor.setCodigoSensor(generarCodigoSensor(estacion.getCodigo(), tipoSensorId));
            sensorRepository.save(sensor);
        }
    }

    private String generarCodigoSensor(String codigoEstacion, Long idTipoSensor) {
        return codigoEstacion + "_T" + idTipoSensor;
    }
}
