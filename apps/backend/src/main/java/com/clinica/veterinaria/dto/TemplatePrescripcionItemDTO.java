package com.clinica.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para TemplatePrescripcionItem
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePrescripcionItemDTO {
    private Long id;
    
    @NotBlank(message = "El nombre del medicamento es requerido")
    private String medicamento;
    
    private String presentacion;
    private String dosis;
    private String frecuencia;
    private String duracion;
    private String indicaciones;
    private Integer orden;
    private LocalDateTime createdAt;
}

