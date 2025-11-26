package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.ClienteLoginRequestDTO;
import com.clinica.veterinaria.dto.ClienteLoginResponseDTO;
import com.clinica.veterinaria.service.ClienteAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación de clientes (propietarios).
 */
@RestController
@RequestMapping("/api/public/clientes/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClienteAuthController {

    private final ClienteAuthService clienteAuthService;

    /**
     * Autentica un cliente y genera un token JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<ClienteLoginResponseDTO> login(@Valid @RequestBody ClienteLoginRequestDTO request) {
        log.info("POST /api/public/clientes/auth/login - email: {}", request.getEmail());
        ClienteLoginResponseDTO response = clienteAuthService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Valida si un token JWT es válido.
     */
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        log.info("GET /api/public/clientes/auth/validate");
        boolean isValid = clienteAuthService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }
}

