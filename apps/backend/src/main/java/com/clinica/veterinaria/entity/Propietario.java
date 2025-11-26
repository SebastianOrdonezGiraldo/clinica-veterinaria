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
 * Entidad JPA que representa a los propietarios de mascotas (clientes de la clínica).
 * 
 * <p>Esta entidad modela la información de contacto y datos personales de los dueños
 * de las mascotas. Un propietario puede tener múltiples mascotas asociadas.</p>
 * 
 * <p><strong>Información gestionada:</strong></p>
 * <ul>
 *   <li><b>Datos personales:</b> Nombre completo</li>
 *   <li><b>Identificación:</b> Documento de identidad (cédula, pasaporte) - único</li>
 *   <li><b>Contacto:</b> Email, teléfono, dirección</li>
 *   <li><b>Estado:</b> Activo/Inactivo (soft delete)</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li><b>Pacientes:</b> Uno a muchos - Un propietario puede tener múltiples mascotas</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Documento - Para búsquedas y validación de unicidad</li>
 *   <li>Email - Para búsquedas y comunicación</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 100 caracteres</li>
 *   <li>Documento: Opcional, máximo 20 caracteres, único si se proporciona</li>
 *   <li>Email: Si se proporciona, debe ser válido, máximo 100 caracteres</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Paciente
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

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String email;

    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(nullable = true)
    @ToString.Exclude // No mostrar en logs por seguridad
    private String password;

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

