package com.clinica.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para TemplateConsulta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateConsultaDTO {
    private Long id;
    
    @NotBlank(message = "El nombre del template es requerido")
    private String nombre;
    
    private String descripcion;
    private String categoria;
    private String examenFisico;
    private String diagnostico;
    private String tratamiento;
    private String observaciones;
    private Boolean activo;
    private Long usuarioId;
    private String usuarioNombre;
    private Integer vecesUsado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

