package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Propietario - Representa a los dueños de las mascotas
 */
@Entity
@Table(name = "propietarios", indexes = {
    @Index(name = "idx_propietario_documento", columnList = "documento"),
    @Index(name = "idx_propietario_email", columnList = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Propietario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 20, message = "El documento no puede exceder 20 caracteres")
    @Column(length = 20)
    private String documento;

    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(length = 100)
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(length = 20)
    private String telefono;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    @Column(length = 200)
    private String direccion;

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
     * Relación uno a muchos con Pacientes
     * Un propietario puede tener múltiples mascotas
     */
    @OneToMany(mappedBy = "propietario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude // Evitar recursión infinita en logs
    @EqualsAndHashCode.Exclude // Evitar problemas con equals/hashCode
    private List<Paciente> pacientes = new ArrayList<>();

    /**
     * Hook antes de persistir
     */
    @PrePersist
    protected void onCreate() {
        if (activo == null) {
            activo = true;
        }
    }
}

