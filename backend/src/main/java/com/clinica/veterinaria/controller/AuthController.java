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
 * Controlador REST para autenticación
 * Endpoints públicos para login
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Login - Autentica un usuario y devuelve un token JWT
     * 
     * @param request Credenciales de login
     * @return Token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("POST /api/auth/login - email: {}", request.getEmail());
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Validar token - Verifica si un token es válido
     * 
     * @param token Token JWT
     * @return true si es válido, false si no
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        log.info("GET /api/auth/validate");
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
}

