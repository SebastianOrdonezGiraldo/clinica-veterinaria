package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una consulta médica veterinaria (registro de atención).
 * 
 * <p>Esta entidad modela el registro completo de cada atención médica realizada a un paciente,
 * incluyendo signos vitales, examen físico, diagnóstico y tratamiento prescrito. Forma parte
 * de la historia clínica del paciente.</p>
 * 
 * <p><strong>Datos registrados:</strong></p>
 * <ul>
 *   <li><b>Signos Vitales:</b> Frecuencia cardíaca (lpm), frecuencia respiratoria (rpm),
 *       temperatura (°C), peso (kg)</li>
 *   <li><b>Examen Físico:</b> Observaciones detalladas del estado del paciente</li>
 *   <li><b>Diagnóstico:</b> Conclusión médica basada en la evaluación</li>
 *   <li><b>Tratamiento:</b> Plan terapéutico prescrito (medicamentos, cuidados, seguimiento)</li>
 *   <li><b>Observaciones:</b> Notas adicionales y recomendaciones</li>
 * </ul>
 * 
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li><b>Relaciones:</b> Conecta con paciente y profesional veterinario</li>
 *   <li><b>Índices:</b> Optimizado para búsquedas por fecha, paciente y profesional</li>
 *   <li><b>Historial:</b> Forma parte del historial médico permanente del paciente</li>
 *   <li><b>Prescripciones:</b> Puede tener múltiples prescripciones asociadas</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Fecha - Para búsquedas cronológicas</li>
 *   <li>Paciente ID - Para historia clínica completa</li>
 *   <li>Profesional ID - Para consultas por veterinario</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Fecha: Requerida</li>
 *   <li>Paciente: Requerido</li>
 *   <li>Profesional: Requerido</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Paciente
 * @see Usuario
 * @see Prescripcion
 */
@Entity
@Table(name = "consultas", indexes = {
    @Index(name = "idx_consulta_fecha", columnList = "fecha"),
    @Index(name = "idx_consulta_paciente", columnList = "paciente_id"),
    @Index(name = "idx_consulta_profesional", columnList = "profesional_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha es requerida")
    @Column(nullable = false)
    private LocalDateTime fecha;

    // Signos vitales
    @Column(name = "frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @Column(name = "frecuencia_respiratoria")
    private Integer frecuenciaRespiratoria;

    @Column(precision = 4, scale = 1)
    private BigDecimal temperatura;

    @Column(name = "peso_kg", precision = 5, scale = 2)
    private BigDecimal pesoKg;

    // Consulta
    @Column(name = "examen_fisico", columnDefinition = "TEXT")
    private String examenFisico;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String tratamiento;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relación muchos a uno con Paciente
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    @NotNull(message = "El paciente es requerido")
    @ToString.Exclude
    private Paciente paciente;

    /**
     * Relación muchos a uno con Usuario (profesional que realiza la consulta)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    @NotNull(message = "El profesional es requerido")
    @ToString.Exclude
    private Usuario profesional;

    /**
     * Relación uno a muchos con Prescripciones
     */
    @OneToMany(mappedBy = "consulta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Prescripcion> prescripciones = new ArrayList<>();

}

