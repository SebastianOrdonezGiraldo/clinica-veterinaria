package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.CitaDTO;
import com.clinica.veterinaria.dto.PacienteDTO;
import com.clinica.veterinaria.dto.PropietarioDTO;
import com.clinica.veterinaria.repository.PropietarioRepository;
import com.clinica.veterinaria.security.JwtUtil;
import com.clinica.veterinaria.service.CitaService;
import com.clinica.veterinaria.service.PacienteService;
import com.clinica.veterinaria.service.PropietarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para clientes (propietarios) autenticados.
 * Permite a los clientes ver sus citas y mascotas.
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClienteController {

    private final PropietarioService propietarioService;
    private final CitaService citaService;
    private final PacienteService pacienteService;
    private final JwtUtil jwtUtil;

    /**
     * Obtiene el propietario autenticado.
     */
    @GetMapping("/mi-perfil")
    public ResponseEntity<PropietarioDTO> getMiPerfil(HttpServletRequest request) {
        String email = extractEmailFromToken(request);
        log.info("GET /api/clientes/mi-perfil - Cliente: {}", email);
        
        PropietarioDTO propietario = propietarioService.findByEmail(email);
        return ResponseEntity.ok(propietario);
    }

    /**
     * Obtiene todas las citas del cliente autenticado.
     */
    @GetMapping("/mis-citas")
    public ResponseEntity<List<CitaDTO>> getMisCitas(HttpServletRequest request) {
        String email = extractEmailFromToken(request);
        log.info("GET /api/clientes/mis-citas - Cliente: {}", email);
        
        PropietarioDTO propietario = propietarioService.findByEmail(email);
        List<CitaDTO> citas = citaService.findByPropietario(propietario.getId());
        
        return ResponseEntity.ok(citas);
    }

    /**
     * Obtiene todas las mascotas del cliente autenticado.
     */
    @GetMapping("/mis-mascotas")
    public ResponseEntity<List<PacienteDTO>> getMisMascotas(HttpServletRequest request) {
        String email = extractEmailFromToken(request);
        log.info("GET /api/clientes/mis-mascotas - Cliente: {}", email);
        
        PropietarioDTO propietario = propietarioService.findByEmail(email);
        List<PacienteDTO> pacientes = pacienteService.findByPropietario(propietario.getId());
        
        return ResponseEntity.ok(pacientes);
    }

    /**
     * Extrae el email del token JWT del request.
     */
    private String extractEmailFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token no encontrado");
        }
        
        String token = authHeader.substring(7);
        return jwtUtil.extractUsername(token);
    }
}

