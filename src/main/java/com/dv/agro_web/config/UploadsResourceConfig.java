package com.dv.agro_web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class UploadsResourceConfig implements WebMvcConfigurer {

    private final Path directorioUploads;
    private final String ubicacionRecursos;

    public UploadsResourceConfig(@Value("${app.uploads.directorio:uploads}") String directorioUploads) {
        this.directorioUploads = Path.of(directorioUploads).toAbsolutePath().normalize();
        String ubicacion = this.directorioUploads.toUri().toString();
        this.ubicacionRecursos = ubicacion.endsWith("/") ? ubicacion : ubicacion + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(ubicacionRecursos);
    }
}
