package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un template (plantilla) reutilizable para prescripciones médicas.
 * 
 * <p>Los templates permiten guardar estructuras comunes de prescripciones con medicamentos
 * predefinidos para agilizar el proceso de emisión de recetas.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-30
 */
@Entity
@Table(name = "templates_prescripcion", indexes = {
    @Index(name = "idx_template_prescripcion_categoria", columnList = "categoria"),
    @Index(name = "idx_template_prescripcion_activo", columnList = "activo")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplatePrescripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del template es requerido")
    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 100)
    private String categoria; // Ej: "Antibióticos", "Analgésicos", "Post-operatorio", "Control"

    @Column(name = "indicaciones_generales", columnDefinition = "TEXT")
    private String indicacionesGenerales;

    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Usuario que creó el template
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    private Usuario creadoPor;

    /**
     * Items de medicamentos del template
     */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TemplatePrescripcionItem> items = new ArrayList<>();

    /**
     * Contador de veces que se ha usado el template
     */
    @Builder.Default
    @Column(name = "veces_usado", nullable = false)
    private Integer vecesUsado = 0;
}

