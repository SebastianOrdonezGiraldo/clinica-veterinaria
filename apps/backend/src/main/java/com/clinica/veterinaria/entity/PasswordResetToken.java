package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa tokens de recuperación de contraseña.
 * 
 * <p>Esta entidad almacena tokens temporales y seguros para permitir a los usuarios
 * recuperar sus contraseñas sin necesidad de intervención administrativa.</p>
 * 
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li><b>Token único:</b> UUID aleatorio para seguridad</li>
 *   <li><b>Expiración:</b> Los tokens expiran después de 24 horas por defecto</li>
 *   <li><b>Uso único:</b> Los tokens se marcan como usados después de su utilización</li>
 *   <li><b>Tipo de usuario:</b> Soporta tanto usuarios del sistema como propietarios/clientes</li>
 * </ul>
 * 
 * <p><strong>Seguridad:</strong></p>
 * <ul>
 *   <li>Los tokens son únicos y no predecibles</li>
 *   <li>Expiración automática para prevenir ataques</li>
 *   <li>Marcado como usado previene reutilización</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-12-XX
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_token_value", columnList = "token", unique = true),
    @Index(name = "idx_token_email", columnList = "email"),
    @Index(name = "idx_token_expires_at", columnList = "expiresAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Token único generado aleatoriamente (UUID)
     */
    @Column(nullable = false, unique = true, length = 255)
    private String token;

    /**
     * Email del usuario que solicita la recuperación
     */
    @Column(nullable = false, length = 100)
    private String email;

    /**
     * Tipo de usuario: "USUARIO" o "PROPIETARIO"
     */
    @Column(nullable = false, length = 20)
    private String userType;

    /**
     * Fecha y hora de expiración del token
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Indica si el token ya fue usado
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean usado = false;

    /**
     * Fecha y hora de creación del token
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Verifica si el token ha expirado
     * 
     * @return true si el token ha expirado, false en caso contrario
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Verifica si el token es válido (no expirado y no usado)
     * 
     * @return true si el token es válido, false en caso contrario
     */
    public boolean isValid() {
        return !usado && !isExpired();
    }
}

