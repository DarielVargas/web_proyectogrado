package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.ImagenSatelital;
import com.dv.agro_web.repositorios.ImagenSatelitalRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class ImagenSatelitalService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String URL_BASE_SATELITAL = "/uploads/satelital/";

    private final ImagenSatelitalRepository imagenSatelitalRepository;

    public ImagenSatelitalService(ImagenSatelitalRepository imagenSatelitalRepository) {
        this.imagenSatelitalRepository = imagenSatelitalRepository;
    }

    public Optional<ImagenSatelitalDto> obtenerImagenMasReciente() {
        return imagenSatelitalRepository.findTopByOrderByFechaSubidaDescIdDesc()
                .flatMap(this::crearDto);
    }

    private Optional<ImagenSatelitalDto> crearDto(ImagenSatelital imagen) {
        String nombreArchivo = obtenerNombreArchivo(imagen.getRutaImagen());
        if (nombreArchivo.isBlank()) {
            return Optional.empty();
        }

        String fechaTexto = imagen.getFechaSubida() != null
                ? imagen.getFechaSubida().format(FORMATO_FECHA)
                : "N/A";

        return Optional.of(new ImagenSatelitalDto(
                imagen.getId(),
                URL_BASE_SATELITAL + nombreArchivo,
                fechaTexto
        ));
    }

    private String obtenerNombreArchivo(String rutaImagen) {
        if (rutaImagen == null || rutaImagen.isBlank()) {
            return "";
        }

        String rutaNormalizada = rutaImagen.trim().replace('\\', '/');
        return rutaNormalizada.substring(rutaNormalizada.lastIndexOf('/') + 1);
    }

    public record ImagenSatelitalDto(Long id, String urlImagen, String fechaSubidaTexto) {
    }
}
