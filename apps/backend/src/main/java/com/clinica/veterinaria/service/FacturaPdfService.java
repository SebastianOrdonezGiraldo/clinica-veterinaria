package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.FacturaDTO;
import com.clinica.veterinaria.dto.ItemFacturaDTO;
import com.clinica.veterinaria.dto.PagoDTO;
import com.clinica.veterinaria.entity.Factura;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Servicio para generar PDFs de facturas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaPdfService {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN);

    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    /**
     * Genera un PDF de la factura
     */
    public byte[] generatePdf(FacturaDTO factura) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = PAGE_HEIGHT - MARGIN;
                
                // Encabezado
                yPosition = drawHeader(contentStream, factura, yPosition);
                
                // Información del cliente
                yPosition = drawClienteInfo(contentStream, factura, yPosition);
                
                // Items de la factura
                yPosition = drawItems(contentStream, factura, yPosition);
                
                // Totales
                yPosition = drawTotales(contentStream, factura, yPosition);
                
                // Pagos
                if (factura.getPagos() != null && !factura.getPagos().isEmpty()) {
                    yPosition = drawPagos(contentStream, factura, yPosition);
                }
                
                // Pie de página
                drawFooter(contentStream, factura);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private float drawHeader(PDPageContentStream contentStream, FacturaDTO factura, float yPosition) throws IOException {
        PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        // Título
        contentStream.beginText();
        contentStream.setFont(titleFont, 24);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("FACTURA");
        contentStream.endText();

        // Número de factura
        yPosition -= 30;
        contentStream.beginText();
        contentStream.setFont(titleFont, 14);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Número: " + factura.getNumeroFactura());
        contentStream.endText();

        // Fecha
        yPosition -= 20;
        contentStream.beginText();
        contentStream.setFont(normalFont, 10);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        String fecha = factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        contentStream.showText("Fecha de Emisión: " + fecha);
        contentStream.endText();

        if (factura.getFechaVencimiento() != null) {
            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            String fechaVenc = factura.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            contentStream.showText("Fecha de Vencimiento: " + fechaVenc);
            contentStream.endText();
        }

        // Estado
        yPosition -= 15;
        contentStream.beginText();
        contentStream.setFont(normalFont, 10);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Estado: " + getEstadoLabel(factura.getEstado()));
        contentStream.endText();

        return yPosition - 30;
    }

    private float drawClienteInfo(PDPageContentStream contentStream, FacturaDTO factura, float yPosition) throws IOException {
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        contentStream.beginText();
        contentStream.setFont(boldFont, 12);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Cliente:");
        contentStream.endText();

        yPosition -= 20;
        contentStream.beginText();
        contentStream.setFont(normalFont, 10);
        contentStream.newLineAtOffset(MARGIN + 10, yPosition);
        contentStream.showText(factura.getPropietarioNombre() != null ? factura.getPropietarioNombre() : "N/A");
        contentStream.endText();

        return yPosition - 20;
    }

    private float drawItems(PDPageContentStream contentStream, FacturaDTO factura, float yPosition) throws IOException {
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        // Encabezado de tabla
        yPosition -= 10;
        contentStream.beginText();
        contentStream.setFont(boldFont, 11);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Items:");
        contentStream.endText();

        yPosition -= 20;
        float tableTop = yPosition;
        
        // Dibujar líneas de encabezado
        contentStream.setLineWidth(1f);
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(PAGE_WIDTH - MARGIN, yPosition);
        contentStream.stroke();

        // Encabezados de columna
        yPosition -= 15;
        contentStream.beginText();
        contentStream.setFont(boldFont, 9);
        contentStream.newLineAtOffset(MARGIN + 5, yPosition);
        contentStream.showText("Descripción");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 9);
        contentStream.newLineAtOffset(MARGIN + 200, yPosition);
        contentStream.showText("Cant.");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 9);
        contentStream.newLineAtOffset(MARGIN + 250, yPosition);
        contentStream.showText("Precio Unit.");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 9);
        contentStream.newLineAtOffset(MARGIN + 350, yPosition);
        contentStream.showText("Subtotal");
        contentStream.endText();

        yPosition -= 10;
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(PAGE_WIDTH - MARGIN, yPosition);
        contentStream.stroke();

        // Items
        if (factura.getItems() != null) {
            for (ItemFacturaDTO item : factura.getItems()) {
                yPosition -= 20;
                
                if (yPosition < MARGIN + 100) {
                    // Nueva página si es necesario
                    break;
                }

                // Descripción (puede ser multilínea)
                String descripcion = item.getDescripcion();
                if (descripcion.length() > 40) {
                    descripcion = descripcion.substring(0, 37) + "...";
                }
                
                contentStream.beginText();
                contentStream.setFont(normalFont, 9);
                contentStream.newLineAtOffset(MARGIN + 5, yPosition);
                contentStream.showText(descripcion);
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 9);
                contentStream.newLineAtOffset(MARGIN + 200, yPosition);
                contentStream.showText(item.getCantidad().toString());
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(normalFont, 9);
                contentStream.newLineAtOffset(MARGIN + 250, yPosition);
                contentStream.showText(formatCurrency(item.getPrecioUnitario()));
                contentStream.endText();

                BigDecimal subtotal = item.getSubtotal() != null ? item.getSubtotal() : 
                    item.getCantidad().multiply(item.getPrecioUnitario());
                contentStream.beginText();
                contentStream.setFont(normalFont, 9);
                contentStream.newLineAtOffset(MARGIN + 350, yPosition);
                contentStream.showText(formatCurrency(subtotal));
                contentStream.endText();
            }
        }

        // Línea final de tabla
        yPosition -= 10;
        contentStream.setLineWidth(1f);
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(PAGE_WIDTH - MARGIN, yPosition);
        contentStream.stroke();

        return yPosition - 20;
    }

    private float drawTotales(PDPageContentStream contentStream, FacturaDTO factura, float yPosition) throws IOException {
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float rightMargin = PAGE_WIDTH - MARGIN - 150;

        // Subtotal
        yPosition -= 10;
        contentStream.beginText();
        contentStream.setFont(normalFont, 10);
        contentStream.newLineAtOffset(rightMargin, yPosition);
        contentStream.showText("Subtotal:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(normalFont, 10);
        contentStream.newLineAtOffset(PAGE_WIDTH - MARGIN - 80, yPosition);
        contentStream.showText(formatCurrency(factura.getSubtotal()));
        contentStream.endText();

        // Descuento
        if (factura.getDescuento() != null && factura.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(rightMargin, yPosition);
            contentStream.showText("Descuento:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(PAGE_WIDTH - MARGIN - 80, yPosition);
            contentStream.showText("-" + formatCurrency(factura.getDescuento()));
            contentStream.endText();
        }

        // Impuesto
        if (factura.getImpuesto() != null && factura.getImpuesto().compareTo(BigDecimal.ZERO) > 0) {
            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(rightMargin, yPosition);
            contentStream.showText("Impuesto:");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(normalFont, 10);
            contentStream.newLineAtOffset(PAGE_WIDTH - MARGIN - 80, yPosition);
            contentStream.showText(formatCurrency(factura.getImpuesto()));
            contentStream.endText();
        }

        // Total
        yPosition -= 20;
        contentStream.setLineWidth(1f);
        contentStream.moveTo(rightMargin, yPosition);
        contentStream.lineTo(PAGE_WIDTH - MARGIN, yPosition);
        contentStream.stroke();

        yPosition -= 15;
        contentStream.beginText();
        contentStream.setFont(boldFont, 12);
        contentStream.newLineAtOffset(rightMargin, yPosition);
        contentStream.showText("TOTAL:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 12);
        contentStream.newLineAtOffset(PAGE_WIDTH - MARGIN - 80, yPosition);
        contentStream.showText(formatCurrency(factura.getTotal()));
        contentStream.endText();

        // Monto pagado y pendiente
        yPosition -= 25;
        contentStream.beginText();
        contentStream.setFont(normalFont, 10);
        contentStream.newLineAtOffset(rightMargin, yPosition);
        contentStream.showText("Pagado:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(normalFont, 10);
        contentStream.newLineAtOffset(PAGE_WIDTH - MARGIN - 80, yPosition);
        contentStream.showText(formatCurrency(factura.getMontoPagado()));
        contentStream.endText();

        yPosition -= 15;
        contentStream.beginText();
        contentStream.setFont(boldFont, 10);
        contentStream.newLineAtOffset(rightMargin, yPosition);
        contentStream.showText("Pendiente:");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(boldFont, 10);
        contentStream.newLineAtOffset(PAGE_WIDTH - MARGIN - 80, yPosition);
        contentStream.showText(formatCurrency(factura.getMontoPendiente()));
        contentStream.endText();

        return yPosition - 20;
    }

    private float drawPagos(PDPageContentStream contentStream, FacturaDTO factura, float yPosition) throws IOException {
        PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        if (yPosition < MARGIN + 150) {
            return yPosition; // No hay espacio suficiente
        }

        yPosition -= 20;
        contentStream.beginText();
        contentStream.setFont(boldFont, 11);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("Historial de Pagos:");
        contentStream.endText();

        yPosition -= 15;
        for (PagoDTO pago : factura.getPagos()) {
            if (yPosition < MARGIN + 50) {
                break;
            }

            String fechaPago = "N/A";
            if (pago.getFechaPago() != null) {
                fechaPago = pago.getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }

            contentStream.beginText();
            contentStream.setFont(normalFont, 9);
            contentStream.newLineAtOffset(MARGIN + 10, yPosition);
            contentStream.showText(String.format("%s - %s (%s)", 
                formatCurrency(pago.getMonto()),
                fechaPago,
                pago.getMetodoPago() != null ? pago.getMetodoPago() : "N/A"));
            contentStream.endText();

            yPosition -= 15;
        }

        return yPosition;
    }

    private void drawFooter(PDPageContentStream contentStream, FacturaDTO factura) throws IOException {
        PDType1Font normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        float footerY = MARGIN + 20;

        if (factura.getObservaciones() != null && !factura.getObservaciones().isEmpty()) {
            contentStream.beginText();
            contentStream.setFont(normalFont, 9);
            contentStream.newLineAtOffset(MARGIN, footerY);
            contentStream.showText("Observaciones: " + factura.getObservaciones());
            contentStream.endText();
            footerY -= 15;
        }

        contentStream.beginText();
        contentStream.setFont(normalFont, 8);
        contentStream.newLineAtOffset(MARGIN, footerY);
        contentStream.showText("Gracias por su preferencia");
        contentStream.endText();
    }

    private String formatCurrency(BigDecimal amount) {
        return currencyFormatter.format(amount);
    }

    private String getEstadoLabel(Factura.EstadoFactura estado) {
        return switch (estado) {
            case PENDIENTE -> "Pendiente";
            case PARCIAL -> "Pago Parcial";
            case PAGADA -> "Pagada";
            case CANCELADA -> "Cancelada";
            case VENCIDA -> "Vencida";
        };
    }
}

