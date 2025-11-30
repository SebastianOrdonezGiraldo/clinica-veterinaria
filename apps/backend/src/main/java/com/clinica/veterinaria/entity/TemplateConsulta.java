package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa un template (plantilla) reutilizable para consultas médicas.
 * 
 * <p>Los templates permiten guardar estructuras comunes de consultas para agilizar
 * el proceso de registro de atenciones médicas.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-30
 */
@Entity
@Table(name = "templates_consulta", indexes = {
    @Index(name = "idx_template_consulta_categoria", columnList = "categoria"),
    @Index(name = "idx_template_consulta_activo", columnList = "activo")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateConsulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del template es requerido")
    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 100)
    private String categoria; // Ej: "General", "Cirugía", "Emergencia", "Control"

    @Column(name = "examen_fisico", columnDefinition = "TEXT")
    private String examenFisico;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String tratamiento;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

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
     * Contador de veces que se ha usado el template
     */
    @Builder.Default
    @Column(name = "veces_usado", nullable = false)
    private Integer vecesUsado = 0;
}

