package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Alerta;
import com.dv.agro_web.entidades.VwMedicionDetalle;
import com.dv.agro_web.repositorios.AlertaRepository;
import com.dv.agro_web.repositorios.HistorialAlertaRepository;
import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertaServiceTest {

    @Mock
    private AlertaRepository alertaRepository;

    @Mock
    private HistorialAlertaRepository historialAlertaRepository;

    @Mock
    private VwMedicionDetalleRepository medicionDetalleRepository;

    private AlertaService alertaService;

    @BeforeEach
    void setUp() {
        alertaService = new AlertaService(alertaRepository, historialAlertaRepository, medicionDetalleRepository);
    }

    @Test
    void debeDispararSoloUnaVezLaMismaAlertaConfigurada() {
        Alerta alerta = new Alerta();
        alerta.setIdAlerta(1L);
        alerta.setEstacionCodigo("EST-001");
        alerta.setSensorTipo("Temperatura Ambiental");
        alerta.setOperador(">");
        alerta.setUmbral(BigDecimal.valueOf(28));
        alerta.setActiva(true);
        alerta.setFechaCreacion(LocalDateTime.now().minusMinutes(5));

        VwMedicionDetalle primeraMedicion = new VwMedicionDetalle();
        setCampo(primeraMedicion, "medicionId", 10L);
        setCampo(primeraMedicion, "valor", BigDecimal.valueOf(29));
        setCampo(primeraMedicion, "fechaMedicion", Timestamp.valueOf(LocalDateTime.now()));

        VwMedicionDetalle segundaMedicion = new VwMedicionDetalle();
        setCampo(segundaMedicion, "medicionId", 11L);
        setCampo(segundaMedicion, "valor", BigDecimal.valueOf(30));
        setCampo(segundaMedicion, "fechaMedicion", Timestamp.valueOf(LocalDateTime.now().plusMinutes(1)));

        when(alertaRepository.findAllByOrderByFechaCreacionAscIdAlertaAsc()).thenReturn(List.of(alerta));
        when(medicionDetalleRepository.findUltimaMedicionPorEstacionYSensorDesde(any(), any(), any(Timestamp.class)))
                .thenReturn(primeraMedicion)
                .thenReturn(segundaMedicion);
        when(alertaRepository.save(any(Alerta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MockHttpSession session = new MockHttpSession();

        List<AlertaService.NotificacionAlertaDto> primeraEvaluacion = alertaService.obtenerAlertasDisparadas(session);
        List<AlertaService.NotificacionAlertaDto> segundaEvaluacion = alertaService.obtenerAlertasDisparadas(session);

        assertEquals(1, primeraEvaluacion.size());
        assertEquals(0, segundaEvaluacion.size());
        verify(historialAlertaRepository, times(1)).save(any());
        verify(alertaRepository, times(1)).save(any(Alerta.class));
    }

    private void setCampo(VwMedicionDetalle medicion, String nombreCampo, Object valor) {
        try {
            Field campo = VwMedicionDetalle.class.getDeclaredField(nombreCampo);
            campo.setAccessible(true);
            campo.set(medicion, valor);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
