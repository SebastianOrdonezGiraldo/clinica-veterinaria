package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un medicamento individual dentro de una prescripción médica.
 * 
 * <p>Esta entidad modela cada medicamento prescrito en una receta, incluyendo información
 * detallada sobre nombre, presentación, dosis, frecuencia, duración y vía de administración.</p>
 * 
 * <p><strong>Información gestionada:</strong></p>
 * <ul>
 *   <li><b>Medicamento:</b> Nombre del fármaco prescrito</li>
 *   <li><b>Presentación:</b> Forma farmacéutica (tabletas, jarabe, inyección, etc.)</li>
 *   <li><b>Dosis:</b> Cantidad a administrar (ej: "10mg", "1 tableta")</li>
 *   <li><b>Frecuencia:</b> Cada cuánto se administra (ej: "Cada 8 horas", "2 veces al día")</li>
 *   <li><b>Duración:</b> Número de días de tratamiento</li>
 *   <li><b>Vía de administración:</b> Oral, inyectable, tópica, oftálmica, ótica, otra</li>
 *   <li><b>Indicaciones:</b> Instrucciones específicas para este medicamento</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li><b>Prescripción:</b> Muchos a uno - Cada item pertenece a una prescripción</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Prescripción ID - Para búsquedas de items por prescripción</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Medicamento: Requerido, máximo 200 caracteres</li>
 *   <li>Dosis: Requerida, máximo 100 caracteres</li>
 *   <li>Frecuencia: Requerida, máximo 100 caracteres</li>
 *   <li>Duración: Si se proporciona, debe ser positiva</li>
 *   <li>Prescripción: Requerida</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Prescripcion
 * @see ViaAdministracion
 */
@Entity
@Table(name = "items_prescripcion", indexes = {
    @Index(name = "idx_item_prescripcion", columnList = "prescripcion_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemPrescripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del medicamento es requerido")
    @Size(max = 200, message = "El nombre del medicamento no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String medicamento;

    @Size(max = 100, message = "La presentación no puede exceder 100 caracteres")
    @Column(length = 100)
    private String presentacion;

    @NotBlank(message = "La dosis es requerida")
    @Size(max = 100, message = "La dosis no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String dosis;

    @NotBlank(message = "La frecuencia es requerida")
    @Size(max = 100, message = "La frecuencia no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String frecuencia;

    @Positive(message = "La duración debe ser positiva")
    @Column(name = "duracion_dias")
    private Integer duracionDias;

    @Enumerated(EnumType.STRING)
    @Column(name = "via_administracion", length = 20)
    private ViaAdministracion viaAdministracion;

    @Column(length = 500)
    private String indicaciones;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Relación muchos a uno con Prescripcion
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescripcion_id", nullable = false)
    @NotNull(message = "La prescripción es requerida")
    @ToString.Exclude
    private Prescripcion prescripcion;

    /**
     * Enum que define las vías de administración disponibles para medicamentos veterinarios.
     * 
     * <p>Las vías de administración determinan cómo se debe aplicar el medicamento al paciente:</p>
     * <ul>
     *   <li><b>ORAL:</b> Por vía oral (tabletas, cápsulas, jarabes, pastas)</li>
     *   <li><b>INYECTABLE:</b> Por inyección (subcutánea, intramuscular, intravenosa)</li>
     *   <li><b>TOPICA:</b> Aplicación tópica sobre la piel (cremas, pomadas, sprays)</li>
     *   <li><b>OFTALMICA:</b> Aplicación en los ojos (gotas, pomadas oftálmicas)</li>
     *   <li><b>OTICA:</b> Aplicación en los oídos (gotas óticas)</li>
     *   <li><b>OTRA:</b> Otras vías no especificadas anteriormente</li>
     * </ul>
     */
    public enum ViaAdministracion {
        /** Administración por vía oral */
        ORAL,
        
        /** Administración por inyección */
        INYECTABLE,
        
        /** Aplicación tópica sobre la piel */
        TOPICA,
        
        /** Aplicación en los ojos */
        OFTALMICA,
        
        /** Aplicación en los oídos */
        OTICA,
        
        /** Otras vías de administración */
        OTRA
    }
}

