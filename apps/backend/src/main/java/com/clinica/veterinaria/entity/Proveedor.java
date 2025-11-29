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
 * Entidad JPA que representa un proveedor de productos para la clínica veterinaria.
 * 
 * <p>Esta entidad almacena la información de contacto y datos de los proveedores
 * que suministran productos al inventario de la clínica. Facilita la gestión de
 * compras y el seguimiento de relaciones comerciales.</p>
 * 
 * <p><strong>Información gestionada:</strong></p>
 * <ul>
 *   <li><b>Identificación:</b> Nombre, razón social, RUC/NIT</li>
 *   <li><b>Contacto:</b> Email, teléfono, dirección</li>
 *   <li><b>Estado:</b> Activo/Inactivo (soft delete)</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li><b>Movimientos:</b> Uno a muchos - Historial de compras a este proveedor</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Email - Para búsquedas por email</li>
 *   <li>RUC/NIT - Para búsquedas por identificación fiscal (si aplica)</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 200 caracteres</li>
 *   <li>Email: Formato válido, máximo 100 caracteres</li>
 *   <li>Teléfono: Opcional, máximo 20 caracteres</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 */
@Entity
@Table(name = "proveedores", indexes = {
    @Index(name = "idx_proveedor_email", columnList = "email"),
    @Index(name = "idx_proveedor_ruc", columnList = "ruc")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Proveedor {

    /**
     * Identificador único del proveedor (generado automáticamente).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Nombre o razón social del proveedor.
     */
    @NotBlank(message = "El nombre del proveedor es requerido")
    @Size(max = 200, message = "El nombre del proveedor no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String nombre;

    /**
     * RUC, NIT o identificación fiscal del proveedor (opcional).
     */
    @Size(max = 50, message = "El RUC/NIT no puede exceder 50 caracteres")
    @Column(length = 50)
    private String ruc;

    /**
     * Email de contacto del proveedor.
     */
    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(length = 100)
    private String email;

    /**
     * Teléfono de contacto del proveedor.
     */
    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(length = 20)
    private String telefono;

    /**
     * Dirección del proveedor.
     */
    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    @Column(length = 500)
    private String direccion;

    /**
     * Notas adicionales sobre el proveedor.
     * Puede incluir información sobre términos de pago, condiciones, etc.
     */
    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    @Column(length = 1000)
    private String notas;

    /**
     * Indica si el proveedor está activo.
     * Los proveedores inactivos no se muestran en listados pero se mantienen
     * para preservar el historial de compras.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Fecha y hora de creación del registro (automático).
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de última actualización (automático).
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

