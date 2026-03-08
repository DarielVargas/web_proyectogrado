package com.dv.agro_web.servicios;

import com.dv.agro_web.entidades.Reporte;
import com.dv.agro_web.entidades.VwMedicionDetalle;
import com.dv.agro_web.repositorios.ReporteRepository;
import com.dv.agro_web.repositorios.VwMedicionDetalleRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ReporteService {

    private final ReporteRepository reporteRepository;
    private final VwMedicionDetalleRepository vwMedicionDetalleRepository;

    public ReporteService(ReporteRepository reporteRepository,
                          VwMedicionDetalleRepository vwMedicionDetalleRepository) {
        this.reporteRepository = reporteRepository;
        this.vwMedicionDetalleRepository = vwMedicionDetalleRepository;
    }

    @Transactional
    public Reporte guardarReporteGenerado(Long estacionId,
                                          LocalDate fechaInicio,
                                          LocalDate fechaFin,
                                          String tipoReporte) {
        Reporte reporte = new Reporte();
        reporte.setEstacionId(estacionId);
        reporte.setFechaInicio(fechaInicio);
        reporte.setFechaFin(fechaFin);
        reporte.setTipoReporte(tipoReporte);
        reporte.setNombreReporte(generarNombreReporte(tipoReporte, fechaInicio, fechaFin));
        reporte.setFechaGenerado(LocalDateTime.now());
        return reporteRepository.save(reporte);
    }

    public List<ReporteRepository.ReporteRecienteView> listarReportesRecientes() {
        return reporteRepository.findTop10Recientes();
    }

    public Optional<ReporteRepository.ReporteRecienteView> obtenerDetalleReporte(Long idReporte) {
        return reporteRepository.findDetalleById(idReporte);
    }

    @Transactional
    public boolean eliminarReportePorId(Long idReporte) {
        if (!reporteRepository.existsById(idReporte)) {
            return false;
        }
        reporteRepository.deleteById(idReporte);
        return true;
    }

    @Transactional(readOnly = true)
    public byte[] generarReportePdf(Long idReporte) {
        ReporteRepository.ReporteRecienteView reporte = reporteRepository.findDetalleById(idReporte)
                .orElseThrow(() -> new IllegalArgumentException("El reporte no existe."));

        if (reporte.getEstacionCodigo() == null) {
            throw new IllegalArgumentException("No se encontró la estación del reporte.");
        }

        List<VwMedicionDetalle> mediciones = vwMedicionDetalleRepository.findReportePorRangoByEstacionCodigo(
                reporte.getEstacionCodigo(),
                reporte.getFechaInicio(),
                reporte.getFechaFin()
        );

        return construirPdf(reporte, mediciones);
    }

    private byte[] construirPdf(ReporteRepository.ReporteRecienteView reporte,
                                List<VwMedicionDetalle> mediciones) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

            document.add(new Paragraph(reporte.getNombreReporte(), titleFont));
            document.add(new Paragraph("Estación: " + reporte.getEstacionCodigo()
                    + (reporte.getEstacionDescripcion() != null ? " - " + reporte.getEstacionDescripcion() : ""), metaFont));
            document.add(new Paragraph("Rango de fechas: " + reporte.getFechaInicio() + " a " + reporte.getFechaFin(), metaFont));
            document.add(new Paragraph("Tipo de reporte: " + reporte.getTipoReporte(), metaFont));
            document.add(new Paragraph("Generado: " + reporte.getFechaGenerado(), metaFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(new float[]{1.2f, 2.3f, 1.2f, 1.0f, 2.0f, 1.8f, 1.6f, 1.5f});
            table.setWidthPercentage(100);

            agregarEncabezado(table, "ID", headerFont);
            agregarEncabezado(table, "Fecha", headerFont);
            agregarEncabezado(table, "Valor", headerFont);
            agregarEncabezado(table, "Unidad", headerFont);
            agregarEncabezado(table, "Tipo Sensor", headerFont);
            agregarEncabezado(table, "Código Sensor", headerFont);
            agregarEncabezado(table, "Modelo", headerFont);
            agregarEncabezado(table, "Estación", headerFont);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (VwMedicionDetalle m : mediciones) {
                table.addCell(new Phrase(String.valueOf(m.getMedicionId()), cellFont));
                String fecha = m.getFechaMedicion() == null ? "" : m.getFechaMedicion().toLocalDateTime().format(dtf);
                table.addCell(new Phrase(fecha, cellFont));
                table.addCell(new Phrase(m.getValor() != null ? m.getValor().toPlainString() : "", cellFont));
                table.addCell(new Phrase(m.getUnidadMedida() != null ? m.getUnidadMedida() : "", cellFont));
                table.addCell(new Phrase(m.getTipoSensor() != null ? m.getTipoSensor() : "", cellFont));
                table.addCell(new Phrase(m.getCodigoSensor() != null ? m.getCodigoSensor() : "", cellFont));
                table.addCell(new Phrase(m.getModelo() != null ? m.getModelo() : "", cellFont));
                table.addCell(new Phrase(m.getEstacionCodigo() != null ? m.getEstacionCodigo() : "", cellFont));
            }

            if (mediciones.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No hay mediciones para este reporte.", cellFont));
                emptyCell.setColspan(8);
                table.addCell(emptyCell);
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF del reporte.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Error inesperado al generar el PDF.", e);
        }
    }

    private void agregarEncabezado(PdfPTable table, String texto, Font font) {
        PdfPCell headerCell = new PdfPCell(new Phrase(texto, font));
        table.addCell(headerCell);
    }

    private String generarNombreReporte(String tipoReporte, LocalDate fechaInicio, LocalDate fechaFin) {
        return switch (tipoReporte) {
            case "DIARIO" -> "Reporte Diario - " + fechaInicio;
            case "SEMANAL" -> "Reporte Semanal - " + fechaInicio + " al " + fechaFin;
            case "MENSUAL" -> "Reporte Mensual - " + fechaInicio.format(DateTimeFormatter.ofPattern("yyyy-MM", new Locale("es", "ES")));
            default -> "Reporte - " + fechaInicio + " al " + fechaFin;
        };
    }
}