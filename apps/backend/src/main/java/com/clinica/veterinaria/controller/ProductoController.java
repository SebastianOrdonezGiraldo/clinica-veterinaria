package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.ProductoDTO;
import com.clinica.veterinaria.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador REST para gestión de productos del inventario.
 * 
 * <p>Expone endpoints HTTP para la gestión completa de productos,
 * incluyendo operaciones CRUD, búsquedas y reportes de stock.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li><b>GET /api/inventario/productos:</b> Lista todos los productos activos</li>
 *   <li><b>GET /api/inventario/productos/all:</b> Lista todos los productos (activos e inactivos)</li>
 *   <li><b>GET /api/inventario/productos/{id}:</b> Obtiene un producto específico</li>
 *   <li><b>GET /api/inventario/productos/codigo/{codigo}:</b> Busca por código</li>
 *   <li><b>GET /api/inventario/productos/buscar:</b> Busca por nombre o código</li>
 *   <li><b>GET /api/inventario/productos/categoria/{categoriaId}:</b> Filtra por categoría</li>
 *   <li><b>GET /api/inventario/productos/stock-bajo:</b> Productos con stock bajo</li>
 *   <li><b>GET /api/inventario/productos/sobrestock:</b> Productos con sobrestock</li>
 *   <li><b>GET /api/inventario/productos/valor-total:</b> Valor total del inventario</li>
 *   <li><b>POST /api/inventario/productos:</b> Crea un nuevo producto</li>
 *   <li><b>PUT /api/inventario/productos/{id}:</b> Actualiza un producto</li>
 *   <li><b>DELETE /api/inventario/productos/{id}:</b> Desactiva un producto (soft delete)</li>
 * </ul>
 * 
 * <p><strong>Control de acceso:</strong> Todos los endpoints requieren autenticación.
 * Creación/Actualización/Eliminación requieren roles ADMIN o RECEPCION.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see ProductoService
 * @see ProductoDTO
 */
@RestController
@RequestMapping("/api/inventario/productos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Obtiene todos los productos activos.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<ProductoDTO>> getAllActivos() {
        log.info("GET /api/inventario/productos");
        return ResponseEntity.ok(productoService.findAllActivos());
    }

    /**
     * Obtiene todos los productos (activos e inactivos).
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<List<ProductoDTO>> getAll() {
        log.info("GET /api/inventario/productos/all");
        return ResponseEntity.ok(productoService.findAll());
    }

    /**
     * Obtiene un producto por su ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<ProductoDTO> getById(@PathVariable Long id) {
        log.info("GET /api/inventario/productos/{}", id);
        return ResponseEntity.ok(productoService.findById(id));
    }

    /**
     * Busca un producto por su código único.
     */
    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<ProductoDTO> getByCodigo(@PathVariable String codigo) {
        log.info("GET /api/inventario/productos/codigo/{}", codigo);
        ProductoDTO producto = productoService.findByCodigo(codigo);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    /**
     * Busca productos por nombre o código.
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<ProductoDTO>> buscar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String codigo,
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        log.info("GET /api/inventario/productos/buscar?nombre={}&codigo={}&soloActivos={}", nombre, codigo, soloActivos);
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            return ResponseEntity.ok(productoService.buscarPorNombre(nombre, soloActivos));
        } else if (codigo != null && !codigo.trim().isEmpty()) {
            return ResponseEntity.ok(productoService.buscarPorCodigo(codigo, soloActivos));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene productos por categoría.
     */
    @GetMapping("/categoria/{categoriaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<ProductoDTO>> getByCategoria(
            @PathVariable Long categoriaId,
            @RequestParam(defaultValue = "true") boolean soloActivos) {
        log.info("GET /api/inventario/productos/categoria/{}?soloActivos={}", categoriaId, soloActivos);
        return ResponseEntity.ok(productoService.buscarPorCategoria(categoriaId, soloActivos));
    }

    /**
     * Obtiene productos con stock bajo.
     */
    @GetMapping("/stock-bajo")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<List<ProductoDTO>> getStockBajo() {
        log.info("GET /api/inventario/productos/stock-bajo");
        return ResponseEntity.ok(productoService.findProductosConStockBajo());
    }

    /**
     * Obtiene productos con sobrestock.
     */
    @GetMapping("/sobrestock")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<List<ProductoDTO>> getSobrestock() {
        log.info("GET /api/inventario/productos/sobrestock");
        return ResponseEntity.ok(productoService.findProductosConSobrestock());
    }

    /**
     * Calcula el valor total del inventario.
     */
    @GetMapping("/valor-total")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<BigDecimal> getValorTotal() {
        log.info("GET /api/inventario/productos/valor-total");
        return ResponseEntity.ok(productoService.calcularValorTotalInventario());
    }

    /**
     * Crea un nuevo producto.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ProductoDTO> create(@Valid @RequestBody ProductoDTO dto) {
        log.info("POST /api/inventario/productos");
        ProductoDTO created = productoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualiza un producto existente.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ProductoDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductoDTO dto) {
        log.info("PUT /api/inventario/productos/{}", id);
        return ResponseEntity.ok(productoService.update(id, dto));
    }

    /**
     * Desactiva un producto (soft delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/inventario/productos/{}", id);
        productoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

