package com.clinica.veterinaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para solicitar recuperación de contraseña.
 * 
 * <p>Este DTO se utiliza cuando un usuario olvida su contraseña y necesita
 * recibir un enlace de recuperación por correo electrónico.</p>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Email: Requerido, formato válido</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-12-XX
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequestDTO {

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;
}

