package com.dv.agro_web.servicios;

import com.dv.agro_web.controllers.IndiceSatelitalRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class IndiceSatelitalSchedulerService {

    private final SatelitalService satelitalService;

    public IndiceSatelitalSchedulerService(SatelitalService satelitalService) {
        this.satelitalService = satelitalService;
    }

    @Scheduled(fixedDelayString = "${app.satelital.scheduler.intervalo-ms:10000}")
    public void generarYGuardarIndiceAutomatico() {
        double ndvi = redondear3(decimalesAleatorio(-0.10, 0.90));
        double ndwi = redondear3(decimalesAleatorio(-0.60, 0.60));

        IndiceSatelitalRequest request = new IndiceSatelitalRequest();
        request.setFecha(LocalDateTime.now());
        request.setNdvi(ndvi);
        request.setNdwi(ndwi);
        request.setEstadoVegetacion(calcularEstadoVegetacion(ndvi));
        request.setEstadoHidrico(calcularEstadoHidrico(ndwi));

        satelitalService.guardarIndiceSatelital(request);
    }

    private double decimalesAleatorio(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private double redondear3(double valor) {
        return Math.round(valor * 1000.0d) / 1000.0d;
    }

    private String calcularEstadoVegetacion(double ndvi) {
        if (ndvi >= 0.6d) {
            return "Saludable";
        }
        if (ndvi >= 0.3d) {
            return "Moderado";
        }
        return "Crítico";
    }

    private String calcularEstadoHidrico(double ndwi) {
        if (ndwi >= 0.2d) {
            return "Saludable";
        }
        if (ndwi >= -0.1d) {
            return "Moderado";
        }
        return "Crítico";
    }
}
