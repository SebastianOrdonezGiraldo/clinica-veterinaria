package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.CategoriaProductoDTO;
import com.clinica.veterinaria.service.CategoriaProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de categorías de productos.
 * 
 * <p>Expone endpoints HTTP para la gestión completa de categorías de productos,
 * incluyendo operaciones CRUD y búsquedas.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li><b>GET /api/inventario/categorias:</b> Lista todas las categorías activas</li>
 *   <li><b>GET /api/inventario/categorias/all:</b> Lista todas las categorías (activas e inactivas)</li>
 *   <li><b>GET /api/inventario/categorias/{id}:</b> Obtiene una categoría específica</li>
 *   <li><b>POST /api/inventario/categorias:</b> Crea una nueva categoría</li>
 *   <li><b>PUT /api/inventario/categorias/{id}:</b> Actualiza una categoría</li>
 *   <li><b>DELETE /api/inventario/categorias/{id}:</b> Desactiva una categoría (soft delete)</li>
 * </ul>
 * 
 * <p><strong>Control de acceso:</strong> Todos los endpoints requieren autenticación.
 * Creación/Actualización/Eliminación requieren roles ADMIN o RECEPCION.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see CategoriaProductoService
 * @see CategoriaProductoDTO
 */
@RestController
@RequestMapping("/api/inventario/categorias")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CategoriaProductoController {

    private final CategoriaProductoService categoriaService;

    /**
     * Obtiene todas las categorías activas.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<CategoriaProductoDTO>> getAllActivas() {
        log.info("GET /api/inventario/categorias");
        return ResponseEntity.ok(categoriaService.findAllActivas());
    }

    /**
     * Obtiene todas las categorías (activas e inactivas).
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<List<CategoriaProductoDTO>> getAll() {
        log.info("GET /api/inventario/categorias/all");
        return ResponseEntity.ok(categoriaService.findAll());
    }

    /**
     * Obtiene una categoría por su ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<CategoriaProductoDTO> getById(@PathVariable Long id) {
        log.info("GET /api/inventario/categorias/{}", id);
        return ResponseEntity.ok(categoriaService.findById(id));
    }

    /**
     * Crea una nueva categoría.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<CategoriaProductoDTO> create(@Valid @RequestBody CategoriaProductoDTO dto) {
        log.info("POST /api/inventario/categorias");
        CategoriaProductoDTO created = categoriaService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza una categoría existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<CategoriaProductoDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaProductoDTO dto) {
        log.info("PUT /api/inventario/categorias/{}", id);
        return ResponseEntity.ok(categoriaService.update(id, dto));
    }

    /**
     * Desactiva una categoría (soft delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/inventario/categorias/{}", id);
        categoriaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

