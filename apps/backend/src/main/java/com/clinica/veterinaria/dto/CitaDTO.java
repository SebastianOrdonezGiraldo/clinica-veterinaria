package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Cita;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad {@link Cita}.
 * 
 * <p>Este DTO se utiliza para transferir datos de citas médicas entre el cliente
 * y el servidor, evitando exponer directamente las entidades JPA. Incluye validaciones
 * de entrada y métodos de conversión desde entidades.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Datos básicos:</b> ID, fecha, motivo, estado, observaciones</li>
 *   <li><b>Relaciones (IDs):</b> pacienteId, propietarioId, profesionalId</li>
 *   <li><b>Información adicional (opcional):</b> Nombres de paciente, propietario y profesional</li>
 *   <li><b>Auditoría:</b> createdAt, updatedAt</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Fecha: Requerida</li>
 *   <li>Motivo: Requerido, máximo 500 caracteres</li>
 *   <li>Paciente, Propietario, Profesional: IDs requeridos</li>
 * </ul>
 * 
 * <p><strong>Uso típico:</strong></p>
 * <pre>
 * // Crear nueva cita
 * CitaDTO dto = CitaDTO.builder()
 *     .fecha(LocalDateTime.now().plusDays(7))
 *     .motivo("Control anual")
 *     .pacienteId(123L)
 *     .propietarioId(456L)
 *     .profesionalId(789L)
 *     .build();
 * 
 * // Convertir desde entidad
 * CitaDTO dto = CitaDTO.fromEntity(cita, true); // Con nombres relacionados
 * </pre>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Cita
 * @see CitaService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaDTO {

    private Long id;

    @NotNull(message = "La fecha es requerida")
    private LocalDateTime fecha;

    @NotBlank(message = "El motivo es requerido")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;

    private Cita.EstadoCita estado;

    private String observaciones;

    @NotNull(message = "El paciente es requerido")
    private Long pacienteId;

    @NotNull(message = "El propietario es requerido")
    private Long propietarioId;

    @NotNull(message = "El profesional es requerido")
    private Long profesionalId;

    // Información adicional para mostrar
    private String pacienteNombre;
    private String propietarioNombre;
    private String profesionalNombre;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Convierte una entidad {@link Cita} a su DTO equivalente.
     * 
     * <p>Versión simplificada que no incluye nombres de entidades relacionadas.
     * Útil cuando solo se necesitan los IDs de las relaciones.</p>
     * 
     * @param cita Entidad Cita a convertir. No puede ser null.
     * @return DTO con los datos de la cita.
     * @see #fromEntity(Cita, boolean)
     */
    public static CitaDTO fromEntity(Cita cita) {
        return fromEntity(cita, false);
    }

    /**
     * Convierte una entidad {@link Cita} a su DTO equivalente con opción de incluir
     * información adicional de entidades relacionadas.
     * 
     * <p>Cuando {@code includeRelated} es true, se incluyen los nombres de paciente,
     * propietario y profesional para facilitar la visualización en el frontend sin
     * necesidad de hacer consultas adicionales.</p>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * // Sin información relacionada (más eficiente)
     * CitaDTO dto = CitaDTO.fromEntity(cita, false);
     * // dto.getPacienteNombre() = null
     * 
     * // Con información relacionada (más completo)
     * CitaDTO dto = CitaDTO.fromEntity(cita, true);
     * // dto.getPacienteNombre() = "Max"
     * // dto.getPropietarioNombre() = "Juan Pérez"
     * // dto.getProfesionalNombre() = "Dr. María García"
     * </pre>
     * 
     * @param cita Entidad Cita a convertir. No puede ser null.
     * @param includeRelated Si es true, incluye nombres de paciente, propietario y profesional.
     * @return DTO con los datos de la cita.
     */
    public static CitaDTO fromEntity(Cita cita, boolean includeRelated) {
        CitaDTOBuilder builder = CitaDTO.builder()
            .id(cita.getId())
            .fecha(cita.getFecha())
            .motivo(cita.getMotivo())
            .estado(cita.getEstado())
            .observaciones(cita.getObservaciones())
            .pacienteId(cita.getPaciente().getId())
            .propietarioId(cita.getPropietario().getId())
            .profesionalId(cita.getProfesional().getId())
            .createdAt(cita.getCreatedAt())
            .updatedAt(cita.getUpdatedAt());

        if (includeRelated) {
            if (cita.getPaciente() != null) {
                builder.pacienteNombre(cita.getPaciente().getNombre());
            }
            if (cita.getPropietario() != null) {
                builder.propietarioNombre(cita.getPropietario().getNombre());
            }
            if (cita.getProfesional() != null) {
                builder.profesionalNombre(cita.getProfesional().getNombre());
            }
        }

        return builder.build();
    }
}

