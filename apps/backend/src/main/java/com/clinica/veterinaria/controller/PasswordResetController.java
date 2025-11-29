package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.ForgotPasswordRequestDTO;
import com.clinica.veterinaria.dto.ResetPasswordWithTokenDTO;
import com.clinica.veterinaria.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para recuperación de contraseñas.
 * 
 * <p>Este controlador expone endpoints públicos para que los usuarios puedan
 * recuperar sus contraseñas mediante tokens temporales enviados por email.</p>
 * 
 * <p><strong>Endpoints disponibles:</strong></p>
 * <ul>
 *   <li><b>POST /api/public/password/forgot-usuario:</b> Solicita recuperación para usuario del sistema</li>
 *   <li><b>POST /api/public/password/forgot-cliente:</b> Solicita recuperación para propietario/cliente</li>
 *   <li><b>POST /api/public/password/reset:</b> Resetea contraseña usando token</li>
 *   <li><b>GET /api/public/password/validate-token:</b> Valida si un token es válido</li>
 * </ul>
 * 
 * <p><strong>Seguridad:</strong></p>
 * <ul>
 *   <li>Endpoints públicos (no requieren autenticación)</li>
 *   <li>Tokens únicos y temporales (expiran en 24 horas)</li>
 *   <li>Uso único (se marcan como usados después de resetear)</li>
 *   <li>No revela si el email existe o no (por seguridad)</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-12-XX
 */
@RestController
@RequestMapping("/api/public/password")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Solicita recuperación de contraseña para un usuario del sistema.
     * 
     * <p>Genera un token único y envía un email con el enlace de recuperación.
     * Por seguridad, siempre retorna éxito incluso si el email no existe.</p>
     * 
     * @param request DTO con el email del usuario
     * @return Respuesta indicando que se envió el email (siempre éxito por seguridad)
     */
    @PostMapping("/forgot-usuario")
    public ResponseEntity<Map<String, String>> solicitarRecuperacionUsuario(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {
        log.info("POST /api/public/password/forgot-usuario - email: {}", request.getEmail());
        
        passwordResetService.solicitarRecuperacionUsuario(request.getEmail());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Si el email existe, recibirás un enlace de recuperación en breve.");
        return ResponseEntity.ok(response);
    }

    /**
     * Solicita recuperación de contraseña para un propietario/cliente.
     * 
     * <p>Genera un token único y envía un email con el enlace de recuperación.
     * Por seguridad, siempre retorna éxito incluso si el email no existe.</p>
     * 
     * @param request DTO con el email del propietario
     * @return Respuesta indicando que se envió el email (siempre éxito por seguridad)
     */
    @PostMapping("/forgot-cliente")
    public ResponseEntity<Map<String, String>> solicitarRecuperacionCliente(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {
        log.info("POST /api/public/password/forgot-cliente - email: {}", request.getEmail());
        
        passwordResetService.solicitarRecuperacionPropietario(request.getEmail());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Si el email existe, recibirás un enlace de recuperación en breve.");
        return ResponseEntity.ok(response);
    }

    /**
     * Resetea la contraseña usando un token de recuperación válido.
     * 
     * @param request DTO con el token y la nueva contraseña
     * @return Respuesta indicando éxito
     * @throws com.clinica.veterinaria.exception.domain.BusinessException si el token es inválido
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetearPassword(
            @Valid @RequestBody ResetPasswordWithTokenDTO request) {
        log.info("POST /api/public/password/reset - token: {}...", 
            request.getToken().substring(0, Math.min(8, request.getToken().length())));
        
        passwordResetService.resetearPasswordConToken(request.getToken(), request.getPassword());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Contraseña restablecida exitosamente. Ya puedes iniciar sesión.");
        return ResponseEntity.ok(response);
    }

    /**
     * Valida si un token de recuperación es válido y retorna información adicional.
     * 
     * @param token Token a validar
     * @return Información del token (válido, fecha de expiración)
     */
    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validarToken(@RequestParam String token) {
        log.info("GET /api/public/password/validate-token");
        
        Map<String, Object> tokenInfo = passwordResetService.obtenerInfoToken(token);
        
        return ResponseEntity.ok(tokenInfo);
    }
}

