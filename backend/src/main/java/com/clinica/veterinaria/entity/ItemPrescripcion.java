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
 * Entidad ItemPrescripcion - Representa un medicamento en una prescripción
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
     * Enum para vías de administración
     */
    public enum ViaAdministracion {
        ORAL,
        INYECTABLE,
        TOPICA,
        OFTALMICA,
        OTICA,
        OTRA
    }
}

