package com.dv.agro_web.servicios;

import com.dv.agro_web.controllers.forms.InformacionCultivoForm;
import com.dv.agro_web.entidades.Estacion;
import com.dv.agro_web.entidades.InformacionCultivo;
import com.dv.agro_web.repositorios.EstacionRepository;
import com.dv.agro_web.repositorios.InformacionCultivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InformacionCultivoService {

    private static final List<String> LOTES_DISPONIBLES = List.of(
            "Lote Norte",
            "Lote Sur",
            "Lote Oriente",
            "Lote Occidente",
            "Invernadero Principal",
            "Parcela Experimental"
    );

    private final InformacionCultivoRepository informacionCultivoRepository;
    private final EstacionRepository estacionRepository;

    public InformacionCultivoService(InformacionCultivoRepository informacionCultivoRepository,
                                     EstacionRepository estacionRepository) {
        this.informacionCultivoRepository = informacionCultivoRepository;
        this.estacionRepository = estacionRepository;
    }

    @Transactional(readOnly = true)
    public Optional<InformacionCultivo> obtenerInformacionPrincipal() {
        return informacionCultivoRepository.findFirstByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public InformacionCultivoForm obtenerFormularioActual() {
        return obtenerInformacionPrincipal()
                .map(this::aFormulario)
                .orElseGet(InformacionCultivoForm::new);
    }

    @Transactional(readOnly = true)
    public List<String> obtenerLotesDisponibles(String loteActual) {
        if (loteActual == null || loteActual.isBlank() || LOTES_DISPONIBLES.contains(loteActual)) {
            return LOTES_DISPONIBLES;
        }

        return java.util.stream.Stream.concat(java.util.stream.Stream.of(loteActual), LOTES_DISPONIBLES.stream())
                .toList();
    }

    @Transactional
    public InformacionCultivo guardar(InformacionCultivoForm form) {
        InformacionCultivo informacion = obtenerInformacionPrincipal()
                .orElseGet(InformacionCultivo::new);

        Estacion estacion = estacionRepository.findById(form.getEstacionId())
                .orElseThrow(() -> new IllegalArgumentException("La estación seleccionada no existe"));

        informacion.setNombreCultivo(form.getNombreCultivo());
        informacion.setEncargado(form.getEncargado());
        informacion.setLoteArea(form.getLoteArea());
        informacion.setEstacion(estacion);
        informacion.setFechaInicio(form.getFechaInicio());
        informacion.setFechaCosechaEstimada(
                form.getFechaCosechaEstimada() != null ? form.getFechaCosechaEstimada() : form.getFechaInicio().plusMonths(10)
        );
        informacion.setObservaciones(form.getObservaciones());
        informacion.setUltimaSupervision(LocalDateTime.now());

        return informacionCultivoRepository.save(informacion);
    }

    public InformacionCultivoForm aFormulario(InformacionCultivo informacion) {
        InformacionCultivoForm form = new InformacionCultivoForm();
        form.setId(informacion.getId());
        form.setNombreCultivo(informacion.getNombreCultivo());
        form.setEncargado(informacion.getEncargado());
        form.setLoteArea(informacion.getLoteArea());
        form.setEstacionId(informacion.getEstacion() != null ? informacion.getEstacion().getId() : null);
        form.setFechaInicio(informacion.getFechaInicio());
        form.setFechaCosechaEstimada(informacion.getFechaCosechaEstimada());
        form.setObservaciones(informacion.getObservaciones());
        return form;
    }
}
