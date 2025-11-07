package com.clinica.veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para respuesta de autenticación exitosa.
 * 
 * <p>Este DTO se retorna después de una autenticación exitosa y contiene el token JWT
 * necesario para acceder a los endpoints protegidos, junto con la información del usuario
 * autenticado (sin contraseña).</p>
 * 
 * <p><strong>Campos:</strong></p>
 * <ul>
 *   <li><b>token:</b> Token JWT firmado que debe incluirse en el header Authorization
 *       de todas las peticiones subsecuentes</li>
 *   <li><b>type:</b> Tipo de token, siempre "Bearer"</li>
 *   <li><b>usuario:</b> Información del usuario autenticado (sin contraseña)</li>
 * </ul>
 * 
 * <p><strong>Uso del token:</strong></p>
 * <pre>
 * // En peticiones HTTP subsecuentes:
 * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 * </pre>
 * 
 * <p><strong>Ejemplo de respuesta:</strong></p>
 * <pre>
 * {
 *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "type": "Bearer",
 *   "usuario": {
 *     "id": 1,
 *     "nombre": "Dr. Juan Pérez",
 *     "email": "juan@clinica.com",
 *     "rol": "VET",
 *     "activo": true
 *   }
 * }
 * </pre>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see LoginRequestDTO
 * @see UsuarioDTO
 * @see AuthService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private String token;
    private String type; // "Bearer"
    private UsuarioDTO usuario;
}

