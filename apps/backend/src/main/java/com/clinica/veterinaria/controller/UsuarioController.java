package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.UsuarioCreateDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<UsuarioDTO> update(@PathVariable Long id, @Valid @RequestBody UsuarioCreateDTO dto) {
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
}

