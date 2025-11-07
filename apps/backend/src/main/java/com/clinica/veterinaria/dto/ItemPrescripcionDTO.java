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
 * Data Transfer Object (DTO) para la entidad {@link ItemPrescripcion}.
 * 
 * <p>Este DTO se utiliza para transferir datos de un medicamento individual dentro de
 * una prescripción médica. Cada prescripción puede contener múltiples items (medicamentos)
 * con información detallada sobre dosis, frecuencia y administración.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Medicamento:</b> Nombre del fármaco prescrito</li>
 *   <li><b>Presentación:</b> Forma farmacéutica (tabletas, jarabe, inyección, etc.)</li>
 *   <li><b>Dosis:</b> Cantidad a administrar (ej: "10mg", "1 tableta")</li>
 *   <li><b>Frecuencia:</b> Cada cuánto se administra (ej: "Cada 8 horas", "2 veces al día")</li>
 *   <li><b>Duración:</b> Número de días de tratamiento</li>
 *   <li><b>Vía de administración:</b> Oral, inyectable, tópica, oftálmica, ótica, otra</li>
 *   <li><b>Indicaciones:</b> Instrucciones específicas para este medicamento</li>
 *   <li><b>Relación:</b> prescripcionId (requerido)</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Medicamento: Requerido, máximo 200 caracteres</li>
 *   <li>Dosis: Requerida, máximo 100 caracteres</li>
 *   <li>Frecuencia: Requerida, máximo 100 caracteres</li>
 *   <li>Duración: Si se proporciona, debe ser positiva</li>
 *   <li>Prescripción: ID requerido</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see ItemPrescripcion
 * @see PrescripcionDTO
 * @see ItemPrescripcion.ViaAdministracion
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
     * Convierte una entidad {@link ItemPrescripcion} a su DTO equivalente.
     * 
     * <p>Incluye todos los campos del item de prescripción, incluyendo el ID de la
     * prescripción a la que pertenece.</p>
     * 
     * @param item Entidad ItemPrescripcion a convertir. No puede ser null.
     * @return DTO con los datos completos del medicamento prescrito.
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

