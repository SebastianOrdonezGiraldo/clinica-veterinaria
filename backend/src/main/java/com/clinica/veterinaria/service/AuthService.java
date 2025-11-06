package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.LoginRequestDTO;
import com.clinica.veterinaria.dto.LoginResponseDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Autentica un usuario y genera un token JWT
     */
    public LoginResponseDTO login(LoginRequestDTO request) {
        log.info("Intento de login para usuario: {}", request.getEmail());

        try {
            // Autenticar con Spring Security
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            log.error("Credenciales inválidas para usuario: {}", request.getEmail());
            throw new RuntimeException("Email o contraseña incorrectos");
        }

        // Cargar los detalles del usuario
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        
        // Obtener el usuario de la base de datos
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Agregar información adicional al token (rol, etc.)
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("rol", usuario.getRol().name());
        extraClaims.put("userId", usuario.getId());

        // Generar el token JWT
        final String jwt = jwtUtil.generateToken(userDetails, extraClaims);

        log.info("Login exitoso para usuario: {}", request.getEmail());

        // Crear la respuesta
        return LoginResponseDTO.builder()
            .token(jwt)
            .type("Bearer")
            .usuario(UsuarioDTO.fromEntity(usuario))
            .build();
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

