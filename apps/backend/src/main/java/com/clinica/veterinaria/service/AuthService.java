package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.LoginRequestDTO;
import com.clinica.veterinaria.dto.LoginResponseDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.logging.AuditLogger;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de autenticación
 * Maneja login y generación de tokens JWT
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final AuditLogger auditLogger;

    /**
     * Autentica un usuario y genera un token JWT
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        String ipAddress = getClientIp();
        log.info("→ Intento de login para usuario: {} desde IP: {}", request.getEmail(), ipAddress);

        try {
            // Autenticar con Spring Security
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            log.error("✗ Credenciales inválidas para usuario: {} desde IP: {}", request.getEmail(), ipAddress);
            // Registrar intento fallido en auditoría
            auditLogger.logLoginFailure(request.getEmail(), ipAddress, "Credenciales inválidas");
            throw new RuntimeException("Email o contraseña incorrectos");
        }

        // Cargar los detalles del usuario
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        
        // Obtener el usuario de la base de datos
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.error("✗ Usuario no encontrado: {}", request.getEmail());
                auditLogger.logLoginFailure(request.getEmail(), ipAddress, "Usuario no encontrado");
                return new RuntimeException("Usuario no encontrado");
            });

        // Verificar si el usuario está activo
        if (!usuario.getActivo()) {
            log.warn("✗ Intento de login de usuario inactivo: {}", request.getEmail());
            auditLogger.logLoginFailure(request.getEmail(), ipAddress, "Usuario inactivo");
            throw new RuntimeException("Usuario inactivo");
        }

        // Agregar información adicional al token (rol, etc.)
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("rol", usuario.getRol().name());
        extraClaims.put("userId", usuario.getId());

        // Generar el token JWT
        final String jwt = jwtUtil.generateToken(userDetails, extraClaims);

        log.info("✓ Login exitoso para usuario: {} (ID: {}, Rol: {}) desde IP: {}", 
                request.getEmail(), usuario.getId(), usuario.getRol(), ipAddress);

        // Registrar login exitoso en auditoría
        auditLogger.logLoginSuccess(request.getEmail(), ipAddress);

        // Crear la respuesta
        return LoginResponseDTO.builder()
            .token(jwt)
            .type("Bearer")
            .usuario(UsuarioDTO.fromEntity(usuario))
            .build();
    }
    
    /**
     * Obtiene la IP del cliente desde el request actual
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            
            // Si hay múltiples IPs, tomar la primera
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            
            return ip != null ? ip : "unknown";
        } catch (Exception e) {
            log.debug("No se pudo obtener la IP del cliente", e);
            return "unknown";
        }
    }

    /**
     * Valida un token JWT
     */
    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            log.error("Error validando token", e);
            return false;
        }
    }

    /**
     * Obtiene el usuario autenticado desde un token
     */
    @Transactional(readOnly = true)
    public UsuarioDTO getUserFromToken(String token) {
        String email = jwtUtil.extractUsername(token);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return UsuarioDTO.fromEntity(usuario);
    }
}

