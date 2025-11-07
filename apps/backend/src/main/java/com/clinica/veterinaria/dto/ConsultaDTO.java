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
 * Data Transfer Object (DTO) para la entidad {@link Consulta}.
 * 
 * <p>Este DTO se utiliza para transferir datos de consultas médicas (historia clínica)
 * entre el cliente y el servidor. Incluye signos vitales, examen físico, diagnóstico
 * y tratamiento, además de información de relaciones.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Signos Vitales:</b> Frecuencia cardíaca (lpm), frecuencia respiratoria (rpm),
 *       temperatura (°C), peso (kg)</li>
 *   <li><b>Datos Clínicos:</b> Examen físico, diagnóstico, tratamiento, observaciones</li>
 *   <li><b>Relaciones (IDs):</b> pacienteId, profesionalId</li>
 *   <li><b>Información adicional (opcional):</b> Nombres de paciente y profesional,
 *       IDs de prescripciones asociadas</li>
 *   <li><b>Auditoría:</b> createdAt, updatedAt</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Fecha: Requerida</li>
 *   <li>Paciente: ID requerido</li>
 *   <li>Profesional: ID requerido</li>
 * </ul>
 * 
 * <p><strong>Uso típico:</strong></p>
 * <pre>
 * // Crear nueva consulta
 * ConsultaDTO dto = ConsultaDTO.builder()
 *     .fecha(LocalDateTime.now())
 *     .frecuenciaCardiaca(120)
 *     .temperatura(new BigDecimal("38.5"))
 *     .pesoKg(new BigDecimal("15.5"))
 *     .diagnostico("Resfriado común")
 *     .pacienteId(123L)
 *     .profesionalId(789L)
 *     .build();
 * </pre>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Consulta
 * @see ConsultaService
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
     * Convierte una entidad {@link Consulta} a su DTO equivalente.
     * 
     * <p>Versión simplificada que no incluye nombres de entidades relacionadas ni
     * IDs de prescripciones. Útil cuando solo se necesitan los datos básicos.</p>
     * 
     * @param consulta Entidad Consulta a convertir. No puede ser null.
     * @return DTO con los datos de la consulta.
     * @see #fromEntity(Consulta, boolean)
     */
    public static ConsultaDTO fromEntity(Consulta consulta) {
        return fromEntity(consulta, false);
    }

    /**
     * Convierte una entidad {@link Consulta} a su DTO equivalente con opción de incluir
     * información adicional de entidades relacionadas.
     * 
     * <p>Cuando {@code includeRelated} es true, se incluyen:</p>
     * <ul>
     *   <li>Nombres de paciente y profesional</li>
     *   <li>Lista de IDs de prescripciones asociadas</li>
     * </ul>
     * 
     * <p>Útil para mostrar información completa en el frontend sin consultas adicionales.</p>
     * 
     * @param consulta Entidad Consulta a convertir. No puede ser null.
     * @param includeRelated Si es true, incluye nombres relacionados y prescripciones.
     * @return DTO con los datos de la consulta.
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

