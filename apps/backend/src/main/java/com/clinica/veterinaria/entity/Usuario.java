package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa los usuarios del sistema con autenticación y autorización.
 * 
 * <p>Esta entidad modela a todos los usuarios que pueden acceder al sistema: veterinarios,
 * administradores, recepcionistas y estudiantes. Incluye información de autenticación
 * (email/password) y autorización (rol).</p>
 * 
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li><b>Autenticación:</b> Email único y contraseña hasheada (BCrypt)</li>
 *   <li><b>Autorización:</b> Sistema de roles para control de acceso</li>
 *   <li><b>Soft Delete:</b> Campo activo para desactivar sin eliminar</li>
 *   <li><b>Auditoría:</b> Timestamps automáticos de creación y actualización</li>
 *   <li><b>Seguridad:</b> Password excluido de toString() para evitar logs inseguros</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Email (único) - Para búsquedas rápidas de autenticación</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 100 caracteres</li>
 *   <li>Email: Requerido, formato válido, único, máximo 100 caracteres</li>
 *   <li>Password: Requerido, mínimo 6 caracteres (se hashea antes de almacenar)</li>
 *   <li>Rol: Requerido, debe ser uno de los valores del enum Rol</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Rol
 */
@Entity
@Table(name = "usuarios", indexes = {
    @Index(name = "idx_usuario_email", columnList = "email")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(nullable = false)
    @ToString.Exclude // No mostrar en logs por seguridad
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

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
     * Enum que define los roles disponibles en el sistema para control de acceso.
     * 
     * <p>Los roles determinan qué operaciones puede realizar cada usuario:</p>
     * <ul>
     *   <li><b>ADMIN:</b> Acceso total al sistema. Puede gestionar usuarios, eliminar registros,
     *       y realizar todas las operaciones administrativas.</li>
     *   <li><b>VET:</b> Veterinario. Puede crear/editar consultas médicas, gestionar citas,
     *       ver historiales clínicos completos. No puede gestionar usuarios.</li>
     *   <li><b>RECEPCION:</b> Recepcionista. Puede gestionar citas, propietarios y pacientes.
     *       No puede crear consultas médicas ni gestionar usuarios.</li>
     *   <li><b>ESTUDIANTE:</b> Solo lectura. Puede consultar información pero no modificar
     *       ningún registro. Útil para estudiantes de veterinaria en prácticas.</li>
     * </ul>
     */
    public enum Rol {
        /** Administrador del sistema - Acceso total */
        ADMIN,
        
        /** Veterinario - Puede crear consultas y gestionar historial clínico */
        VET,
        
        /** Recepcionista - Gestión de citas y clientes */
        RECEPCION,
        
        /** Estudiante - Solo lectura */
        ESTUDIANTE
    }

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

