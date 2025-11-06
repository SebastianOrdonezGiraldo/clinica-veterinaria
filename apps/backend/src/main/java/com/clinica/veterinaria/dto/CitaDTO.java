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
 * DTO para la entidad Cita
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
     * Constructor desde entidad (sin datos relacionados)
     */
    public static CitaDTO fromEntity(Cita cita) {
        return fromEntity(cita, false);
    }

    /**
     * Constructor desde entidad (con opción de cargar datos relacionados)
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

