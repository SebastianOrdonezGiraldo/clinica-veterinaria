package com.clinica.veterinaria.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro para interceptar requests y validar tokens JWT
 * Se ejecuta una vez por cada request
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final ClienteUserDetailsService clienteUserDetailsService;

    /**
     * Filtra cada request para validar el token JWT
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Extraer el header Authorization
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Validar que el header existe y tiene el formato correcto
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extraer el token
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Error extrayendo username del token JWT", e);
            }
        }

        // Si tenemos username y no hay autenticación en el contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            UserDetails userDetails = null;
            
            try {
                // Intentar extraer el rol del token
                String rol = jwtUtil.extractRole(jwt);
                
                // Cargar los detalles según el tipo de usuario
                if ("CLIENTE".equals(rol)) {
                    userDetails = this.clienteUserDetailsService.loadUserByUsername(username);
                } else {
                    userDetails = this.userDetailsService.loadUserByUsername(username);
                }
            } catch (Exception e) {
                // Si no se puede extraer el rol, intentar como usuario del sistema
                try {
                    userDetails = this.userDetailsService.loadUserByUsername(username);
                } catch (UsernameNotFoundException ex) {
                    // Si falla, intentar como cliente
                    try {
                        userDetails = this.clienteUserDetailsService.loadUserByUsername(username);
                    } catch (UsernameNotFoundException ex2) {
                        logger.error("Usuario no encontrado: " + username);
                    }
                }
            }

            // Validar el token si tenemos userDetails
            if (userDetails != null && Boolean.TRUE.equals(jwtUtil.validateToken(jwt, userDetails))) {
                
                // Crear el objeto de autenticación
                UsernamePasswordAuthenticationToken authenticationToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                
                authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // Establecer la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        
        // Continuar con el filtro chain
        filterChain.doFilter(request, response);
    }
}

