package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.ImagenSatelital;
import com.dv.agro_web.repositorios.ImagenSatelitalRepository;
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
class ImagenSatelitalServiceTest {

    @Mock
    private ImagenSatelitalRepository imagenSatelitalRepository;

    private ImagenSatelitalService imagenSatelitalService;

    @BeforeEach
    void setUp() {
        imagenSatelitalService = new ImagenSatelitalService(imagenSatelitalRepository);
    }

    @Test
    void debeCrearUrlPublicaParaLaImagenSatelitalMasReciente() {
        ImagenSatelital imagen = new ImagenSatelital();
        imagen.setId(1L);
        imagen.setRutaImagen("uploads/satelital/sentinel-2026-06-08.jpg");
        imagen.setFechaSubida(LocalDateTime.of(2026, 6, 8, 10, 15));

        when(imagenSatelitalRepository.findTopByOrderByFechaSubidaDescIdDesc()).thenReturn(Optional.of(imagen));

        Optional<ImagenSatelitalService.ImagenSatelitalDto> resultado = imagenSatelitalService.obtenerImagenMasReciente();

        assertTrue(resultado.isPresent());
        assertEquals("/uploads/satelital/sentinel-2026-06-08.jpg", resultado.get().urlImagen());
        assertEquals("08/06/2026 10:15", resultado.get().fechaSubidaTexto());
    }

    @Test
    void debeRetornarVacioCuandoNoHayImagenesSatelitales() {
        when(imagenSatelitalRepository.findTopByOrderByFechaSubidaDescIdDesc()).thenReturn(Optional.empty());

        Optional<ImagenSatelitalService.ImagenSatelitalDto> resultado = imagenSatelitalService.obtenerImagenMasReciente();

        assertTrue(resultado.isEmpty());
    }
}