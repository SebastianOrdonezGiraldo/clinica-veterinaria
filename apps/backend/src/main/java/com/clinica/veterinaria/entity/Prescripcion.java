package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa una prescripción médica (receta médica veterinaria).
 * 
 * <p>Esta entidad modela las recetas médicas emitidas durante una consulta, incluyendo
 * indicaciones generales y una lista de medicamentos prescritos (items de prescripción).</p>
 * 
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li><b>Asociación con consulta:</b> Cada prescripción está vinculada a una consulta médica específica</li>
 *   <li><b>Items de medicamentos:</b> Contiene múltiples medicamentos con dosis, frecuencia y duración</li>
 *   <li><b>Indicaciones generales:</b> Instrucciones adicionales para el propietario</li>
 *   <li><b>Fecha de emisión:</b> Registro de cuándo fue emitida la receta</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li><b>Consulta:</b> Muchos a uno - Una prescripción pertenece a una consulta</li>
 *   <li><b>Items:</b> Uno a muchos - Una prescripción contiene múltiples medicamentos</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Consulta ID - Para búsquedas de prescripciones por consulta</li>
 *   <li>Fecha de emisión - Para búsquedas cronológicas</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Fecha de emisión: Requerida (se asigna automáticamente si no se proporciona)</li>
 *   <li>Consulta: Requerida</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Consulta
 * @see ItemPrescripcion
 */
@Entity
@Table(name = "prescripciones", indexes = {
    @Index(name = "idx_prescripcion_consulta", columnList = "consulta_id"),
    @Index(name = "idx_prescripcion_fecha", columnList = "fecha_emision")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prescripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha de emisión es requerida")
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "indicaciones_generales", columnDefinition = "TEXT")
    private String indicacionesGenerales;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relación muchos a uno con Consulta
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id", nullable = false)
    @NotNull(message = "La consulta es requerida")
    @ToString.Exclude
    private Consulta consulta;

    /**
     * Relación uno a muchos con ItemsPrescripcion (medicamentos)
     */
    @OneToMany(mappedBy = "prescripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ItemPrescripcion> items = new ArrayList<>();

    /**
     * Hook antes de persistir
     */
    @PrePersist
    protected void onCreate() {
        if (fechaEmision == null) {
            fechaEmision = LocalDateTime.now();
        }
    }
}

