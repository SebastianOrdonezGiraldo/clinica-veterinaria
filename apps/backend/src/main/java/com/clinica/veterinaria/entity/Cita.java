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
 * Entidad JPA que representa las citas médicas agendadas en la clínica veterinaria.
 * 
 * <p>Esta entidad modela el ciclo de vida completo de una cita médica, desde su
 * programación inicial hasta su finalización o cancelación. Incluye relaciones
 * con paciente, propietario y profesional veterinario.</p>
 * 
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li><b>Estados:</b> Sistema de estados para rastrear el ciclo de vida (PENDIENTE → CONFIRMADA → EN_PROCESO → COMPLETADA/CANCELADA)</li>
 *   <li><b>Relaciones:</b> Conecta paciente, propietario y veterinario</li>
 *   <li><b>Índices:</b> Optimizado para búsquedas por fecha, paciente, profesional y estado</li>
 *   <li><b>Auditoría:</b> Timestamps automáticos de creación y actualización</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Fecha - Para búsquedas por rango de fechas</li>
 *   <li>Paciente ID - Para historial de citas por mascota</li>
 *   <li>Profesional ID - Para agenda del veterinario</li>
 *   <li>Estado - Para filtros por estado</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Fecha: Requerida</li>
 *   <li>Motivo: Requerido, máximo 500 caracteres</li>
 *   <li>Estado: Por defecto PENDIENTE si no se especifica</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see EstadoCita
 * @see Paciente
 * @see Propietario
 * @see Usuario
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
     * Enum que define los posibles estados de una cita médica durante su ciclo de vida.
     * 
     * <p>Los estados representan las diferentes fases por las que pasa una cita:</p>
     * <ul>
     *   <li><b>PENDIENTE:</b> Cita programada, esperando confirmación del propietario o inicio de atención</li>
     *   <li><b>CONFIRMADA:</b> Cita confirmada por el propietario, lista para ser atendida</li>
     *   <li><b>ATENDIDA:</b> Cita completada exitosamente, atención médica finalizada</li>
     *   <li><b>CANCELADA:</b> Cita cancelada por cualquier motivo (propietario, clínica, etc.)</li>
     * </ul>
     * 
     * <p><strong>Transiciones típicas:</strong></p>
     * <ul>
     *   <li>PENDIENTE → CONFIRMADA (cuando el propietario confirma)</li>
     *   <li>CONFIRMADA → ATENDIDA (cuando se completa la atención)</li>
     *   <li>Cualquier estado → CANCELADA (si se cancela)</li>
     * </ul>
     */
    public enum EstadoCita {
        /** Cita programada, esperando confirmación */
        PENDIENTE,
        
        /** Cita confirmada, lista para atención */
        CONFIRMADA,
        
        /** Cita completada, atención finalizada */
        ATENDIDA,
        
        /** Cita cancelada */
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

