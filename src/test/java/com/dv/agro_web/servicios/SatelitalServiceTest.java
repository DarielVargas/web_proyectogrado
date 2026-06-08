package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.IndiceSatelital;
import com.dv.agro_web.repositorios.IndiceSatelitalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SatelitalServiceTest {

    @Mock
    private IndiceSatelitalRepository indiceSatelitalRepository;

    private SatelitalService satelitalService;

    @BeforeEach
    void setUp() {
        satelitalService = new SatelitalService(indiceSatelitalRepository);
    }

    @Test
    void debeObtenerFechaDelUltimoAnalisisSatelital() {
        LocalDateTime fecha = LocalDateTime.of(2026, 6, 1, 8, 30);
        IndiceSatelital indice = new IndiceSatelital();
        indice.setFecha(fecha);

        when(indiceSatelitalRepository.findTopByOrderByFechaDescIdDesc()).thenReturn(Optional.of(indice));

        Optional<LocalDateTime> resultado = satelitalService.obtenerFechaUltimoAnalisis();

        assertTrue(resultado.isPresent());
        assertEquals(fecha, resultado.get());
    }

    @Test
    void debeRetornarVacioCuandoNoHayAnalisisSatelitales() {
        when(indiceSatelitalRepository.findTopByOrderByFechaDescIdDesc()).thenReturn(Optional.empty());

        Optional<LocalDateTime> resultado = satelitalService.obtenerFechaUltimoAnalisis();

        assertTrue(resultado.isEmpty());
    }
}