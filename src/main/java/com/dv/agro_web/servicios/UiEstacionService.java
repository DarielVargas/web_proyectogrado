package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.UiEstacion;
import com.dv.agro_web.repositorios.UiEstacionRepository;
import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UiEstacionService {

    private static final Duration MAX_TIEMPO_SIN_MEDICIONES = Duration.ofMinutes(1);

    private final UiEstacionRepository uiEstacionRepository;
    private final VwMedicionDetalleRepository medicionDetalleRepository;

    public UiEstacionService(UiEstacionRepository uiEstacionRepository,
                             VwMedicionDetalleRepository medicionDetalleRepository) {
        this.uiEstacionRepository = uiEstacionRepository;
        this.medicionDetalleRepository = medicionDetalleRepository;
    }

    public Map<String, Boolean> obtenerEstadosPorCodigo() {
        Map<String, Boolean> estados = uiEstacionRepository.findAll().stream()
                .collect(Collectors.toMap(
                        UiEstacion::getEstacionCodigo,
                        e -> Boolean.TRUE.equals(e.getActivo()),
                        (prev, next) -> next,
                        LinkedHashMap::new
                ));

        Map<String, LocalDateTime> ultimasMediciones = medicionDetalleRepository.findUltimaFechaMedicionPorEstacion().stream()
                .filter(resumen -> resumen.getEstacionCodigo() != null)
                .filter(resumen -> resumen.getFechaMedicion() != null)
                .collect(Collectors.toMap(
                        VwMedicionDetalleRepository.UltimaMedicionEstacion::getEstacionCodigo,
                        VwMedicionDetalleRepository.UltimaMedicionEstacion::getFechaMedicion,
                        (prev, next) -> prev,
                        LinkedHashMap::new
                ));

        LocalDateTime ahora = LocalDateTime.now();
        for (Map.Entry<String, LocalDateTime> entry : ultimasMediciones.entrySet()) {
            boolean inactivaPorTiempo = !entry.getValue()
                    .isAfter(ahora.minus(MAX_TIEMPO_SIN_MEDICIONES));
            estados.merge(entry.getKey(), !inactivaPorTiempo, (actual, calculado) -> actual && calculado);
        }

        return estados;
    }

    public List<String> obtenerCodigosActivos() {
        return obtenerEstadosPorCodigo().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList();
    }

    public long contarEstacionesActivas() {
        return obtenerEstadosPorCodigo().values().stream()
                .filter(Boolean.TRUE::equals)
                .count();
    }

    @Transactional
    public void alternarEstado(String estacionCodigo) {
        UiEstacion estado = uiEstacionRepository.findById(estacionCodigo)
                .orElseGet(() -> {
                    UiEstacion nuevo = new UiEstacion();
                    nuevo.setEstacionCodigo(estacionCodigo);
                    nuevo.setActivo(true);
                    return nuevo;
                });

        estado.setActivo(!Boolean.TRUE.equals(estado.getActivo()));
        estado.setUpdatedAt(LocalDateTime.now());
        uiEstacionRepository.save(estado);
    }
}
