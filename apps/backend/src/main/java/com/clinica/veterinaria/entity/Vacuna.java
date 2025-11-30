package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa los tipos de vacunas disponibles en la clínica.
 * 
 * <p>Esta entidad define las vacunas que pueden ser aplicadas a los pacientes,
 * incluyendo información sobre la especie para la cual es válida, el número de
 * dosis requeridas y el intervalo entre dosis.</p>
 * 
 * <p><strong>Información gestionada:</strong></p>
 * <ul>
 *   <li><b>Identificación:</b> Nombre de la vacuna</li>
 *   <li><b>Especificaciones:</b> Especie objetivo, número de dosis, intervalo entre dosis</li>
 *   <li><b>Información adicional:</b> Descripción, fabricante</li>
 *   <li><b>Estado:</b> Activo/Inactivo</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li><b>Vacunaciones:</b> Uno a muchos - Historial de aplicaciones de esta vacuna</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 * @see Vacunacion
 */
@Entity
@Table(name = "vacunas", indexes = {
    @Index(name = "idx_vacuna_nombre", columnList = "nombre"),
    @Index(name = "idx_vacuna_especie", columnList = "especie")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vacuna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la vacuna es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 50, message = "La especie no puede exceder 50 caracteres")
    @Column(length = 50)
    private String especie; // Canino, Felino, etc. null = todas las especies

    @NotNull(message = "El número de dosis es requerido")
    @Positive(message = "El número de dosis debe ser positivo")
    @Column(name = "numero_dosis", nullable = false)
    private Integer numeroDosis;

    @Positive(message = "El intervalo entre dosis debe ser positivo")
    @Column(name = "intervalo_dias")
    private Integer intervaloDias; // Días entre cada dosis

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(length = 500)
    private String descripcion;

    @Size(max = 100, message = "El fabricante no puede exceder 100 caracteres")
    @Column(length = 100)
    private String fabricante;

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
     * Relación uno a muchos con Vacunacion
     */
    @OneToMany(mappedBy = "vacuna", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Vacunacion> vacunaciones = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (activo == null) {
            activo = true;
        }
    }
}

