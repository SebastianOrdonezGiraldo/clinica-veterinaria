package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.NotificacionCreateDTO;
import com.clinica.veterinaria.dto.NotificacionDTO;
import com.clinica.veterinaria.service.NotificacionService;
import com.clinica.veterinaria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de notificaciones.
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-09
 */
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class NotificacionController {

    private final NotificacionService notificacionService;
    private final UsuarioService usuarioService;

    /**
     * Obtiene todas las notificaciones del usuario autenticado
     */
    @GetMapping
    public ResponseEntity<List<NotificacionDTO>> getMisNotificaciones() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("GET /api/notificaciones - email: {}", email);
        
        Long usuarioId = usuarioService.findByEmail(email).getId();
        return ResponseEntity.ok(notificacionService.findByUsuarioId(usuarioId));
    }

    /**
     * Obtiene las notificaciones no leídas del usuario autenticado
     */
    @GetMapping("/no-leidas")
    public ResponseEntity<List<NotificacionDTO>> getNoLeidas() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("GET /api/notificaciones/no-leidas - email: {}", email);
        
        Long usuarioId = usuarioService.findByEmail(email).getId();
        return ResponseEntity.ok(notificacionService.findNoLeidasByUsuarioId(usuarioId));
    }

    /**
     * Cuenta las notificaciones no leídas del usuario autenticado
     */
    @GetMapping("/no-leidas/count")
    public ResponseEntity<Map<String, Long>> getCountNoLeidas() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("GET /api/notificaciones/no-leidas/count - email: {}", email);
        
        Long usuarioId = usuarioService.findByEmail(email).getId();
        long count = notificacionService.countNoLeidasByUsuarioId(usuarioId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Crea una nueva notificación
     */
    @PostMapping
    public ResponseEntity<NotificacionDTO> create(@Valid @RequestBody NotificacionCreateDTO dto) {
        log.info("POST /api/notificaciones - usuarioId: {}", dto.getUsuarioId());
        NotificacionDTO created = notificacionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Marca una notificación como leída
     */
    @PutMapping("/{id}/leer")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("PUT /api/notificaciones/{}/leer - email: {}", id, email);
        
        Long usuarioId = usuarioService.findByEmail(email).getId();
        notificacionService.marcarComoLeida(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marca todas las notificaciones del usuario como leídas
     */
    @PutMapping("/leer-todas")
    public ResponseEntity<Void> marcarTodasComoLeidas() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("PUT /api/notificaciones/leer-todas - email: {}", email);
        
        Long usuarioId = usuarioService.findByEmail(email).getId();
        notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Elimina una notificación
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("DELETE /api/notificaciones/{} - email: {}", id, email);
        
        Long usuarioId = usuarioService.findByEmail(email).getId();
        notificacionService.delete(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}

