package com.clinica.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para resetear contraseña usando un token de recuperación.
 * 
 * <p>Este DTO se utiliza cuando el usuario hace clic en el enlace de recuperación
 * y establece una nueva contraseña.</p>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Token: Requerido</li>
 *   <li>Password: Requerido, mínimo 6 caracteres</li>
 * </ul>
 * 
 * <p><strong>Nota de seguridad:</strong> La contraseña se recibe en texto plano pero se
 * hashea automáticamente con BCrypt antes de almacenarse en la base de datos.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-12-XX
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordWithTokenDTO {

    @NotBlank(message = "El token es requerido")
    private String token;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}

