package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.UiEstacion;
import com.dv.agro_web.repositorios.UiEstacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UiEstacionService {

    private final UiEstacionRepository uiEstacionRepository;

    public UiEstacionService(UiEstacionRepository uiEstacionRepository) {
        this.uiEstacionRepository = uiEstacionRepository;
    }

    public Map<String, Boolean> obtenerEstadosPorCodigo() {
        return uiEstacionRepository.findAll().stream()
                .collect(Collectors.toMap(
                        UiEstacion::getEstacionCodigo,
                        e -> Boolean.TRUE.equals(e.getActivo()),
                        (prev, next) -> next
                ));
    }

    public List<String> obtenerCodigosActivos() {
        return uiEstacionRepository.findByActivoTrue().stream()
                .map(UiEstacion::getEstacionCodigo)
                .toList();
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
