package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.UiEstacion;
import com.dv.agro_web.repositorios.UiEstacionRepository;
import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UiEstacionServiceTest {

    @Mock
    private UiEstacionRepository uiEstacionRepository;

    @Mock
    private VwMedicionDetalleRepository medicionDetalleRepository;

    private UiEstacionService uiEstacionService;

    @BeforeEach
    void setUp() {
        uiEstacionService = new UiEstacionService(uiEstacionRepository, medicionDetalleRepository);
    }

    @Test
    void debeMarcarComoInactivaUnaEstacionConMasDeTreintaSegundosSinMediciones() {
        UiEstacion estadoManual = new UiEstacion();
        estadoManual.setEstacionCodigo("EST-001");
        estadoManual.setActivo(true);

        when(uiEstacionRepository.findAll()).thenReturn(List.of(estadoManual));
        when(medicionDetalleRepository.findUltimaFechaMedicionPorEstacion()).thenReturn(List.of(
                ultimaMedicion("EST-001", LocalDateTime.now().minusSeconds(30))
        ));

        Map<String, Boolean> estados = uiEstacionService.obtenerEstadosPorCodigo();

        assertEquals(false, estados.get("EST-001"));
        assertEquals(0, uiEstacionService.contarEstacionesActivas());
        assertEquals(List.of(), uiEstacionService.obtenerCodigosActivos());
    }

    @Test
    void debeMantenerInactivaUnaEstacionDesactivadaManualmenteAunqueTengaMedicionesRecientes() {
        UiEstacion estadoManual = new UiEstacion();
        estadoManual.setEstacionCodigo("EST-002");
        estadoManual.setActivo(false);

        when(uiEstacionRepository.findAll()).thenReturn(List.of(estadoManual));
        when(medicionDetalleRepository.findUltimaFechaMedicionPorEstacion()).thenReturn(List.of(
                ultimaMedicion("EST-002", LocalDateTime.now().minusSeconds(30))
        ));

        Map<String, Boolean> estados = uiEstacionService.obtenerEstadosPorCodigo();

        assertEquals(false, estados.get("EST-002"));
    }

    @Test
    void debeMantenerOperativaUnaEstacionConMenosDeTreintaSegundosSinMediciones() {
        when(uiEstacionRepository.findAll()).thenReturn(List.of());
        when(medicionDetalleRepository.findUltimaFechaMedicionPorEstacion()).thenReturn(List.of(
                ultimaMedicion("EST-003", LocalDateTime.now().minusSeconds(29))
        ));

        Map<String, Boolean> estados = uiEstacionService.obtenerEstadosPorCodigo();

        assertEquals(true, estados.get("EST-003"));
        assertEquals(1, uiEstacionService.contarEstacionesActivas());
        assertEquals(List.of("EST-003"), uiEstacionService.obtenerCodigosActivos());
    }

    private VwMedicionDetalleRepository.UltimaMedicionEstacion ultimaMedicion(String codigo, LocalDateTime fecha) {
        return new VwMedicionDetalleRepository.UltimaMedicionEstacion() {
            @Override
            public String getEstacionCodigo() {
                return codigo;
            }

            @Override
            public LocalDateTime getFechaMedicion() {
                return fecha;
            }
        };
    }
}