package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.ItemPrescripcion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para la entidad ItemPrescripcion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPrescripcionDTO {

    private Long id;

    @NotBlank(message = "El nombre del medicamento es requerido")
    @Size(max = 200, message = "El nombre del medicamento no puede exceder 200 caracteres")
    private String medicamento;

    @Size(max = 100, message = "La presentación no puede exceder 100 caracteres")
    private String presentacion;

    @NotBlank(message = "La dosis es requerida")
    @Size(max = 100, message = "La dosis no puede exceder 100 caracteres")
    private String dosis;

    @NotBlank(message = "La frecuencia es requerida")
    @Size(max = 100, message = "La frecuencia no puede exceder 100 caracteres")
    private String frecuencia;

    @Positive(message = "La duración debe ser positiva")
    private Integer duracionDias;

    private ItemPrescripcion.ViaAdministracion viaAdministracion;

    private String indicaciones;

    @NotNull(message = "La prescripción es requerida")
    private Long prescripcionId;

    private LocalDateTime createdAt;

    /**
     * Constructor desde entidad
     */
    public static ItemPrescripcionDTO fromEntity(ItemPrescripcion item) {
        return ItemPrescripcionDTO.builder()
            .id(item.getId())
            .medicamento(item.getMedicamento())
            .presentacion(item.getPresentacion())
            .dosis(item.getDosis())
            .frecuencia(item.getFrecuencia())
            .duracionDias(item.getDuracionDias())
            .viaAdministracion(item.getViaAdministracion())
            .indicaciones(item.getIndicaciones())
            .prescripcionId(item.getPrescripcion().getId())
            .createdAt(item.getCreatedAt())
            .build();
    }
}

