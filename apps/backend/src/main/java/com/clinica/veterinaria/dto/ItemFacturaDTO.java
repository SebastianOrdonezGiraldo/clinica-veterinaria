package com.clinica.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para ItemFactura
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemFacturaDTO {
    private Long id;
    
    @NotBlank(message = "La descripción es requerida")
    private String descripcion;
    
    private String tipoItem; // SERVICIO, MEDICAMENTO, PROCEDIMIENTO, OTRO
    
    private String codigoProducto;
    
    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser positiva")
    private BigDecimal cantidad;
    
    @NotNull(message = "El precio unitario es requerido")
    @Positive(message = "El precio unitario debe ser positivo")
    private BigDecimal precioUnitario;
    
    private BigDecimal descuento;
    
    private BigDecimal subtotal;
    
    private Integer orden;
}

