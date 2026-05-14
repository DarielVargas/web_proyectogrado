package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.CalendarioAgricola;
import com.dv.agro_web.repositorios.CalendarioAgricolaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CalendarioAgricolaService {

    public static final List<String> TIPOS_TAREA = List.of(
            "Riego",
            "Fertilización",
            "Poda",
            "Control de Plagas",
            "Cosecha",
            "Siembra"
    );

    private final CalendarioAgricolaRepository calendarioAgricolaRepository;

    public CalendarioAgricolaService(CalendarioAgricolaRepository calendarioAgricolaRepository) {
        this.calendarioAgricolaRepository = calendarioAgricolaRepository;
    }

    public List<CalendarioAgricola> listarPorEstado(String estado) {
        LocalDateTime ahora = LocalDateTime.now();
        return switch (normalizarEstado(estado)) {
            case "pendientes" -> calendarioAgricolaRepository
                    .findAllByCompletadaFalseAndFechaHoraGreaterThanEqualOrderByFechaHoraAscIdAsc(ahora);
            case "atrasadas" -> calendarioAgricolaRepository
                    .findAllByCompletadaFalseAndFechaHoraBeforeOrderByFechaHoraAscIdAsc(ahora);
            case "completadas" -> calendarioAgricolaRepository
                    .findAllByCompletadaTrueOrderByFechaCompletadaDescFechaHoraDescIdDesc();
            default -> calendarioAgricolaRepository.findAllByOrderByFechaHoraAscIdAsc();
        };
    }

    public ConteoTareas contarPorEstado() {
        LocalDateTime ahora = LocalDateTime.now();
        long pendientes = calendarioAgricolaRepository.countByCompletadaFalseAndFechaHoraGreaterThanEqual(ahora);
        long atrasadas = calendarioAgricolaRepository.countByCompletadaFalseAndFechaHoraBefore(ahora);
        long completadas = calendarioAgricolaRepository.countByCompletadaTrue();
        return new ConteoTareas(pendientes + atrasadas + completadas, pendientes, atrasadas, completadas);
    }

    @Transactional
    public CalendarioAgricola crearTarea(String titulo,
                                         String descripcion,
                                         String tipo,
                                         LocalDateTime fechaHora,
                                         String asignadoA,
                                         Integer duracionMinutos) {
        CalendarioAgricola tarea = new CalendarioAgricola();
        aplicarDatosEditables(tarea, titulo, descripcion, tipo, fechaHora, asignadoA, duracionMinutos);
        tarea.setCompletada(false);
        tarea.setFechaCompletada(null);
        return calendarioAgricolaRepository.save(tarea);
    }

    @Transactional
    public CalendarioAgricola editarTarea(Long id,
                                          String titulo,
                                          String descripcion,
                                          String tipo,
                                          LocalDateTime fechaHora,
                                          String asignadoA,
                                          Integer duracionMinutos) {
        CalendarioAgricola tarea = calendarioAgricolaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La tarea seleccionada no existe."));
        aplicarDatosEditables(tarea, titulo, descripcion, tipo, fechaHora, asignadoA, duracionMinutos);
        return calendarioAgricolaRepository.save(tarea);
    }

    @Transactional
    public void eliminarTarea(Long id) {
        if (calendarioAgricolaRepository.existsById(id)) {
            calendarioAgricolaRepository.deleteById(id);
        }
    }

    @Transactional
    public void actualizarEstadoCompletada(Long id, boolean completada) {
        calendarioAgricolaRepository.findById(id).ifPresent(tarea -> {
            tarea.setCompletada(completada);
            tarea.setFechaCompletada(completada ? LocalDateTime.now() : null);
            calendarioAgricolaRepository.save(tarea);
        });
    }

    public boolean esAtrasada(CalendarioAgricola tarea) {
        return !Boolean.TRUE.equals(tarea.getCompletada())
                && tarea.getFechaHora() != null
                && tarea.getFechaHora().isBefore(LocalDateTime.now());
    }

    public String normalizarEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return "todas";
        }
        String estadoNormalizado = estado.trim().toLowerCase();
        return List.of("todas", "pendientes", "atrasadas", "completadas").contains(estadoNormalizado)
                ? estadoNormalizado
                : "todas";
    }

    private void aplicarDatosEditables(CalendarioAgricola tarea,
                                       String titulo,
                                       String descripcion,
                                       String tipo,
                                       LocalDateTime fechaHora,
                                       String asignadoA,
                                       Integer duracionMinutos) {
        if (fechaHora == null) {
            throw new IllegalArgumentException("Seleccione la fecha y hora de la tarea.");
        }
        tarea.setTitulo(limpiar(titulo));
        tarea.setDescripcion(limpiarOpcional(descripcion));
        tarea.setTipo(validarTipo(tipo));
        tarea.setFechaHora(fechaHora);
        tarea.setAsignadoA(limpiarOpcional(asignadoA));
        tarea.setDuracionMinutos(duracionMinutos == null || duracionMinutos <= 0 ? 60 : duracionMinutos);
    }

    private String validarTipo(String tipo) {
        String tipoLimpio = limpiar(tipo);
        if (!TIPOS_TAREA.contains(tipoLimpio)) {
            throw new IllegalArgumentException("Seleccione un tipo de tarea válido.");
        }
        return tipoLimpio;
    }

    private String limpiar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Complete los campos obligatorios.");
        }
        return valor.trim();
    }

    private String limpiarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public record ConteoTareas(long todas, long pendientes, long atrasadas, long completadas) {
    }
}