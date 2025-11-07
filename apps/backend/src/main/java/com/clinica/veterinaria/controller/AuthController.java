package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.LoginRequestDTO;
import com.clinica.veterinaria.dto.LoginResponseDTO;
import com.clinica.veterinaria.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación y autorización.
 * 
 * <p>Este controlador expone endpoints públicos para la autenticación de usuarios
 * mediante email/contraseña y generación de tokens JWT. No requiere autenticación
 * previa (endpoints públicos).</p>
 * 
 * <p><strong>Endpoints disponibles:</strong></p>
 * <ul>
 *   <li><b>POST /api/auth/login:</b> Autentica usuario y retorna token JWT</li>
 *   <li><b>GET /api/auth/validate:</b> Valida si un token JWT es válido</li>
 * </ul>
 * 
 * <p><strong>Seguridad:</strong></p>
 * <ul>
 *   <li>Endpoints públicos (no requieren autenticación)</li>
 *   <li>Validación de entrada con Jakarta Bean Validation</li>
 *   <li>Logging de todos los intentos de autenticación</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see AuthService
 * @see LoginRequestDTO
 * @see LoginResponseDTO
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Autentica un usuario y genera un token JWT para acceso stateless.
     * 
     * <p>Este endpoint es el punto de entrada principal para la autenticación.
     * Valida las credenciales y retorna un token JWT que debe ser incluido en
     * el header Authorization de todas las peticiones subsecuentes.</p>
     * 
     * <p><strong>Request Body:</strong></p>
     * <pre>
     * {
     *   "email": "usuario@clinica.com",
     *   "password": "contraseña123"
     * }
     * </pre>
     * 
     * <p><strong>Response (200 OK):</strong></p>
     * <pre>
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "type": "Bearer",
     *   "usuario": {
     *     "id": 1,
     *     "nombre": "Dr. Juan Pérez",
     *     "email": "juan@clinica.com",
     *     "rol": "VET"
     *   }
     * }
     * </pre>
     * 
     * <p><strong>Códigos de respuesta:</strong></p>
     * <ul>
     *   <li><b>200 OK:</b> Autenticación exitosa</li>
     *   <li><b>400 Bad Request:</b> Datos de entrada inválidos</li>
     *   <li><b>401 Unauthorized:</b> Credenciales inválidas o usuario inactivo</li>
     * </ul>
     * 
     * @param request Credenciales de autenticación (email y password). Debe ser válido según
     *                las anotaciones de validación en LoginRequestDTO.
     * @return Respuesta con token JWT y datos del usuario autenticado.
     * @throws RuntimeException si las credenciales son inválidas o el usuario está inactivo.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("POST /api/auth/login - email: {}", request.getEmail());
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Valida si un token JWT es válido y no ha expirado.
     * 
     * <p>Útil para verificar la validez de un token antes de realizar operaciones
     * o para renovar sesiones en el frontend.</p>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * GET /api/auth/validate?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * </pre>
     * 
     * <p><strong>Response:</strong></p>
     * <ul>
     *   <li><b>true:</b> Token válido y no expirado</li>
     *   <li><b>false:</b> Token inválido, expirado o usuario no existe</li>
     * </ul>
     * 
     * @param token Token JWT a validar (sin el prefijo "Bearer "). No puede ser null.
     * @return true si el token es válido, false en caso contrario.
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        log.info("GET /api/auth/validate");
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
}

