package com.clinica.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para TemplatePrescripcion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePrescripcionDTO {
    private Long id;
    
    @NotBlank(message = "El nombre del template es requerido")
    private String nombre;
    
    private String descripcion;
    private String categoria;
    private String indicacionesGenerales;
    private Boolean activo;
    private Long usuarioId;
    private String usuarioNombre;
    private Integer vecesUsado;
    
    @Valid
    @Builder.Default
    private List<TemplatePrescripcionItemDTO> items = new ArrayList<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

