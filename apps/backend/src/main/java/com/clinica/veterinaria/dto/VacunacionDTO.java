package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Vacunacion;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad {@link Vacunacion}.
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 * @see Vacunacion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacunacionDTO {

    private Long id;

    @NotNull(message = "El paciente es requerido")
    private Long pacienteId;

    @NotNull(message = "La vacuna es requerida")
    private Long vacunaId;

    @NotNull(message = "El profesional es requerido")
    private Long profesionalId;

    @NotNull(message = "La fecha de aplicación es requerida")
    private LocalDate fechaAplicacion;

    @NotNull(message = "El número de dosis es requerido")
    @Positive(message = "El número de dosis debe ser positivo")
    private Integer numeroDosis;

    private LocalDate proximaDosis;

    @Size(max = 50, message = "El lote no puede exceder 50 caracteres")
    private String lote;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;

    // Información adicional para mostrar (no se envía al crear/actualizar)
    private String pacienteNombre;
    private String vacunaNombre;
    private String profesionalNombre;
    private String especiePaciente;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convierte una entidad {@link Vacunacion} a su DTO equivalente.
     */
    public static VacunacionDTO fromEntity(Vacunacion vacunacion) {
        return fromEntity(vacunacion, false);
    }

    /**
     * Convierte una entidad {@link Vacunacion} a su DTO equivalente con opción de incluir nombres.
     */
    public static VacunacionDTO fromEntity(Vacunacion vacunacion, boolean includeNames) {
        VacunacionDTOBuilder builder = VacunacionDTO.builder()
            .id(vacunacion.getId())
            .pacienteId(vacunacion.getPaciente().getId())
            .vacunaId(vacunacion.getVacuna().getId())
            .profesionalId(vacunacion.getProfesional().getId())
            .fechaAplicacion(vacunacion.getFechaAplicacion())
            .numeroDosis(vacunacion.getNumeroDosis())
            .proximaDosis(vacunacion.getProximaDosis())
            .lote(vacunacion.getLote())
            .observaciones(vacunacion.getObservaciones())
            .createdAt(vacunacion.getCreatedAt())
            .updatedAt(vacunacion.getUpdatedAt());

        if (includeNames) {
            if (vacunacion.getPaciente() != null) {
                builder.pacienteNombre(vacunacion.getPaciente().getNombre());
                builder.especiePaciente(vacunacion.getPaciente().getEspecie());
            }
            if (vacunacion.getVacuna() != null) {
                builder.vacunaNombre(vacunacion.getVacuna().getNombre());
            }
            if (vacunacion.getProfesional() != null) {
                builder.profesionalNombre(vacunacion.getProfesional().getNombre());
            }
        }

        return builder.build();
    }
}

