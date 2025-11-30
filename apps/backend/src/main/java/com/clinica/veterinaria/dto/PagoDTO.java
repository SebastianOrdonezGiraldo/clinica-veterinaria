package com.clinica.veterinaria.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para Pago
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagoDTO {
    private Long id;
    
    @NotNull(message = "El monto es requerido")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal monto;
    
    @NotNull(message = "La fecha de pago es requerida")
    private LocalDateTime fechaPago;
    
    private String metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA, CHEQUE
    
    private String referencia;
    
    private String observaciones;
    
    private Long facturaId;
    
    private Long usuarioId;
    
    private String usuarioNombre;
    
    private LocalDateTime createdAt;
}

