package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Factura;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para Factura
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacturaDTO {
    private Long id;
    
    private String numeroFactura;
    
    @NotNull(message = "La fecha de emisión es requerida")
    private LocalDate fechaEmision;
    
    private LocalDate fechaVencimiento;
    
    @NotNull(message = "El subtotal es requerido")
    private BigDecimal subtotal;
    
    private BigDecimal descuento;
    
    private BigDecimal impuesto;
    
    @NotNull(message = "El total es requerido")
    private BigDecimal total;
    
    private BigDecimal montoPagado;
    
    private BigDecimal montoPendiente;
    
    private String observaciones;
    
    private Factura.EstadoFactura estado;
    
    private Long propietarioId;
    
    private String propietarioNombre;
    
    private Long consultaId;
    
    @Valid
    @Builder.Default
    private List<ItemFacturaDTO> items = new ArrayList<>();
    
    @Builder.Default
    private List<PagoDTO> pagos = new ArrayList<>();
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}

