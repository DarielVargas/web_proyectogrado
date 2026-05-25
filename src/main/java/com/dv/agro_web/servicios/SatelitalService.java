package com.dv.agro_web.servicios;

import com.dv.agro_web.controllers.IndiceSatelitalRequest;
import com.dv.agro_web.entidades.IndiceSatelital;
import com.dv.agro_web.repositorios.IndiceSatelitalRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    @Scheduled(cron = "0 0 8 */5 * *")
    public void guardarIndicesAutomaticos() {

        IndiceSatelital indice = new IndiceSatelital();

        indice.setFecha(LocalDateTime.now());

        indice.setNdvi(0.75);

        indice.setNdwi(0.28);

        indice.setEstadoVegetacion("Saludable");

        indice.setEstadoHidrico("Óptimo");

        indiceSatelitalRepository.save(indice);

        System.out.println("Índices satelitales guardados automáticamente");
    }
}