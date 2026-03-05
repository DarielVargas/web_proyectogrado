package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.repositorios.EstacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstacionService {

    private final EstacionRepository estacionRepository;

    public EstacionService(EstacionRepository estacionRepository) {
        this.estacionRepository = estacionRepository;
    }

    public List<Estacion> obtenerEstaciones() {
        return estacionRepository.findAll();
    }

    public Optional<Estacion> obtenerEstacionPorId(Long id) {
        return estacionRepository.findById(id);
    }

    public boolean existeCodigo(String codigo) {
        return estacionRepository.existsByCodigoIgnoreCase(codigo);
    }

    public Estacion crearEstacion(Estacion estacion) {
        return estacionRepository.save(estacion);
    }
}
