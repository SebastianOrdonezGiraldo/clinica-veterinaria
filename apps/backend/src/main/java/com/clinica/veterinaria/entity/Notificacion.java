package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa las notificaciones del sistema.
 * 
 * <p>Esta entidad modela las notificaciones que se envían a los usuarios del sistema,
 * como recordatorios de citas, alertas de pacientes, mensajes del sistema, etc.</p>
 * 
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li><b>Tipo:</b> Diferentes tipos de notificaciones (CITA, CONSULTA, SISTEMA, etc.)</li>
 *   <li><b>Estado:</b> Leída o no leída</li>
 *   <li><b>Usuario:</b> Relación con el usuario destinatario</li>
 *   <li><b>Auditoría:</b> Timestamp automático de creación</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-09
 */
@Entity
@Table(name = "notificaciones", indexes = {
    @Index(name = "idx_notificacion_usuario", columnList = "usuario_id"),
    @Index(name = "idx_notificacion_leida", columnList = "leida"),
    @Index(name = "idx_notificacion_fecha", columnList = "fecha_creacion")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "El usuario es requerido")
    private Usuario usuario;

    @NotBlank(message = "El título es requerido")
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotBlank(message = "El mensaje es requerido")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull(message = "El tipo es requerido")
    private Tipo tipo;

    @Builder.Default
    @Column(nullable = false)
    private Boolean leida = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    // Campos opcionales para enlaces a entidades relacionadas
    @Column(name = "entidad_tipo", length = 50)
    private String entidadTipo; // Ej: "CITA", "CONSULTA", "PACIENTE"

    @Column(name = "entidad_id")
    private Long entidadId; // ID de la entidad relacionada

    /**
     * Tipos de notificaciones disponibles
     */
    public enum Tipo {
        CITA,           // Notificaciones relacionadas con citas
        CONSULTA,       // Notificaciones relacionadas con consultas
        PACIENTE,       // Notificaciones relacionadas con pacientes
        SISTEMA,        // Notificaciones del sistema
        PRESCRIPCION,   // Notificaciones relacionadas con prescripciones
        RECORDATORIO    // Recordatorios generales
    }

    @Override
    public String toString() {
        return "Notificacion{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", tipo=" + tipo +
                ", leida=" + leida +
                ", fechaCreacion=" + fechaCreacion +
                '}';
    }
}

