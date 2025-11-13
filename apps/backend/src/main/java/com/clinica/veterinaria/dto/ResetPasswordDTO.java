package com.clinica.veterinaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para resetear la contraseña de un usuario.
 * 
 * <p>Este DTO se utiliza exclusivamente para la operación de reset de contraseña,
 * que permite a un administrador cambiar la contraseña de cualquier usuario del sistema.</p>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Password: Requerido, mínimo 6 caracteres</li>
 * </ul>
 * 
 * <p><strong>Nota de seguridad:</strong> La contraseña se recibe en texto plano pero se
 * hashea automáticamente con BCrypt antes de almacenarse en la base de datos.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordDTO {

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}

