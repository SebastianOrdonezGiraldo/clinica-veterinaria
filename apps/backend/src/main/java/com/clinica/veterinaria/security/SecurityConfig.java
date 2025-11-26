package com.clinica.veterinaria.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración principal de Spring Security para la aplicación.
 * 
 * <p>Esta clase define toda la configuración de seguridad, incluyendo:</p>
 * <ul>
 *   <li><b>Autenticación:</b> Configuración de proveedores de autenticación y password encoder</li>
 *   <li><b>Autorización:</b> Reglas de acceso a endpoints (públicos vs protegidos)</li>
 *   <li><b>Filtros:</b> Configuración de filtros JWT y CORS</li>
 *   <li><b>Sesiones:</b> Configuración stateless (sin sesiones, solo JWT)</li>
 * </ul>
 * 
 * <p><strong>Endpoints públicos (sin autenticación):</strong></p>
 * <ul>
 *   <li>/api/auth/** - Endpoints de autenticación (login, validación)</li>
 *   <li>/api/public/** - Endpoints públicos</li>
 *   <li>/swagger-ui/**, /v3/api-docs/** - Documentación de API</li>
 * </ul>
 * 
 * <p><strong>Endpoints protegidos:</strong></p>
 * <ul>
 *   <li>Todos los demás endpoints requieren autenticación mediante JWT</li>
 *   <li>El control de acceso por roles se realiza mediante @PreAuthorize en los controllers</li>
 * </ul>
 * 
 * <p><strong>Configuración CORS:</strong></p>
 * <ul>
 *   <li>Orígenes permitidos: localhost:5173 (Vite), localhost:3000 (React)</li>
 *   <li>Métodos permitidos: GET, POST, PUT, DELETE, OPTIONS</li>
 *   <li>Headers permitidos: Authorization, Content-Type</li>
 *   <li>Credenciales: Habilitadas</li>
 * </ul>
 * 
 * <p><strong>Seguridad:</strong></p>
 * <ul>
 *   <li>CSRF deshabilitado (no necesario con JWT stateless)</li>
 *   <li>Sesiones stateless (sin almacenamiento de sesión en servidor)</li>
 *   <li>Password encoder: BCrypt (hashing seguro de contraseñas)</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see CustomUserDetailsService
 * @see JwtAuthenticationFilter
 * @see JwtUtil
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configura el password encoder (BCrypt)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura el proveedor de autenticación
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Expone el AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Permitir múltiples puertos de desarrollo (Vite puede usar diferentes puertos)
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",  // Puerto por defecto de Vite
            "http://localhost:5174",  // Puerto alternativo de Vite
            "http://localhost:3000"   // Puerto por defecto de Create React App
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Correlation-ID"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configura la cadena de filtros de seguridad
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF (usamos JWT)
            .csrf(csrf -> csrf.disable())
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configurar autorización
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (sin autenticación)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                // Swagger/OpenAPI - múltiples rutas posibles
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**").permitAll()
                
                // Todos los demás endpoints requieren autenticación
                .anyRequest().authenticated()
            )
            
            // Configurar sesión stateless (sin sesión, usamos JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configurar el proveedor de autenticación
            .authenticationProvider(authenticationProvider())
            
            // Agregar el filtro JWT antes del filtro de autenticación de usuario/contraseña
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

