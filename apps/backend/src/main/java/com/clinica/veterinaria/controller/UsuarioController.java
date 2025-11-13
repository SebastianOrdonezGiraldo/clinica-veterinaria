package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.ResetPasswordDTO;
import com.clinica.veterinaria.dto.UsuarioCreateDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.dto.UsuarioUpdateDTO;
import com.clinica.veterinaria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de usuarios del sistema.
 * 
 * <p>Expone endpoints HTTP para la gestión completa de usuarios (veterinarios,
 * administradores, recepcionistas). Incluye operaciones CRUD y consultas especializadas.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li><b>GET /api/usuarios:</b> Lista todos los usuarios (Solo ADMIN)</li>
 *   <li><b>GET /api/usuarios/{id}:</b> Obtiene un usuario específico</li>
 *   <li><b>GET /api/usuarios/email/{email}:</b> Busca por email</li>
 *   <li><b>GET /api/usuarios/veterinarios:</b> Lista veterinarios activos</li>
 *   <li><b>POST /api/usuarios:</b> Crea un nuevo usuario (Solo ADMIN)</li>
 *   <li><b>PUT /api/usuarios/{id}:</b> Actualiza un usuario (Solo ADMIN)</li>
 *   <li><b>DELETE /api/usuarios/{id}:</b> Desactiva un usuario (Solo ADMIN)</li>
 * </ul>
 * 
 * <p><strong>Seguridad:</strong></p>
 * <ul>
 *   <li>Todos los endpoints requieren autenticación</li>
 *   <li>Operaciones de escritura (POST, PUT, DELETE) requieren rol ADMIN</li>
 *   <li>Las contraseñas nunca se exponen en las respuestas</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see UsuarioService
 * @see UsuarioDTO
 * @see UsuarioCreateDTO
 */
@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    /**
     * Obtener todos los usuarios
     * Solo ADMIN
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> getAll() {
        log.info("GET /api/usuarios");
        return ResponseEntity.ok(usuarioService.findAll());
    }

    /**
     * Obtener un usuario por ID
     * Solo ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> getById(@PathVariable Long id) {
        log.info("GET /api/usuarios/{}", id);
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    /**
     * Buscar un usuario por email
     * Solo ADMIN
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> getByEmail(@PathVariable String email) {
        log.info("GET /api/usuarios/email/{}", email);
        return ResponseEntity.ok(usuarioService.findByEmail(email));
    }

    /**
     * Crear un nuevo usuario
     * Solo ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> create(@Valid @RequestBody UsuarioCreateDTO dto) {
        log.info("POST /api/usuarios - email: {}", dto.getEmail());
        UsuarioDTO created = usuarioService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualizar un usuario
     * Solo ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDTO> update(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO dto) {
        log.info("PUT /api/usuarios/{}", id);
        return ResponseEntity.ok(usuarioService.update(id, dto));
    }

    /**
     * Eliminar un usuario (soft delete)
     * Solo ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/usuarios/{}", id);
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtener veterinarios activos
     * Accesible para todos los usuarios autenticados
     */
    @GetMapping("/veterinarios")
    public ResponseEntity<List<UsuarioDTO>> getVeterinarios() {
        log.info("GET /api/usuarios/veterinarios");
        return ResponseEntity.ok(usuarioService.findVeterinariosActivos());
    }

    /**
     * Resetear la contraseña de un usuario
     * Solo ADMIN
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordDTO dto) {
        log.info("POST /api/usuarios/{}/reset-password", id);
        usuarioService.resetPassword(id, dto.getPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Actualizar el perfil del usuario autenticado
     * Cualquier usuario autenticado puede actualizar su propio perfil
     * No permite cambiar el rol ni el estado activo
     */
    @PutMapping("/me")
    public ResponseEntity<UsuarioDTO> updateMyProfile(@Valid @RequestBody UsuarioUpdateDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("PUT /api/usuarios/me - email: {}", email);
        
        // Obtener el usuario actual por email
        UsuarioDTO currentUser = usuarioService.findByEmail(email);
        
        // Crear un DTO que solo permita cambiar nombre, email y password
        // No permitir cambiar rol ni estado desde el perfil
        UsuarioUpdateDTO updateDTO = UsuarioUpdateDTO.builder()
            .nombre(dto.getNombre())
            .email(dto.getEmail())
            .password(dto.getPassword())
            .rol(currentUser.getRol()) // Mantener el rol actual
            .activo(currentUser.getActivo()) // Mantener el estado actual
            .build();
        
        UsuarioDTO updated = usuarioService.update(currentUser.getId(), updateDTO);
        return ResponseEntity.ok(updated);
    }
}

