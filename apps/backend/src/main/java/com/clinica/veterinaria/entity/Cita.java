package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad Cita - Representa las citas médicas agendadas
 */
@Entity
@Table(name = "citas", indexes = {
    @Index(name = "idx_cita_fecha", columnList = "fecha"),
    @Index(name = "idx_cita_paciente", columnList = "paciente_id"),
    @Index(name = "idx_cita_profesional", columnList = "profesional_id"),
    @Index(name = "idx_cita_estado", columnList = "estado")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La fecha es requerida")
    @Column(nullable = false)
    private LocalDateTime fecha;

    @NotBlank(message = "El motivo es requerido")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    @Column(nullable = false, length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private EstadoCita estado = EstadoCita.PENDIENTE;

    @Column(length = 1000)
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
     * Relación muchos a uno con Propietario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    @NotNull(message = "El propietario es requerido")
    @ToString.Exclude
    private Propietario propietario;

    /**
     * Relación muchos a uno con Usuario (profesional que atiende)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    @NotNull(message = "El profesional es requerido")
    @ToString.Exclude
    private Usuario profesional;

    /**
     * Enum para estados de la cita
     */
    public enum EstadoCita {
        PENDIENTE,
        CONFIRMADA,
        ATENDIDA,
        CANCELADA
    }

    /**
     * Hook antes de persistir
     */
    @PrePersist
    protected void onCreate() {
        if (estado == null) {
            estado = EstadoCita.PENDIENTE;
        }
    }
}

