package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.LoginRequestDTO;
import com.clinica.veterinaria.dto.LoginResponseDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
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
 * Servicio de autenticación y autorización con JWT.
 * 
 * <p>Este servicio es el núcleo del sistema de seguridad de la aplicación. Gestiona
 * la autenticación de usuarios mediante email/contraseña y genera tokens JWT para
 * sesiones stateless. Incluye auditoría completa de intentos de login.</p>
 * 
 * <p><strong>Flujo de autenticación:</strong></p>
 * <ol>
 *   <li>Cliente envía credenciales (email + password)</li>
 *   <li>Spring Security valida las credenciales contra base de datos (password BCrypt)</li>
 *   <li>Si es válido, se verifica que el usuario esté activo</li>
 *   <li>Se genera un token JWT firmado con información del usuario</li>
 *   <li>Token es retornado al cliente con información del usuario</li>
 *   <li>Cliente incluye token en header Authorization: Bearer {token} en requests subsecuentes</li>
 * </ol>
 * 
 * <p><strong>Estructura del token JWT:</strong></p>
 * <pre>
 * {
 *   "sub": "usuario@email.com",           // Email del usuario (subject)
 *   "rol": "VET",                         // Rol del usuario
 *   "userId": 123,                        // ID del usuario
 *   "iat": 1699999999,                    // Fecha de emisión (timestamp)
 *   "exp": 1700086399                     // Fecha de expiración (timestamp)
 * }
 * </pre>
 * 
 * <p><strong>Seguridad implementada:</strong></p>
 * <ul>
 *   <li><b>Autenticación Spring Security:</b> Validación robusta con AuthenticationManager</li>
 *   <li><b>Tokens firmados:</b> JWT firmado con clave secreta (HMAC-SHA256)</li>
 *   <li><b>Expiración configurale:</b> Tokens con tiempo de vida limitado</li>
 *   <li><b>Auditoría completa:</b> Registro de login exitoso y fallidos con IP</li>
 *   <li><b>Validación de estado:</b> Usuarios inactivos no pueden autenticarse</li>
 *   <li><b>Detección de IP:</b> Captura IP real incluso detrás de proxies/load balancers</li>
 * </ul>
 * 
 * <p><strong>Auditoría de seguridad:</strong></p>
 * <ul>
 *   <li>Todos los intentos de login (exitosos y fallidos) son registrados</li>
 *   <li>Incluye email, IP del cliente y razón de fallo si aplica</li>
 *   <li>Permite detectar patrones de ataques de fuerza bruta</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see JwtUtil
 * @see AuthenticationManager
 * @see AuditLogger
 * @see LoginRequestDTO
 * @see LoginResponseDTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private static final String MSG_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    private static final String MSG_CREDENCIALES_INVALIDAS = "Email o contraseña incorrectos";
    private static final String MSG_USUARIO_INACTIVO = "Usuario inactivo";
    private static final String UNKNOWN_IP = "unknown";

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final IAuditLogger auditLogger;

    /**
     * Autentica un usuario y genera un token JWT para acceso stateless.
     * 
     * <p>Este método implementa el flujo completo de autenticación:</p>
     * <ol>
     *   <li>Valida credenciales con Spring Security (BCrypt password verification)</li>
     *   <li>Verifica que el usuario esté activo</li>
     *   <li>Genera token JWT con claims personalizados (rol, userId)</li>
     *   <li>Registra el evento en auditoría con IP del cliente</li>
     * </ol>
     * 
     * <p><strong>Manejo de errores:</strong></p>
     * <ul>
     *   <li><b>Credenciales inválidas:</b> BadCredentialsException → RuntimeException genérica
     *       (no revelar si el email existe o la contraseña es incorrecta por seguridad)</li>
     *   <li><b>Usuario no encontrado:</b> RuntimeException</li>
     *   <li><b>Usuario inactivo:</b> RuntimeException</li>
     * </ul>
     * 
     * <p><strong>Auditoría:</strong> Todos los intentos (exitosos y fallidos) son registrados
     * con email, IP y razón del fallo para análisis de seguridad.</p>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * LoginRequestDTO request = new LoginRequestDTO("vet@clinica.com", "password123");
     * LoginResponseDTO response = authService.login(request);
     * // response.getToken() = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * // response.getUsuario() = UsuarioDTO con datos del usuario autenticado
     * </pre>
     * 
     * @param request Credenciales del usuario (email y password). No puede ser null.
     * @return Respuesta con token JWT y datos del usuario autenticado.
     * @throws RuntimeException si las credenciales son inválidas, el usuario no existe o está inactivo.
     * @see AuditLogger#logLoginSuccess(String, String)
     * @see AuditLogger#logLoginFailure(String, String, String)
     * @see JwtUtil#generateToken(UserDetails, Map)
     */
    @SuppressWarnings("null") // Los valores del DTO son validados antes de usar
    public LoginResponseDTO login(@NonNull LoginRequestDTO request) {
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
            throw new BadCredentialsException(MSG_CREDENCIALES_INVALIDAS);
        }

        // Cargar los detalles del usuario
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        
        // Obtener el usuario de la base de datos
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.error("✗ Usuario no encontrado: {}", request.getEmail());
                auditLogger.logLoginFailure(request.getEmail(), ipAddress, MSG_USUARIO_NO_ENCONTRADO);
                return new ResourceNotFoundException("Usuario", "email", request.getEmail());
            });

        // Verificar si el usuario está activo
        if (Boolean.FALSE.equals(usuario.getActivo())) {
            log.warn("✗ Intento de login de usuario inactivo: {}", request.getEmail());
            auditLogger.logLoginFailure(request.getEmail(), ipAddress, MSG_USUARIO_INACTIVO);
            throw new BusinessException(MSG_USUARIO_INACTIVO);
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
     * Obtiene la dirección IP real del cliente que realiza el request.
     * 
     * <p>Este método maneja correctamente solicitudes que pasan a través de proxies,
     * load balancers o CDNs, inspeccionando los headers estándar de forwarding:</p>
     * <ul>
     *   <li>X-Forwarded-For (estándar de facto)</li>
     *   <li>X-Real-IP (usado por Nginx y otros)</li>
     *   <li>RemoteAddr (IP directa si no hay proxy)</li>
     * </ul>
     * 
     * <p><strong>Caso especial:</strong> Si X-Forwarded-For contiene múltiples IPs
     * (cliente → proxy1 → proxy2 → servidor), se toma la primera (IP del cliente real).</p>
     * 
     * @return Dirección IP del cliente o "unknown" si no se puede determinar.
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || UNKNOWN_IP.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            
            // Si hay múltiples IPs, tomar la primera
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
            
            return ip != null ? ip : UNKNOWN_IP;
        } catch (Exception e) {
            log.debug("No se pudo obtener la IP del cliente", e);
            return UNKNOWN_IP;
        }
    }

    /**
     * Valida si un token JWT es válido y no ha expirado.
     * 
     * <p>Verifica:</p>
     * <ul>
     *   <li>Firma del token (no ha sido alterado)</li>
     *   <li>Fecha de expiración</li>
     *   <li>Usuario existe y coincide</li>
     * </ul>
     * 
     * @param token Token JWT a validar (sin el prefijo "Bearer "). No puede ser null.
     * @return true si el token es válido, false en caso contrario.
     */
    @Transactional(readOnly = true)
    public boolean validateToken(@NonNull String token) {
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
     * Extrae los datos del usuario autenticado desde un token JWT.
     * 
     * <p>Útil para endpoints que necesitan información del usuario actual
     * basándose en el token recibido.</p>
     * 
     * @param token Token JWT válido. No puede ser null.
     * @return DTO del usuario sin contraseña.
     * @throws RuntimeException si el usuario no existe.
     */
    @Transactional(readOnly = true)
    public UsuarioDTO getUserFromToken(@NonNull String token) {
        String email = jwtUtil.extractUsername(token);
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "email", email));
        return UsuarioDTO.fromEntity(usuario);
    }
}
