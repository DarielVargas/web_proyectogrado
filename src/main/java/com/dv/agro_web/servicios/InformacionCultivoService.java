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

    private static final List<String> LOTES_DISPONIBLES = List.of("Norte", "Sur", "Este", "Oeste");

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
    public List<InformacionCultivo> obtenerCultivos() {
        return informacionCultivoRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public InformacionCultivoForm obtenerFormularioActual() {
        return obtenerInformacionPrincipal()
                .map(this::aFormulario)
                .orElseGet(InformacionCultivoForm::new);
    }

    @Transactional(readOnly = true)
    public List<String> obtenerLotesDisponibles(String loteActual) {
        return LOTES_DISPONIBLES;
    }

    @Transactional(readOnly = true)
    public List<String> obtenerLotesDisponibles() {
        return LOTES_DISPONIBLES;
    }

    @Transactional
    public InformacionCultivo guardar(InformacionCultivoForm form) {
        validarLote(form.getLoteArea());

        InformacionCultivo informacion = form.getId() != null
                ? informacionCultivoRepository.findById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("El cultivo seleccionado no existe"))
                : new InformacionCultivo();

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
        if (informacion.getUltimaSupervision() == null) {
            informacion.setUltimaSupervision(LocalDateTime.now());
        }

        return informacionCultivoRepository.save(informacion);
    }

    @Transactional
    public void registrarSupervision(Long id) {
        InformacionCultivo informacion = informacionCultivoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El cultivo seleccionado no existe"));
        informacion.setUltimaSupervision(LocalDateTime.now());
        informacionCultivoRepository.save(informacion);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!informacionCultivoRepository.existsById(id)) {
            throw new IllegalArgumentException("El cultivo seleccionado no existe");
        }
        informacionCultivoRepository.deleteById(id);
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

    private void validarLote(String loteArea) {
        if (!LOTES_DISPONIBLES.contains(loteArea)) {
            throw new IllegalArgumentException("Seleccione un lote válido: Norte, Sur, Este u Oeste");
        }
    }
}
