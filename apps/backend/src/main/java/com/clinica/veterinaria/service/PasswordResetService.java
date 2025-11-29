package com.clinica.veterinaria.service;

import com.clinica.veterinaria.entity.PasswordResetToken;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.PasswordResetTokenRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio para gestionar la recuperación de contraseñas mediante tokens.
 * 
 * <p>Este servicio maneja el flujo completo de recuperación de contraseña:</p>
 * <ol>
 *   <li>Generación de token único y temporal</li>
 *   <li>Envío de email con enlace de recuperación</li>
 *   <li>Validación de token</li>
 *   <li>Reset de contraseña</li>
 *   <li>Limpieza automática de tokens expirados</li>
 * </ol>
 * 
 * <p><strong>Seguridad:</strong></p>
 * <ul>
 *   <li>Tokens únicos generados con UUID</li>
 *   <li>Expiración automática (24 horas por defecto)</li>
 *   <li>Uso único (se marcan como usados después de resetear)</li>
 *   <li>No revela si el email existe o no (por seguridad)</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-12-XX
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.password-reset.expiration-hours:24}")
    private int expirationHours;

    @Value("${app.mail.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Solicita un token de recuperación de contraseña para un usuario del sistema.
     * 
     * <p>Genera un token único, lo almacena y envía un email con el enlace de recuperación.
     * Por seguridad, siempre retorna éxito incluso si el email no existe.</p>
     * 
     * @param email Email del usuario que solicita la recuperación
     * @return true si el proceso se completó (siempre retorna true por seguridad)
     */
    public boolean solicitarRecuperacionUsuario(@NonNull String email) {
        log.info("→ Solicitud de recuperación de contraseña para usuario: {}", email);
        
        // Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        
        // Por seguridad, no revelar si el email existe o no
        if (usuario == null || Boolean.FALSE.equals(usuario.getActivo())) {
            log.warn("⚠ Solicitud de recuperación para email no encontrado o inactivo: {} (no se revela al usuario)", email);
            // Simular delay para prevenir timing attacks
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true; // Siempre retornar true por seguridad
        }

        // Invalidar tokens anteriores para este usuario
        invalidarTokensAnteriores(email, "USUARIO");

        // Generar nuevo token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .email(email)
            .userType("USUARIO")
            .expiresAt(expiresAt)
            .usado(false)
            .build();

        tokenRepository.save(resetToken);
        log.info("✓ Token de recuperación generado para usuario: {}", email);

        // Enviar email con enlace de recuperación
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token + "&type=usuario";
            boolean emailEnviado = emailService.enviarEmailRecuperacionPassword(
                email,
                usuario.getNombre(),
                resetUrl,
                "USUARIO"
            );
            
            if (emailEnviado) {
                log.info("✓ Email de recuperación enviado exitosamente a: {}", email);
            } else {
                log.warn("✗ No se pudo enviar email de recuperación a: {}", email);
            }
        } catch (Exception e) {
            log.error("✗ Error al enviar email de recuperación: {}", e.getMessage(), e);
            // No lanzar excepción para no revelar información
        }

        return true;
    }

    /**
     * Solicita un token de recuperación de contraseña para un propietario/cliente.
     * 
     * <p>Genera un token único, lo almacena y envía un email con el enlace de recuperación.
     * Por seguridad, siempre retorna éxito incluso si el email no existe.</p>
     * 
     * @param email Email del propietario que solicita la recuperación
     * @return true si el proceso se completó (siempre retorna true por seguridad)
     */
    public boolean solicitarRecuperacionPropietario(@NonNull String email) {
        log.info("→ Solicitud de recuperación de contraseña para propietario: {}", email);
        
        // Buscar propietario por email
        Propietario propietario = propietarioRepository.findByEmail(email).orElse(null);
        
        // Por seguridad, no revelar si el email existe o no
        if (propietario == null || Boolean.FALSE.equals(propietario.getActivo())) {
            log.warn("⚠ Solicitud de recuperación para email no encontrado o inactivo: {} (no se revela al usuario)", email);
            // Simular delay para prevenir timing attacks
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true; // Siempre retornar true por seguridad
        }

        // Verificar que el propietario tenga contraseña establecida
        if (propietario.getPassword() == null || propietario.getPassword().trim().isEmpty()) {
            log.warn("⚠ Propietario sin contraseña establecida: {} (debe usar establecer-password)", email);
            // Simular delay
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true; // No revelar que no tiene contraseña
        }

        // Invalidar tokens anteriores para este propietario
        invalidarTokensAnteriores(email, "PROPIETARIO");

        // Generar nuevo token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

        PasswordResetToken resetToken = PasswordResetToken.builder()
            .token(token)
            .email(email)
            .userType("PROPIETARIO")
            .expiresAt(expiresAt)
            .usado(false)
            .build();

        tokenRepository.save(resetToken);
        log.info("✓ Token de recuperación generado para propietario: {}", email);

        // Enviar email con enlace de recuperación
        try {
            String resetUrl = frontendUrl + "/cliente/reset-password?token=" + token;
            boolean emailEnviado = emailService.enviarEmailRecuperacionPassword(
                email,
                propietario.getNombre(),
                resetUrl,
                "PROPIETARIO"
            );
            
            if (emailEnviado) {
                log.info("✓ Email de recuperación enviado exitosamente a: {}", email);
            } else {
                log.warn("✗ No se pudo enviar email de recuperación a: {}", email);
            }
        } catch (Exception e) {
            log.error("✗ Error al enviar email de recuperación: {}", e.getMessage(), e);
            // No lanzar excepción para no revelar información
        }

        return true;
    }

    /**
     * Resetea la contraseña usando un token de recuperación válido.
     * 
     * @param token Token de recuperación
     * @param newPassword Nueva contraseña en texto plano
     * @throws BusinessException si el token es inválido, expirado o ya fue usado
     */
    public void resetearPasswordConToken(@NonNull String token, @NonNull String newPassword) {
        log.info("→ Intento de resetear contraseña con token: {}...", token.substring(0, Math.min(8, token.length())));
        
        // Buscar token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> {
                log.error("✗ Token de recuperación no encontrado");
                return new BusinessException("El enlace de recuperación no es válido o ha expirado.");
            });

        // Validar token
        if (!resetToken.isValid()) {
            log.error("✗ Token inválido o expirado - Usado: {}, Expirado: {}", 
                resetToken.getUsado(), resetToken.isExpired());
            throw new BusinessException("El enlace de recuperación no es válido o ha expirado.");
        }

        // Resetear contraseña según el tipo de usuario
        if ("USUARIO".equals(resetToken.getUserType())) {
            Usuario usuario = usuarioRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> {
                    log.error("✗ Usuario no encontrado para email: {}", resetToken.getEmail());
                    return new ResourceNotFoundException("Usuario", "email", resetToken.getEmail());
                });

            usuario.setPassword(passwordEncoder.encode(newPassword));
            usuarioRepository.save(usuario);
            log.info("✓ Contraseña reseteada exitosamente para usuario: {}", resetToken.getEmail());
        } else if ("PROPIETARIO".equals(resetToken.getUserType())) {
            Propietario propietario = propietarioRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> {
                    log.error("✗ Propietario no encontrado para email: {}", resetToken.getEmail());
                    return new ResourceNotFoundException("Propietario", "email", resetToken.getEmail());
                });

            propietario.setPassword(passwordEncoder.encode(newPassword));
            propietarioRepository.save(propietario);
            log.info("✓ Contraseña reseteada exitosamente para propietario: {}", resetToken.getEmail());
        } else {
            log.error("✗ Tipo de usuario inválido en token: {}", resetToken.getUserType());
            throw new BusinessException("Tipo de usuario inválido.");
        }

        // Marcar token como usado
        tokenRepository.markAsUsed(resetToken.getId());
        log.info("✓ Token marcado como usado");

        // Enviar email de confirmación
        try {
            if ("USUARIO".equals(resetToken.getUserType())) {
                Usuario usuario = usuarioRepository.findByEmail(resetToken.getEmail()).orElse(null);
                if (usuario != null) {
                    emailService.enviarEmailCambioPasswordUsuario(
                        usuario.getEmail(),
                        usuario.getNombre(),
                        false // No es reset por admin, es recuperación
                    );
                }
            } else {
                Propietario propietario = propietarioRepository.findByEmail(resetToken.getEmail()).orElse(null);
                if (propietario != null) {
                    emailService.enviarEmailCambioPasswordCliente(
                        propietario.getEmail(),
                        propietario.getNombre()
                    );
                }
            }
        } catch (Exception e) {
            log.error("✗ Error al enviar email de confirmación: {}", e.getMessage(), e);
            // No lanzar excepción, la contraseña ya fue cambiada
        }
    }

    /**
     * Valida si un token de recuperación es válido.
     * 
     * @param token Token a validar
     * @return true si el token es válido, false en caso contrario
     */
    @Transactional(readOnly = true)
    public boolean validarToken(@NonNull String token) {
        return tokenRepository.findByToken(token)
            .map(PasswordResetToken::isValid)
            .orElse(false);
    }

    /**
     * Obtiene información completa del token (válido, fecha de expiración).
     * 
     * @param token Token a consultar
     * @return Map con información del token
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerInfoToken(@NonNull String token) {
        Map<String, Object> info = new HashMap<>();
        
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        
        if (tokenOpt.isPresent()) {
            PasswordResetToken resetToken = tokenOpt.get();
            info.put("valid", resetToken.isValid());
            info.put("expiresAt", resetToken.getExpiresAt());
            info.put("expiresInHours", java.time.Duration.between(
                LocalDateTime.now(), 
                resetToken.getExpiresAt()
            ).toHours());
        } else {
            info.put("valid", false);
            info.put("expiresAt", null);
            info.put("expiresInHours", 0);
        }
        
        return info;
    }

    /**
     * Invalida todos los tokens anteriores para un email y tipo de usuario.
     * 
     * @param email Email del usuario
     * @param userType Tipo de usuario
     */
    private void invalidarTokensAnteriores(String email, String userType) {
        tokenRepository.findValidTokenByEmailAndUserType(email, userType, LocalDateTime.now())
            .ifPresent(token -> {
                tokenRepository.markAsUsed(token.getId());
                log.debug("Token anterior invalidado para: {}", email);
            });
    }

    /**
     * Limpia automáticamente tokens expirados (ejecuta diariamente a las 2 AM).
     */
    @Scheduled(cron = "0 0 2 * * ?") // Diariamente a las 2 AM
    public void limpiarTokensExpirados() {
        log.info("🧹 Limpiando tokens de recuperación expirados...");
        int eliminados = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("✓ {} tokens expirados eliminados", eliminados);
    }
}

