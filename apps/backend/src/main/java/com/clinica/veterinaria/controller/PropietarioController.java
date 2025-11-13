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
     * 
     * @deprecated Usar {@link #searchWithFilters} en su lugar para búsquedas paginadas con filtros
     */
    @Deprecated
    @GetMapping("/page")
    public ResponseEntity<Page<PropietarioDTO>> getPage(Pageable pageable) {
        log.info("GET /api/propietarios/page (DEPRECATED)");
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
     * 
     * @deprecated Usar {@link #searchWithFilters} en su lugar para búsquedas paginadas
     */
    @Deprecated
    @GetMapping("/buscar")
    public ResponseEntity<List<PropietarioDTO>> searchByNombre(@RequestParam String nombre) {
        log.info("GET /api/propietarios/buscar?nombre={} (DEPRECATED)", nombre);
        return ResponseEntity.ok(propietarioService.findByNombre(nombre));
    }
    
    /**
     * Endpoint de búsqueda avanzada con filtros combinados y paginación del lado del servidor.
     * 
     * <p>Este endpoint implementa <strong>búsqueda multicritero</strong> permitiendo
     * combinar diferentes filtros. La paginación se realiza en la base de datos para
     * manejar eficientemente grandes volúmenes de datos.</p>
     * 
     * <p><strong>Parámetros de búsqueda (todos opcionales):</strong></p>
     * <ul>
     *   <li><b>nombre:</b> Búsqueda parcial en nombre (case-insensitive)</li>
     *   <li><b>documento:</b> Búsqueda parcial en documento de identidad</li>
     *   <li><b>telefono:</b> Búsqueda parcial en número de teléfono</li>
     * </ul>
     * 
     * <p><strong>Parámetros de paginación (Spring Data):</strong></p>
     * <ul>
     *   <li><b>page:</b> Número de página (0-indexed, default: 0)</li>
     *   <li><b>size:</b> Elementos por página (default: 20)</li>
     *   <li><b>sort:</b> Ordenamiento, ej: "nombre,asc" o "id,desc"</li>
     * </ul>
     * 
     * <p><strong>Ejemplos de uso:</strong></p>
     * <pre>
     * // Caso 1: Buscar por nombre con paginación
     * GET /api/propietarios/search?nombre=Juan&page=0&size=20&sort=nombre,asc
     * 
     * // Caso 2: Buscar por documento
     * GET /api/propietarios/search?documento=12345&page=0&size=10
     * 
     * // Caso 3: Buscar por teléfono
     * GET /api/propietarios/search?telefono=555&page=0&size=15
     * 
     * // Caso 4: Búsqueda combinada (nombre + documento)
     * GET /api/propietarios/search?nombre=Juan&documento=12345&page=0&size=20
     * 
     * // Caso 5: Listar todos con paginación (sin filtros)
     * GET /api/propietarios/search?page=0&size=20&sort=nombre,asc
     * </pre>
     * 
     * <p><strong>Formato de respuesta:</strong> Objeto Page de Spring con:</p>
     * <ul>
     *   <li><b>content:</b> Array de PropietarioDTO</li>
     *   <li><b>totalElements:</b> Total de registros que cumplen los filtros</li>
     *   <li><b>totalPages:</b> Total de páginas</li>
     *   <li><b>size:</b> Tamaño de página solicitado</li>
     *   <li><b>number:</b> Número de página actual (0-indexed)</li>
     * </ul>
     * 
     * @param nombre Filtro opcional de nombre (búsqueda parcial)
     * @param documento Filtro opcional de documento (búsqueda parcial)
     * @param telefono Filtro opcional de teléfono (búsqueda parcial)
     * @param pageable Parámetros automáticos de paginación y orden de Spring
     * @return Page con propietarios que cumplen los criterios de búsqueda
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PropietarioDTO>> searchWithFilters(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String documento,
            @RequestParam(required = false) String telefono,
            Pageable pageable) {
        
        log.info("GET /api/propietarios/search - nombre: {}, documento: {}, telefono: {}, page: {}, size: {}", 
            nombre, documento, telefono, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<PropietarioDTO> result = propietarioService.searchWithFilters(
            nombre, documento, telefono, pageable);
        
        log.info("✓ Encontrados {} propietarios | Página {}/{} | Total: {}", 
            result.getNumberOfElements(), 
            result.getNumber() + 1, 
            result.getTotalPages(), 
            result.getTotalElements());
        
        return ResponseEntity.ok(result);
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

