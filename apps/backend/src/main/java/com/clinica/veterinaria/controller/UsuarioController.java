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
 * Controlador REST para gestión de usuarios
 * Requiere autenticación para todos los endpoints
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

