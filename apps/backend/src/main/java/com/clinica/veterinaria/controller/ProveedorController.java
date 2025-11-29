package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.ProveedorDTO;
import com.clinica.veterinaria.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de proveedores.
 * 
 * <p>Expone endpoints HTTP para la gestión completa de proveedores,
 * incluyendo operaciones CRUD y búsquedas.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li><b>GET /api/inventario/proveedores:</b> Lista todos los proveedores activos</li>
 *   <li><b>GET /api/inventario/proveedores/all:</b> Lista todos los proveedores (activos e inactivos)</li>
 *   <li><b>GET /api/inventario/proveedores/{id}:</b> Obtiene un proveedor específico</li>
 *   <li><b>GET /api/inventario/proveedores/buscar:</b> Busca proveedores por nombre</li>
 *   <li><b>POST /api/inventario/proveedores:</b> Crea un nuevo proveedor</li>
 *   <li><b>PUT /api/inventario/proveedores/{id}:</b> Actualiza un proveedor</li>
 *   <li><b>DELETE /api/inventario/proveedores/{id}:</b> Desactiva un proveedor (soft delete)</li>
 * </ul>
 * 
 * <p><strong>Control de acceso:</strong> Todos los endpoints requieren autenticación.
 * Creación/Actualización/Eliminación requieren roles ADMIN o RECEPCION.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see ProveedorService
 * @see ProveedorDTO
 */
@RestController
@RequestMapping("/api/inventario/proveedores")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProveedorController {

    private final ProveedorService proveedorService;

    /**
     * Obtiene todos los proveedores activos.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<ProveedorDTO>> getAllActivos() {
        log.info("GET /api/inventario/proveedores");
        return ResponseEntity.ok(proveedorService.findAllActivos());
    }

    /**
     * Obtiene todos los proveedores (activos e inactivos).
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<List<ProveedorDTO>> getAll() {
        log.info("GET /api/inventario/proveedores/all");
        return ResponseEntity.ok(proveedorService.findAll());
    }

    /**
     * Obtiene un proveedor por su ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<ProveedorDTO> getById(@PathVariable Long id) {
        log.info("GET /api/inventario/proveedores/{}", id);
        return ResponseEntity.ok(proveedorService.findById(id));
    }

    /**
     * Busca proveedores por nombre.
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<ProveedorDTO>> buscar(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        log.info("GET /api/inventario/proveedores/buscar?nombre={}&soloActivos={}", nombre, soloActivos);
        return ResponseEntity.ok(proveedorService.buscarPorNombre(nombre, soloActivos));
    }

    /**
     * Crea un nuevo proveedor.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ProveedorDTO> create(@Valid @RequestBody ProveedorDTO dto) {
        log.info("POST /api/inventario/proveedores");
        ProveedorDTO created = proveedorService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un proveedor existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ProveedorDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProveedorDTO dto) {
        log.info("PUT /api/inventario/proveedores/{}", id);
        return ResponseEntity.ok(proveedorService.update(id, dto));
    }

    /**
     * Desactiva un proveedor (soft delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/inventario/proveedores/{}", id);
        proveedorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

