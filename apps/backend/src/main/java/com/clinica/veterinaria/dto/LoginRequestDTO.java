package com.clinica.veterinaria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para solicitud de autenticación (login).
 * 
 * <p>Este DTO se utiliza en el endpoint de autenticación para recibir las credenciales
 * del usuario. Contiene email y contraseña en texto plano (que será validada contra
 * el hash almacenado en la base de datos).</p>
 * 
 * <p><strong>Campos:</strong></p>
 * <ul>
 *   <li><b>email:</b> Email del usuario (requerido, formato válido)</li>
 *   <li><b>password:</b> Contraseña en texto plano (requerida)</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Email: Requerido, formato válido</li>
 *   <li>Password: Requerido</li>
 * </ul>
 * 
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>
 * POST /api/auth/login
 * {
 *   "email": "vet@clinica.com",
 *   "password": "miPassword123"
 * }
 * </pre>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see LoginResponseDTO
 * @see AuthService
 * @see AuthController
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "El email es requerido")
    @Email(message = "Email debe ser válido")
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    private String password;
}

