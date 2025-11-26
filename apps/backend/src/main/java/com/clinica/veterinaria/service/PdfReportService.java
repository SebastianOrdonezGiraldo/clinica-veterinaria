package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ReporteDTO;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Servicio para generar reportes en formato PDF.
 * 
 * <p>Este servicio proporciona funcionalidad para crear documentos PDF
 * profesionales con reportes operativos de la clínica veterinaria.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReportService {

    private static final String CLINICA_NOMBRE = "Clínica Veterinaria Universitaria Humboldt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final TemplateEngine templateEngine;

    /**
     * Genera un PDF con el reporte operativo.
     * 
     * @param reporte Datos del reporte a incluir en el PDF
     * @param periodo Periodo del reporte (hoy, semana, mes, año)
     * @return Array de bytes con el contenido del PDF
     * @throws IOException si hay error al generar el PDF
     */
    public byte[] generarReportePdf(ReporteDTO reporte, String periodo) throws IOException {
        log.info("Generando PDF de reporte para periodo: {}", periodo);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            // Fuentes
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontTitle = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Encabezado
            Paragraph title = new Paragraph(CLINICA_NOMBRE)
                    .setFont(fontTitle)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(title);

            Paragraph subtitle = new Paragraph("Reporte Operativo - " + periodo.toUpperCase())
                    .setFont(fontNormal)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            document.add(subtitle);

            Paragraph fecha = new Paragraph("Generado el: " + LocalDate.now().format(DATE_FORMATTER))
                    .setFont(fontNormal)
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(fecha);

            // Estadísticas generales
            document.add(crearTituloSeccion("Estadísticas Generales", fontBold));
            Table statsTable = new Table(2);
            statsTable.setWidth(UnitValue.createPercentValue(100));
            
            agregarFilaTabla(statsTable, "Total de Citas", String.valueOf(reporte.getTotalCitas()), fontNormal, fontBold);
            agregarFilaTabla(statsTable, "Total de Consultas", String.valueOf(reporte.getTotalConsultas()), fontNormal, fontBold);
            agregarFilaTabla(statsTable, "Total de Pacientes", String.valueOf(reporte.getTotalPacientes()), fontNormal, fontBold);
            agregarFilaTabla(statsTable, "Total de Veterinarios", String.valueOf(reporte.getTotalVeterinarios()), fontNormal, fontBold);
            
            document.add(statsTable);
            document.add(new Paragraph("\n"));

            // Citas por estado
            if (reporte.getCitasPorEstado() != null && !reporte.getCitasPorEstado().isEmpty()) {
                document.add(crearTituloSeccion("Citas por Estado", fontBold));
                Table estadoTable = crearTablaDosColumnas(
                    reporte.getCitasPorEstado(),
                    estado -> estado.getEstado(),
                    estado -> String.valueOf(estado.getCantidad()),
                    "Estado",
                    "Cantidad",
                    fontBold,
                    fontNormal
                );
                document.add(estadoTable);
                document.add(new Paragraph("\n"));
            }

            // Tendencia de citas
            if (reporte.getTendenciaCitas() != null && !reporte.getTendenciaCitas().isEmpty()) {
                document.add(crearTituloSeccion("Tendencia de Citas", fontBold));
                Table tendenciaTable = crearTablaDosColumnas(
                    reporte.getTendenciaCitas(),
                    tendencia -> tendencia.getMes(),
                    tendencia -> String.valueOf(tendencia.getCitas()),
                    "Mes",
                    "Citas",
                    fontBold,
                    fontNormal
                );
                document.add(tendenciaTable);
                document.add(new Paragraph("\n"));
            }

            // Pacientes por especie
            if (reporte.getPacientesPorEspecie() != null && !reporte.getPacientesPorEspecie().isEmpty()) {
                document.add(crearTituloSeccion("Pacientes por Especie", fontBold));
                Table especieTable = crearTablaDosColumnas(
                    reporte.getPacientesPorEspecie(),
                    especie -> especie.getEspecie(),
                    especie -> String.valueOf(especie.getCantidad()),
                    "Especie",
                    "Cantidad",
                    fontBold,
                    fontNormal
                );
                document.add(especieTable);
                document.add(new Paragraph("\n"));
            }

            // Atenciones por veterinario
            if (reporte.getAtencionesPorVeterinario() != null && !reporte.getAtencionesPorVeterinario().isEmpty()) {
                document.add(crearTituloSeccion("Atenciones por Veterinario", fontBold));
                Table vetTable = crearTablaDosColumnas(
                    reporte.getAtencionesPorVeterinario(),
                    vet -> vet.getNombre(),
                    vet -> String.valueOf(vet.getConsultas()),
                    "Veterinario",
                    "Consultas",
                    fontBold,
                    fontNormal
                );
                document.add(vetTable);
                document.add(new Paragraph("\n"));
            }

            // Top motivos de consulta
            if (reporte.getTopMotivosConsulta() != null && !reporte.getTopMotivosConsulta().isEmpty()) {
                document.add(crearTituloSeccion("Top Motivos de Consulta", fontBold));
                Table motivosTable = new Table(3);
                motivosTable.setWidth(UnitValue.createPercentValue(100));
                
                // Encabezado
                Cell header1 = new Cell().add(new Paragraph("Motivo").setFont(fontBold))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                Cell header2 = new Cell().add(new Paragraph("Cantidad").setFont(fontBold))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                Cell header3 = new Cell().add(new Paragraph("Porcentaje").setFont(fontBold))
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER);
                motivosTable.addHeaderCell(header1);
                motivosTable.addHeaderCell(header2);
                motivosTable.addHeaderCell(header3);
                
                // Filas
                reporte.getTopMotivosConsulta().forEach(motivo -> {
                    motivosTable.addCell(new Cell().add(new Paragraph(motivo.getMotivo()).setFont(fontNormal)));
                    motivosTable.addCell(new Cell().add(new Paragraph(String.valueOf(motivo.getCantidad())).setFont(fontNormal))
                            .setTextAlignment(TextAlignment.CENTER));
                    String porcentaje = String.format("%.1f%%", motivo.getPorcentaje());
                    motivosTable.addCell(new Cell().add(new Paragraph(porcentaje).setFont(fontNormal))
                            .setTextAlignment(TextAlignment.CENTER));
                });
                
                document.add(motivosTable);
            }

            // Pie de página
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph("Este reporte fue generado automáticamente por el sistema de gestión.")
                    .setFont(fontNormal)
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY);
            document.add(footer);

            document.close();
            
            log.info("✓ PDF generado exitosamente. Tamaño: {} bytes", baos.size());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("✗ Error al generar PDF: {}", e.getMessage(), e);
            throw new IOException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Crea un título de sección para el PDF.
     */
    private Paragraph crearTituloSeccion(String titulo, PdfFont font) {
        return new Paragraph(titulo)
                .setFont(font)
                .setFontSize(14)
                .setMarginTop(10)
                .setMarginBottom(5)
                .setFontColor(ColorConstants.BLUE);
    }

    /**
     * Agrega una fila a una tabla con dos columnas.
     */
    private void agregarFilaTabla(Table table, String label, String value, PdfFont fontLabel, PdfFont fontValue) {
        Cell cellLabel = new Cell().add(new Paragraph(label).setFont(fontLabel))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY);
        Cell cellValue = new Cell().add(new Paragraph(value).setFont(fontValue))
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(cellLabel);
        table.addCell(cellValue);
    }

    /**
     * Crea una tabla genérica de dos columnas.
     */
    private <T> Table crearTablaDosColumnas(
            java.util.List<T> datos,
            java.util.function.Function<T, String> getColumna1,
            java.util.function.Function<T, String> getColumna2,
            String header1,
            String header2,
            PdfFont fontHeader,
            PdfFont fontData) {
        
        Table table = new Table(2);
        table.setWidth(UnitValue.createPercentValue(100));
        
        // Encabezado
        Cell headerCell1 = new Cell().add(new Paragraph(header1).setFont(fontHeader))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        Cell headerCell2 = new Cell().add(new Paragraph(header2).setFont(fontHeader))
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(headerCell1);
        table.addHeaderCell(headerCell2);
        
        // Filas de datos
        datos.forEach(item -> {
            table.addCell(new Cell().add(new Paragraph(getColumna1.apply(item)).setFont(fontData)));
            table.addCell(new Cell().add(new Paragraph(getColumna2.apply(item)).setFont(fontData))
                    .setTextAlignment(TextAlignment.CENTER));
        });
        
        return table;
    }

    /**
     * Genera un PDF con los datos de una prescripción médica (receta veterinaria) usando plantilla HTML/CSS.
     * 
     * @param prescripcionDTO Datos de la prescripción
     * @param pacienteNombre Nombre del paciente
     * @param pacienteEspecie Especie del paciente
     * @param pacienteRaza Raza del paciente
     * @param propietarioNombre Nombre del propietario
     * @param consultaFecha Fecha de la consulta
     * @param profesionalNombre Nombre del veterinario
     * @return Array de bytes con el contenido del PDF
     * @throws IOException si hay error al generar el PDF
     */
    public byte[] generarPrescripcionPdf(
            com.clinica.veterinaria.dto.PrescripcionDTO prescripcionDTO,
            String pacienteNombre,
            String pacienteEspecie,
            String pacienteRaza,
            String propietarioNombre,
            LocalDateTime consultaFecha,
            String profesionalNombre) throws IOException {
        
        log.info("Generando PDF de prescripción ID: {} usando plantilla HTML", prescripcionDTO.getId());
        
        try {
            // Preparar contexto para Thymeleaf
            Context context = new Context(Locale.getDefault());
            context.setVariable("prescripcion", prescripcionDTO);
            context.setVariable("pacienteNombre", pacienteNombre);
            context.setVariable("pacienteEspecie", pacienteEspecie);
            context.setVariable("pacienteRaza", pacienteRaza);
            context.setVariable("propietarioNombre", propietarioNombre);
            context.setVariable("profesionalNombre", profesionalNombre);
            
            // Formatear fechas
            String consultaFechaStr = consultaFecha != null ? consultaFecha.toLocalDate().format(DATE_FORMATTER) : "N/A";
            String fechaEmisionStr = prescripcionDTO.getFechaEmision() != null 
                ? prescripcionDTO.getFechaEmision().toLocalDate().format(DATE_FORMATTER) 
                : "N/A";
            
            context.setVariable("consultaFecha", consultaFechaStr);
            context.setVariable("fechaEmision", fechaEmisionStr);
            
            // Procesar plantilla HTML con Thymeleaf
            String htmlContent = templateEngine.process("pdf/prescripcion", context);
            
            // Convertir HTML a PDF usando html2pdf
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(htmlContent, baos);
            
            log.info("✓ PDF de prescripción generado exitosamente usando plantilla HTML. Tamaño: {} bytes", baos.size());
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("✗ Error al generar PDF de prescripción: {}", e.getMessage(), e);
            throw new IOException("Error al generar PDF de prescripción: " + e.getMessage(), e);
        }
    }
}

