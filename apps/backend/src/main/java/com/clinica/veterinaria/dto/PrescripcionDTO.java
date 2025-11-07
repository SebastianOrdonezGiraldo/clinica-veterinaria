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
 * Data Transfer Object (DTO) para la entidad {@link Prescripcion}.
 * 
 * <p>Este DTO se utiliza para transferir datos de prescripciones médicas (recetas veterinarias)
 * entre el cliente y el servidor. Incluye la fecha de emisión, indicaciones generales y
 * una lista opcional de medicamentos prescritos (items).</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Datos básicos:</b> ID, fecha de emisión, indicaciones generales</li>
 *   <li><b>Relación:</b> consultaId (requerido) - ID de la consulta asociada</li>
 *   <li><b>Items (opcional):</b> Lista de medicamentos prescritos ({@link ItemPrescripcionDTO})</li>
 *   <li><b>Auditoría:</b> createdAt, updatedAt</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Fecha de emisión: Requerida</li>
 *   <li>Consulta: ID requerido</li>
 * </ul>
 * 
 * <p><strong>Uso típico:</strong></p>
 * <pre>
 * // Crear prescripción con items
 * PrescripcionDTO dto = PrescripcionDTO.builder()
 *     .fechaEmision(LocalDateTime.now())
 *     .consultaId(123L)
 *     .indicacionesGenerales("Administrar con comida")
 *     .items(listaDeMedicamentos)
 *     .build();
 * </pre>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Prescripcion
 * @see ItemPrescripcionDTO
 * @see ConsultaDTO
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
     * Convierte una entidad {@link Prescripcion} a su DTO equivalente.
     * 
     * <p>Versión simplificada que no incluye los items (medicamentos) de la prescripción.
     * Útil cuando solo se necesita la información básica de la receta.</p>
     * 
     * @param prescripcion Entidad Prescripcion a convertir. No puede ser null.
     * @return DTO con los datos de la prescripción sin items.
     * @see #fromEntity(Prescripcion, boolean)
     */
    public static PrescripcionDTO fromEntity(Prescripcion prescripcion) {
        return fromEntity(prescripcion, false);
    }

    /**
     * Convierte una entidad {@link Prescripcion} a su DTO equivalente con opción de incluir
     * los items (medicamentos) de la prescripción.
     * 
     * <p>Cuando {@code includeItems} es true, se incluyen todos los medicamentos prescritos
     * con sus detalles completos (dosis, frecuencia, vía de administración, etc.).</p>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * // Sin items (más eficiente)
     * PrescripcionDTO dto = PrescripcionDTO.fromEntity(prescripcion, false);
     * // dto.getItems() = null
     * 
     * // Con items (completo)
     * PrescripcionDTO dto = PrescripcionDTO.fromEntity(prescripcion, true);
     * // dto.getItems() = [ItemPrescripcionDTO, ItemPrescripcionDTO, ...]
     * </pre>
     * 
     * @param prescripcion Entidad Prescripcion a convertir. No puede ser null.
     * @param includeItems Si es true, incluye la lista completa de medicamentos prescritos.
     * @return DTO con los datos de la prescripción.
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

