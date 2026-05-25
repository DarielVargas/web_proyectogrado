package com.dv.agro_web.servicios;

import com.dv.agro_web.controllers.IndiceSatelitalRequest;
import com.dv.agro_web.entidades.IndiceSatelital;
import com.dv.agro_web.repositorios.IndiceSatelitalRepository;
import org.springframework.stereotype.Service;

@Service
public class SatelitalService {

    private final IndiceSatelitalRepository indiceSatelitalRepository;

    public SatelitalService(IndiceSatelitalRepository indiceSatelitalRepository) {
        this.indiceSatelitalRepository = indiceSatelitalRepository;
    }

    public IndiceSatelital guardarIndiceSatelital(IndiceSatelitalRequest request) {
        IndiceSatelital indiceSatelital = new IndiceSatelital();
        indiceSatelital.setFecha(request.getFecha());
        indiceSatelital.setNdvi(request.getNdvi());
        indiceSatelital.setNdwi(request.getNdwi());
        indiceSatelital.setEstadoVegetacion(request.getEstadoVegetacion());
        indiceSatelital.setEstadoHidrico(request.getEstadoHidrico());
        return indiceSatelitalRepository.save(indiceSatelital);
    }
}
