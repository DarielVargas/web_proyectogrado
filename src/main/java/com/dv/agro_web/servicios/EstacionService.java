package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.repositorios.EstacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EstacionService {

    private final EstacionRepository estacionRepository;

    public EstacionService(EstacionRepository estacionRepository) {
        this.estacionRepository = estacionRepository;
    }

    public List<Estacion> obtenerEstaciones() {
        return estacionRepository.findAll();
    }
}