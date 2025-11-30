package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Vacuna;
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
 * Data Transfer Object (DTO) para la entidad {@link Vacuna}.
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 * @see Vacuna
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacunaDTO {

    private Long id;

    @NotBlank(message = "El nombre de la vacuna es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 50, message = "La especie no puede exceder 50 caracteres")
    private String especie;

    @NotNull(message = "El número de dosis es requerido")
    @Positive(message = "El número de dosis debe ser positivo")
    private Integer numeroDosis;

    @Positive(message = "El intervalo entre dosis debe ser positivo")
    private Integer intervaloDias;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @Size(max = 100, message = "El fabricante no puede exceder 100 caracteres")
    private String fabricante;

    private Boolean activo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convierte una entidad {@link Vacuna} a su DTO equivalente.
     */
    public static VacunaDTO fromEntity(Vacuna vacuna) {
        return VacunaDTO.builder()
            .id(vacuna.getId())
            .nombre(vacuna.getNombre())
            .especie(vacuna.getEspecie())
            .numeroDosis(vacuna.getNumeroDosis())
            .intervaloDias(vacuna.getIntervaloDias())
            .descripcion(vacuna.getDescripcion())
            .fabricante(vacuna.getFabricante())
            .activo(vacuna.getActivo())
            .createdAt(vacuna.getCreatedAt())
            .updatedAt(vacuna.getUpdatedAt())
            .build();
    }
}

