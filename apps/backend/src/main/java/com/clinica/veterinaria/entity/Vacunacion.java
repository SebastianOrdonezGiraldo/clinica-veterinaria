package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa el registro de una vacunación aplicada a un paciente.
 * 
 * <p>Esta entidad registra cada aplicación de vacuna a un paciente, incluyendo
 * la fecha de aplicación, el número de dosis, el profesional que la aplicó
 * y la fecha de próxima dosis si aplica.</p>
 * 
 * <p><strong>Información gestionada:</strong></p>
 * <ul>
 *   <li><b>Relaciones:</b> Paciente, Vacuna, Profesional</li>
 *   <li><b>Fechas:</b> Fecha de aplicación, próxima dosis</li>
 *   <li><b>Dosis:</b> Número de dosis aplicada</li>
 *   <li><b>Información adicional:</b> Lote, observaciones</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li><b>Paciente:</b> Muchos a uno - Cada vacunación pertenece a un paciente</li>
 *   <li><b>Vacuna:</b> Muchos a uno - Tipo de vacuna aplicada</li>
 *   <li><b>Profesional:</b> Muchos a uno - Veterinario que aplicó la vacuna</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 * @see Paciente
 * @see Vacuna
 * @see Usuario
 */
@Entity
@Table(name = "vacunaciones", indexes = {
    @Index(name = "idx_vacunacion_paciente", columnList = "paciente_id"),
    @Index(name = "idx_vacunacion_vacuna", columnList = "vacuna_id"),
    @Index(name = "idx_vacunacion_fecha", columnList = "fecha_aplicacion"),
    @Index(name = "idx_vacunacion_proxima", columnList = "proxima_dosis")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vacunacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El paciente es requerido")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    @ToString.Exclude
    private Paciente paciente;

    @NotNull(message = "La vacuna es requerida")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacuna_id", nullable = false)
    @ToString.Exclude
    private Vacuna vacuna;

    @NotNull(message = "El profesional es requerido")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    @ToString.Exclude
    private Usuario profesional;

    @NotNull(message = "La fecha de aplicación es requerida")
    @Column(name = "fecha_aplicacion", nullable = false)
    private LocalDate fechaAplicacion;

    @NotNull(message = "El número de dosis es requerido")
    @Positive(message = "El número de dosis debe ser positivo")
    @Column(name = "numero_dosis", nullable = false)
    private Integer numeroDosis; // 1, 2, 3, etc.

    @Column(name = "proxima_dosis")
    private LocalDate proximaDosis; // Fecha de la próxima dosis si aplica

    @Size(max = 50, message = "El lote no puede exceder 50 caracteres")
    @Column(length = 50)
    private String lote;

    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

