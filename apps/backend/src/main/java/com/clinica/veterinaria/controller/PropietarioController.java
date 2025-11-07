package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.PropietarioDTO;
import com.clinica.veterinaria.service.PropietarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de propietarios de mascotas (clientes).
 * 
 * <p>Expone endpoints HTTP para la gestión completa de propietarios, incluyendo
 * operaciones CRUD, búsquedas por nombre, y soporte de paginación.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li><b>GET /api/propietarios:</b> Lista todos los propietarios (con paginación opcional)</li>
 *   <li><b>GET /api/propietarios/{id}:</b> Obtiene un propietario específico</li>
 *   <li><b>GET /api/propietarios/buscar:</b> Búsqueda por nombre</li>
 *   <li><b>POST /api/propietarios:</b> Registra un nuevo propietario</li>
 *   <li><b>PUT /api/propietarios/{id}:</b> Actualiza un propietario</li>
 *   <li><b>DELETE /api/propietarios/{id}:</b> Desactiva un propietario (soft delete)</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong> El documento de identidad debe ser único en el sistema.</p>
 * 
 * <p><strong>Control de acceso:</strong> Todos los endpoints requieren autenticación.
 * Creación/Actualización/Eliminación requieren roles ADMIN, RECEPCION o VET.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see PropietarioService
 * @see PropietarioDTO
 */
@RestController
@RequestMapping("/api/propietarios")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PropietarioController {

    private final PropietarioService propietarioService;

    /**
     * Obtener todos los propietarios
     */
    @GetMapping
    public ResponseEntity<List<PropietarioDTO>> getAll() {
        log.info("GET /api/propietarios");
        return ResponseEntity.ok(propietarioService.findAll());
    }

    /**
     * Obtener propietarios con paginación
     */
    @GetMapping("/page")
    public ResponseEntity<Page<PropietarioDTO>> getPage(Pageable pageable) {
        log.info("GET /api/propietarios/page");
        return ResponseEntity.ok(propietarioService.findAll(pageable));
    }

    /**
     * Obtener un propietario por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropietarioDTO> getById(@PathVariable Long id) {
        log.info("GET /api/propietarios/{}", id);
        return ResponseEntity.ok(propietarioService.findById(id));
    }

    /**
     * Buscar propietarios por nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<PropietarioDTO>> searchByNombre(@RequestParam String nombre) {
        log.info("GET /api/propietarios/buscar?nombre={}", nombre);
        return ResponseEntity.ok(propietarioService.findByNombre(nombre));
    }

    /**
     * Crear un nuevo propietario
     * ADMIN y RECEPCION
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<PropietarioDTO> create(@Valid @RequestBody PropietarioDTO dto) {
        log.info("POST /api/propietarios - nombre: {}", dto.getNombre());
        PropietarioDTO created = propietarioService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualizar un propietario
     * ADMIN y RECEPCION
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<PropietarioDTO> update(@PathVariable Long id, @Valid @RequestBody PropietarioDTO dto) {
        log.info("PUT /api/propietarios/{}", id);
        return ResponseEntity.ok(propietarioService.update(id, dto));
    }

    /**
     * Eliminar un propietario
     * Solo ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/propietarios/{}", id);
        propietarioService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

