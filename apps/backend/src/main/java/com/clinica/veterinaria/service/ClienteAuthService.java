package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ClienteLoginRequestDTO;
import com.clinica.veterinaria.dto.ClienteLoginResponseDTO;
import com.clinica.veterinaria.dto.PropietarioDTO;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.repository.PropietarioRepository;
import com.clinica.veterinaria.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de autenticación para clientes (propietarios).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClienteAuthService {

    private final PropietarioRepository propietarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Autentica un cliente (propietario) y genera un token JWT.
     */
    public ClienteLoginResponseDTO login(@NonNull ClienteLoginRequestDTO request) {
        log.info("→ Intento de login de cliente: {}", request.getEmail());

        // Buscar propietario por email
        Propietario propietario = propietarioRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.error("✗ Cliente no encontrado con email: {}", request.getEmail());
                throw new BadCredentialsException("Email o contraseña incorrectos");
            });

        // Verificar que el propietario tenga contraseña (está registrado)
        if (propietario.getPassword() == null || propietario.getPassword().trim().isEmpty()) {
            log.error("✗ Cliente no tiene contraseña registrada: {}", request.getEmail());
            throw new BusinessException("Este cliente no tiene cuenta registrada. Por favor, regístrese primero.");
        }

        // Verificar que el propietario esté activo
        if (Boolean.FALSE.equals(propietario.getActivo())) {
            log.warn("✗ Intento de login de cliente inactivo: {}", request.getEmail());
            throw new BusinessException("Su cuenta está inactiva. Contacte con la clínica.");
        }

        // Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), propietario.getPassword())) {
            log.error("✗ Contraseña incorrecta para cliente: {}", request.getEmail());
            throw new BadCredentialsException("Email o contraseña incorrectos");
        }

        // Crear UserDetails para generar token
        UserDetails userDetails = User.builder()
            .username(propietario.getEmail())
            .password(propietario.getPassword())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE")))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!propietario.getActivo())
            .build();

        // Generar token JWT
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("rol", "CLIENTE");
        extraClaims.put("propietarioId", propietario.getId());
        
        String token = jwtUtil.generateToken(userDetails, extraClaims);
        
        log.info("✓ Cliente autenticado exitosamente - ID: {}", propietario.getId());
        
        return ClienteLoginResponseDTO.builder()
            .token(token)
            .type("Bearer")
            .propietario(PropietarioDTO.fromEntity(propietario))
            .build();
    }

    /**
     * Valida si un token JWT es válido para un cliente.
     */
    public boolean validateToken(String token) {
        try {
            // Extraer email del token
            String email = jwtUtil.extractUsername(token);
            if (email == null) {
                return false;
            }
            
            // Buscar propietario y crear UserDetails para validación
            Propietario propietario = propietarioRepository.findByEmail(email)
                .orElse(null);
            
            if (propietario == null || !propietario.getActivo()) {
                return false;
            }
            
            UserDetails userDetails = User.builder()
                .username(propietario.getEmail())
                .password(propietario.getPassword() != null ? propietario.getPassword() : "")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE")))
                .build();
            
            return jwtUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }
}

