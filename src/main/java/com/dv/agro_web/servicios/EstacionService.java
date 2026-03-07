package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.repositorios.EstacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EstacionService {

    private final EstacionRepository estacionRepository;

    public EstacionService(EstacionRepository estacionRepository) {
        this.estacionRepository = estacionRepository;
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

    public Estacion crearEstacion(Estacion estacion) {
        if (estacion.getActiva() == null) {
            estacion.setActiva(true);
        }
        return estacionRepository.save(estacion);
    }

    @Transactional
    public boolean desactivarEstacion(Long id) {
        return estacionRepository.desactivarPorId(id) > 0;
    }
}
