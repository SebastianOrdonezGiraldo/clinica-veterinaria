package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Prescripcion;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para la entidad Prescripcion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescripcionDTO {

    private Long id;

    @NotNull(message = "La fecha de emisión es requerida")
    private LocalDateTime fechaEmision;

    private String indicacionesGenerales;

    @NotNull(message = "La consulta es requerida")
    private Long consultaId;

    // Lista de items (medicamentos)
    private List<ItemPrescripcionDTO> items;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Constructor desde entidad (sin items)
     */
    public static PrescripcionDTO fromEntity(Prescripcion prescripcion) {
        return fromEntity(prescripcion, false);
    }

    /**
     * Constructor desde entidad (con opción de cargar items)
     */
    public static PrescripcionDTO fromEntity(Prescripcion prescripcion, boolean includeItems) {
        PrescripcionDTOBuilder builder = PrescripcionDTO.builder()
            .id(prescripcion.getId())
            .fechaEmision(prescripcion.getFechaEmision())
            .indicacionesGenerales(prescripcion.getIndicacionesGenerales())
            .consultaId(prescripcion.getConsulta().getId())
            .createdAt(prescripcion.getCreatedAt())
            .updatedAt(prescripcion.getUpdatedAt());

        if (includeItems && prescripcion.getItems() != null) {
            builder.items(
                prescripcion.getItems().stream()
                    .map(ItemPrescripcionDTO::fromEntity)
                    .collect(Collectors.toList())
            );
        }

        return builder.build();
    }
}

