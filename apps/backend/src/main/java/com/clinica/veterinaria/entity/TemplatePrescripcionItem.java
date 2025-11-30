package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un item (medicamento) dentro de un template de prescripción.
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-30
 */
@Entity
@Table(name = "templates_prescripcion_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePrescripcionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del medicamento es requerido")
    @Column(nullable = false, length = 200)
    private String medicamento;

    @Column(length = 100)
    private String presentacion; // Ej: "Tabletas", "Jarabe", "Inyección"

    @Column(length = 200)
    private String dosis; // Ej: "10 mg/kg", "1 tableta"

    @Column(length = 200)
    private String frecuencia; // Ej: "Cada 8 horas", "2 veces al día"

    @Column(length = 200)
    private String duracion; // Ej: "7 días", "Hasta terminar"

    @Column(columnDefinition = "TEXT")
    private String indicaciones; // Instrucciones específicas del medicamento

    @Column(name = "orden", nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Relación muchos a uno con TemplatePrescripcion
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    @NotNull(message = "El template es requerido")
    @ToString.Exclude
    private TemplatePrescripcion template;
}

