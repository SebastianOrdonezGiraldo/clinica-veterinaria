package com.clinica.veterinaria.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilidad para la gestión completa de tokens JWT (JSON Web Tokens).
 * 
 * <p>Este componente proporciona funcionalidades para generar, validar y extraer información
 * de tokens JWT utilizados para autenticación stateless en la aplicación. Utiliza el algoritmo
 * HS256 (HMAC SHA-256) para firmar los tokens.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li><b>Generación de tokens:</b> Crea tokens JWT firmados con información del usuario</li>
 *   <li><b>Validación:</b> Verifica la validez y expiración de tokens</li>
 *   <li><b>Extracción de información:</b> Obtiene email, claims y fecha de expiración</li>
 * </ul>
 * 
 * <p><strong>Configuración:</strong></p>
 * <ul>
 *   <li><b>Secret:</b> Clave secreta configurada en application.properties (jwt.secret)</li>
 *   <li><b>Validez:</b> 10 horas (36000000 milisegundos)</li>
 *   <li><b>Algoritmo:</b> HS256 (HMAC SHA-256)</li>
 * </ul>
 * 
 * <p><strong>Seguridad:</strong></p>
 * <ul>
 *   <li>Los tokens están firmados con una clave secreta</li>
 *   <li>La validez se verifica en cada petición</li>
 *   <li>Los tokens expirados son rechazados automáticamente</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see SecurityConfig
 * @see JwtAuthenticationFilter
 * @see AuthService
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private static final long JWT_TOKEN_VALIDITY = 10L * 60L * 60L * 1000L; // 10 horas

    /**
     * Obtiene la clave de firma
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el email (username) del token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Verifica si el token ha expirado
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Genera un token para un usuario
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Genera un token con claims adicionales
     */
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        return createToken(extraClaims, userDetails.getUsername());
    }

    /**
     * Crea el token JWT usando la API moderna de JJWT.
     * 
     * <p>Utiliza los métodos no deprecados de la librería JJWT 0.12.x:
     * - claims() en lugar de setClaims()
     * - subject() en lugar de setSubject()
     * - issuedAt() en lugar de setIssuedAt()
     * - expiration() en lugar de setExpiration()
     * - signWith(SecretKey) sin especificar algoritmo (se infiere automáticamente)
     * </p>
     * 
     * @param claims Claims adicionales a incluir en el token
     * @param subject Subject del token (normalmente el email del usuario)
     * @return Token JWT firmado como string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + JWT_TOKEN_VALIDITY);
        
        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(getSigningKey())
            .compact();
    }

    /**
     * Valida el token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

