package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST público para recursos accesibles sin autenticación.
 * 
 * <p>Este controlador expone endpoints públicos que pueden ser accedidos sin
 * autenticación, útiles para funcionalidades públicas como agendar citas.</p>
 * 
 * <p><strong>Endpoints disponibles:</strong></p>
 * <ul>
 *   <li><b>GET /api/public/veterinarios:</b> Lista veterinarios activos disponibles</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PublicController {

    private final UsuarioService usuarioService;

    /**
     * Obtiene la lista de veterinarios activos disponibles para agendar citas.
     * 
     * <p>Este endpoint es público y no requiere autenticación. Útil para que
     * los clientes puedan ver qué veterinarios están disponibles al agendar
     * una cita.</p>
     * 
     * @return Lista de veterinarios activos.
     */
    @GetMapping("/veterinarios")
    public ResponseEntity<List<UsuarioDTO>> getVeterinarios() {
        log.info("GET /api/public/veterinarios");
        return ResponseEntity.ok(usuarioService.findVeterinariosActivos());
    }
}

