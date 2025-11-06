package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Consulta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO para la entidad Consulta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaDTO {

    private Long id;

    @NotNull(message = "La fecha es requerida")
    private LocalDateTime fecha;

    // Signos vitales
    private Integer frecuenciaCardiaca;
    private Integer frecuenciaRespiratoria;
    private BigDecimal temperatura;
    private BigDecimal pesoKg;

    // Datos de consulta
    private String examenFisico;
    private String diagnostico;
    private String tratamiento;
    private String observaciones;

    @NotNull(message = "El paciente es requerido")
    private Long pacienteId;

    @NotNull(message = "El profesional es requerido")
    private Long profesionalId;

    // Información adicional para mostrar
    private String pacienteNombre;
    private String profesionalNombre;

    // Lista de prescripciones (opcional)
    private List<Long> prescripcionesIds;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Constructor desde entidad (sin datos relacionados)
     */
    public static ConsultaDTO fromEntity(Consulta consulta) {
        return fromEntity(consulta, false);
    }

    /**
     * Constructor desde entidad (con opción de cargar datos relacionados)
     */
    public static ConsultaDTO fromEntity(Consulta consulta, boolean includeRelated) {
        ConsultaDTOBuilder builder = ConsultaDTO.builder()
            .id(consulta.getId())
            .fecha(consulta.getFecha())
            .frecuenciaCardiaca(consulta.getFrecuenciaCardiaca())
            .frecuenciaRespiratoria(consulta.getFrecuenciaRespiratoria())
            .temperatura(consulta.getTemperatura())
            .pesoKg(consulta.getPesoKg())
            .examenFisico(consulta.getExamenFisico())
            .diagnostico(consulta.getDiagnostico())
            .tratamiento(consulta.getTratamiento())
            .observaciones(consulta.getObservaciones())
            .pacienteId(consulta.getPaciente().getId())
            .profesionalId(consulta.getProfesional().getId())
            .createdAt(consulta.getCreatedAt())
            .updatedAt(consulta.getUpdatedAt());

        if (includeRelated) {
            if (consulta.getPaciente() != null) {
                builder.pacienteNombre(consulta.getPaciente().getNombre());
            }
            if (consulta.getProfesional() != null) {
                builder.profesionalNombre(consulta.getProfesional().getNombre());
            }
            if (consulta.getPrescripciones() != null) {
                builder.prescripcionesIds(
                    consulta.getPrescripciones().stream()
                        .map(p -> p.getId())
                        .collect(Collectors.toList())
                );
            }
        }

        return builder.build();
    }
}

