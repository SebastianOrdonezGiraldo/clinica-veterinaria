package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ProductoDTO;
import com.clinica.veterinaria.entity.CategoriaProducto;
import com.clinica.veterinaria.entity.Producto;
import com.clinica.veterinaria.exception.domain.DuplicateResourceException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.CategoriaProductoRepository;
import com.clinica.veterinaria.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar productos del inventario.
 * 
 * <p>Este servicio centraliza toda la lógica de negocio relacionada con los productos
 * del inventario. Proporciona operaciones CRUD completas, búsquedas, control de stock
 * y funcionalidades de auditoría.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Crear, actualizar y eliminar (soft delete) productos</li>
 *   <li>Búsqueda por código, nombre, categoría</li>
 *   <li>Listado de productos activos</li>
 *   <li>Detección de stock bajo y sobrestock</li>
 *   <li>Validación de duplicados</li>
 *   <li>Auditoría completa de operaciones</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see Producto
 * @see ProductoDTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaProductoRepository categoriaRepository;
    private final IAuditLogger auditLogger;

    /**
     * Obtiene todos los productos activos, ordenados por nombre.
     * 
     * @return Lista de productos activos
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO> findAllActivos() {
        log.debug("Obteniendo todos los productos activos");
        return productoRepository.findByActivoTrueOrderByNombreAsc().stream()
            .map(ProductoDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los productos (activos e inactivos).
     * 
     * @return Lista completa de productos
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO> findAll() {
        log.debug("Obteniendo todos los productos");
        return productoRepository.findAll().stream()
            .map(ProductoDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Busca un producto por su ID.
     * 
     * @param id ID del producto
     * @return DTO del producto
     * @throws ResourceNotFoundException si no existe el producto
     */
    @Transactional(readOnly = true)
    public ProductoDTO findById(@NonNull Long id) {
        log.debug("Buscando producto con ID: {}", id);
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
        return ProductoDTO.fromEntity(producto);
    }

    /**
     * Busca un producto por su código único.
     * 
     * @param codigo Código del producto
     * @return DTO del producto o null si no existe
     */
    @Transactional(readOnly = true)
    public ProductoDTO findByCodigo(String codigo) {
        log.debug("Buscando producto con código: {}", codigo);
        return productoRepository.findByCodigo(codigo)
            .map(ProductoDTO::fromEntity)
            .orElse(null);
    }

    /**
     * Busca productos por nombre (búsqueda parcial, case-insensitive).
     * 
     * @param nombre Texto a buscar en el nombre
     * @param soloActivos Si es true, solo busca productos activos
     * @return Lista de productos que coinciden
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorNombre(String nombre, boolean soloActivos) {
        log.debug("Buscando productos con nombre: {}", nombre);
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoOrderByNombreAsc(nombre, soloActivos)
            .stream()
            .map(ProductoDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Busca productos por código (búsqueda parcial, case-insensitive).
     * 
     * @param codigo Texto a buscar en el código
     * @param soloActivos Si es true, solo busca productos activos
     * @return Lista de productos que coinciden
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorCodigo(String codigo, boolean soloActivos) {
        log.debug("Buscando productos con código: {}", codigo);
        return productoRepository.findByCodigoContainingIgnoreCaseAndActivoOrderByNombreAsc(codigo, soloActivos)
            .stream()
            .map(ProductoDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Busca productos por categoría.
     * 
     * @param categoriaId ID de la categoría
     * @param soloActivos Si es true, solo busca productos activos
     * @return Lista de productos de la categoría
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO> buscarPorCategoria(Long categoriaId, boolean soloActivos) {
        log.debug("Buscando productos de categoría: {}", categoriaId);
        CategoriaProducto categoria = categoriaRepository.findById(categoriaId)
            .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto", "id", categoriaId));
        
        return productoRepository.findByCategoriaAndActivoOrderByNombreAsc(categoria, soloActivos)
            .stream()
            .map(ProductoDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene productos con stock bajo (stock actual <= stock mínimo).
     * 
     * @return Lista de productos con stock bajo
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO> findProductosConStockBajo() {
        log.debug("Buscando productos con stock bajo");
        return productoRepository.findProductosConStockBajo().stream()
            .map(ProductoDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene productos con sobrestock (stock actual > stock máximo).
     * 
     * @return Lista de productos con sobrestock
     */
    @Transactional(readOnly = true)
    public List<ProductoDTO> findProductosConSobrestock() {
        log.debug("Buscando productos con sobrestock");
        return productoRepository.findProductosConSobrestock().stream()
            .map(ProductoDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Calcula el valor total del inventario (suma de costo * stockActual).
     * 
     * @return Valor total del inventario
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularValorTotalInventario() {
        log.debug("Calculando valor total del inventario");
        return productoRepository.calcularValorTotalInventario();
    }

    /**
     * Crea un nuevo producto.
     * 
     * @param dto DTO con los datos del producto
     * @return DTO del producto creado
     * @throws ResourceNotFoundException si la categoría no existe
     * @throws DuplicateResourceException si ya existe un producto con ese código
     */
    public ProductoDTO create(@NonNull ProductoDTO dto) {
        log.info("→ Creando nuevo producto: {}", dto.getNombre());

        // Validar que no exista un producto con el mismo código
        if (productoRepository.existsByCodigo(dto.getCodigo())) {
            log.warn("✗ Producto con código duplicado: {}", dto.getCodigo());
            throw new DuplicateResourceException("Producto", "codigo", dto.getCodigo());
        }

        // Validar que la categoría exista
        CategoriaProducto categoria = categoriaRepository.findById(dto.getCategoriaId())
            .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto", "id", dto.getCategoriaId()));

        Producto producto = Producto.builder()
            .nombre(dto.getNombre())
            .codigo(dto.getCodigo())
            .descripcion(dto.getDescripcion())
            .categoria(categoria)
            .unidadMedida(dto.getUnidadMedida() != null ? dto.getUnidadMedida() : "unidad")
            .stockActual(dto.getStockActual() != null ? dto.getStockActual() : BigDecimal.ZERO)
            .stockMinimo(dto.getStockMinimo())
            .stockMaximo(dto.getStockMaximo())
            .costo(dto.getCosto() != null ? dto.getCosto() : BigDecimal.ZERO)
            .precioVenta(dto.getPrecioVenta())
            .activo(true)
            .build();

        producto = productoRepository.save(producto);
        log.info("✓ Producto creado exitosamente con ID: {}", producto.getId());

        auditLogger.logCreate("Producto", producto.getId(), 
            String.format("Nombre: %s, Código: %s, Categoría: %s", 
                producto.getNombre(), producto.getCodigo(), categoria.getNombre()));
        return ProductoDTO.fromEntity(producto);
    }

    /**
     * Actualiza un producto existente.
     * 
     * @param id ID del producto a actualizar
     * @param dto DTO con los nuevos datos
     * @return DTO del producto actualizado
     * @throws ResourceNotFoundException si no existe el producto o la categoría
     * @throws DuplicateResourceException si el nuevo código ya existe en otro producto
     */
    public ProductoDTO update(@NonNull Long id, @NonNull ProductoDTO dto) {
        log.info("→ Actualizando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        // Validar que el nuevo código no esté en uso por otro producto
        if (!producto.getCodigo().equals(dto.getCodigo()) &&
            productoRepository.existsByCodigo(dto.getCodigo())) {
            log.warn("✗ Código de producto duplicado: {}", dto.getCodigo());
            throw new DuplicateResourceException("Producto", "codigo", dto.getCodigo());
        }

        // Validar que la categoría exista (si cambió)
        CategoriaProducto categoria = producto.getCategoria();
        if (!producto.getCategoria().getId().equals(dto.getCategoriaId())) {
            categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("CategoriaProducto", "id", dto.getCategoriaId()));
        }

        String oldData = String.format("Nombre: %s, Código: %s, Stock: %s", 
            producto.getNombre(), producto.getCodigo(), producto.getStockActual());

        producto.setNombre(dto.getNombre());
        producto.setCodigo(dto.getCodigo());
        producto.setDescripcion(dto.getDescripcion());
        producto.setCategoria(categoria);
        producto.setUnidadMedida(dto.getUnidadMedida());
        // No actualizamos stockActual aquí - se actualiza mediante movimientos
        producto.setStockMinimo(dto.getStockMinimo());
        producto.setStockMaximo(dto.getStockMaximo());
        producto.setCosto(dto.getCosto());
        producto.setPrecioVenta(dto.getPrecioVenta());
        if (dto.getActivo() != null) {
            producto.setActivo(dto.getActivo());
        }

        producto = productoRepository.save(producto);
        log.info("✓ Producto actualizado exitosamente");

        String newData = String.format("Nombre: %s, Código: %s, Stock: %s", 
            producto.getNombre(), producto.getCodigo(), producto.getStockActual());
        auditLogger.logUpdate("Producto", producto.getId(), oldData, newData);
        return ProductoDTO.fromEntity(producto);
    }

    /**
     * Desactiva un producto (soft delete).
     * 
     * @param id ID del producto a desactivar
     * @throws ResourceNotFoundException si no existe el producto
     */
    public void delete(@NonNull Long id) {
        log.info("→ Desactivando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));

        producto.setActivo(false);
        productoRepository.save(producto);
        log.info("✓ Producto desactivado exitosamente");

        auditLogger.logDelete("Producto", id);
    }

    /**
     * Actualiza el stock de un producto (usado internamente por InventarioService).
     * 
     * @param productoId ID del producto
     * @param nuevoStock Nuevo valor de stock
     */
    void actualizarStock(@NonNull Long productoId, @NonNull BigDecimal nuevoStock) {
        log.debug("Actualizando stock del producto {} a {}", productoId, nuevoStock);
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productoId));
        producto.setStockActual(nuevoStock);
        productoRepository.save(producto);
    }
}

