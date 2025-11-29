package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.CategoriaProductoDTO;
import com.clinica.veterinaria.entity.CategoriaProducto;
import com.clinica.veterinaria.exception.domain.DuplicateResourceException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.CategoriaProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar categorías de productos del inventario.
 * 
 * <p>Este servicio centraliza toda la lógica de negocio relacionada con las categorías
 * de productos. Proporciona operaciones CRUD completas y funcionalidades de auditoría.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Crear, actualizar y eliminar (soft delete) categorías</li>
 *   <li>Búsqueda por nombre (case-insensitive)</li>
 *   <li>Listado de categorías activas</li>
 *   <li>Validación de duplicados</li>
 *   <li>Auditoría completa de operaciones</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see CategoriaProducto
 * @see CategoriaProductoDTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoriaProductoService {

    private final CategoriaProductoRepository categoriaRepository;
    private final IAuditLogger auditLogger;

    /**
     * Obtiene todas las categorías activas, ordenadas por nombre.
     * 
     * @return Lista de categorías activas
     */
    @Transactional(readOnly = true)
    public List<CategoriaProductoDTO> findAllActivas() {
        log.debug("Obteniendo todas las categorías activas");
        return categoriaRepository.findByActivoTrueOrderByNombreAsc().stream()
            .map(CategoriaProductoDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las categorías (activas e inactivas).
     * 
     * @return Lista completa de categorías
     */
    @Transactional(readOnly = true)
    public List<CategoriaProductoDTO> findAll() {
        log.debug("Obteniendo todas las categorías");
        return categoriaRepository.findAll().stream()
            .map(CategoriaProductoDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Busca una categoría por su ID.
     * 
     * @param id ID de la categoría
     * @return DTO de la categoría
     * @throws ResourceNotFoundException si no existe la categoría
     */
    @Transactional(readOnly = true)
    public CategoriaProductoDTO findById(@NonNull Long id) {
        log.debug("Buscando categoría con ID: {}", id);
        CategoriaProducto categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto", "id", id));
        return CategoriaProductoDTO.fromEntity(categoria);
    }

    /**
     * Busca una categoría por su nombre (case-insensitive).
     * 
     * @param nombre Nombre de la categoría
     * @return DTO de la categoría o null si no existe
     */
    @Transactional(readOnly = true)
    public CategoriaProductoDTO findByNombre(String nombre) {
        log.debug("Buscando categoría con nombre: {}", nombre);
        return categoriaRepository.findByNombreIgnoreCase(nombre)
            .map(CategoriaProductoDTO::fromEntity)
            .orElse(null);
    }

    /**
     * Crea una nueva categoría de producto.
     * 
     * @param dto DTO con los datos de la categoría
     * @return DTO de la categoría creada
     * @throws DuplicateResourceException si ya existe una categoría con ese nombre
     */
    public CategoriaProductoDTO create(@NonNull CategoriaProductoDTO dto) {
        log.info("→ Creando nueva categoría: {}", dto.getNombre());

        // Validar que no exista una categoría con el mismo nombre
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            log.warn("✗ Categoría duplicada: {}", dto.getNombre());
            throw new DuplicateResourceException("CategoriaProducto", "nombre", dto.getNombre());
        }

        CategoriaProducto categoria = dto.toEntity();
        categoria = categoriaRepository.save(categoria);
        log.info("✓ Categoría creada exitosamente con ID: {}", categoria.getId());

        auditLogger.logCreate("CategoriaProducto", categoria.getId(), categoria.getNombre());
        return CategoriaProductoDTO.fromEntity(categoria);
    }

    /**
     * Actualiza una categoría existente.
     * 
     * @param id ID de la categoría a actualizar
     * @param dto DTO con los nuevos datos
     * @return DTO de la categoría actualizada
     * @throws ResourceNotFoundException si no existe la categoría
     * @throws DuplicateResourceException si el nuevo nombre ya existe en otra categoría
     */
    public CategoriaProductoDTO update(@NonNull Long id, @NonNull CategoriaProductoDTO dto) {
        log.info("→ Actualizando categoría con ID: {}", id);

        CategoriaProducto categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto", "id", id));

        // Validar que el nuevo nombre no esté en uso por otra categoría
        if (!categoria.getNombre().equalsIgnoreCase(dto.getNombre()) &&
            categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            log.warn("✗ Nombre de categoría duplicado: {}", dto.getNombre());
            throw new DuplicateResourceException("CategoriaProducto", "nombre", dto.getNombre());
        }

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            categoria.setActivo(dto.getActivo());
        }

        String oldData = String.format("Nombre: %s", categoria.getNombre());
        categoria = categoriaRepository.save(categoria);
        String newData = String.format("Nombre: %s", categoria.getNombre());
        log.info("✓ Categoría actualizada exitosamente");

        auditLogger.logUpdate("CategoriaProducto", categoria.getId(), oldData, newData);
        return CategoriaProductoDTO.fromEntity(categoria);
    }

    /**
     * Desactiva una categoría (soft delete).
     * 
     * @param id ID de la categoría a desactivar
     * @throws ResourceNotFoundException si no existe la categoría
     */
    public void delete(@NonNull Long id) {
        log.info("→ Desactivando categoría con ID: {}", id);

        CategoriaProducto categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto", "id", id));

        // Verificar que no tenga productos activos asociados
        long productosActivos = categoriaRepository.countProductosActivosByCategoriaId(id);
        if (productosActivos > 0) {
            log.warn("✗ No se puede desactivar categoría con {} productos activos", productosActivos);
            throw new com.clinica.veterinaria.exception.domain.BusinessException(
                "No se puede desactivar la categoría porque tiene " + productosActivos + " productos activos asociados."
            );
        }

        categoria.setActivo(false);
        categoriaRepository.save(categoria);
        log.info("✓ Categoría desactivada exitosamente");

        auditLogger.logDelete("CategoriaProducto", categoria.getId());
    }
}

