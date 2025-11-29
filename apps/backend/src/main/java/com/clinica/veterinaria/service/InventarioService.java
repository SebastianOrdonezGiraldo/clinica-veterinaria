package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.MovimientoInventarioDTO;
import com.clinica.veterinaria.entity.MovimientoInventario;
import com.clinica.veterinaria.entity.Producto;
import com.clinica.veterinaria.entity.Proveedor;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.MovimientoInventarioRepository;
import com.clinica.veterinaria.repository.ProductoRepository;
import com.clinica.veterinaria.repository.ProveedorRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar movimientos de inventario.
 * 
 * <p>Este servicio es el núcleo del sistema de inventario. Gestiona todos los movimientos
 * (entradas, salidas y ajustes) y actualiza automáticamente el stock de los productos.
 * Mantiene un historial completo y auditable de todos los cambios en el inventario.</p>
 * 
 * <p><strong>Tipos de movimiento:</strong></p>
 * <ul>
 *   <li><b>ENTRADA:</b> Productos que ingresan al inventario (compras, donaciones). Aumenta el stock.</li>
 *   <li><b>SALIDA:</b> Productos que salen del inventario (ventas, uso, pérdidas). Disminuye el stock.</li>
 *   <li><b>AJUSTE:</b> Correcciones de inventario (inventario físico, correcciones). Puede aumentar o disminuir.</li>
 * </ul>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Registrar movimientos de inventario</li>
 *   <li>Actualizar stock automáticamente</li>
 *   <li>Validar stock suficiente para salidas</li>
 *   <li>Historial completo de movimientos</li>
 *   <li>Búsquedas por producto, tipo, fecha, usuario</li>
 *   <li>Auditoría completa de operaciones</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see MovimientoInventario
 * @see MovimientoInventarioDTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoService productoService;
    private final IAuditLogger auditLogger;

    /**
     * Obtiene todos los movimientos de un producto, ordenados por fecha descendente.
     * 
     * @param productoId ID del producto
     * @return Lista de movimientos del producto
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> findByProducto(@NonNull Long productoId) {
        log.debug("Buscando movimientos del producto: {}", productoId);
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productoId));
        
        return movimientoRepository.findByProductoOrderByFechaDesc(producto).stream()
            .map(MovimientoInventarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene movimientos por tipo, ordenados por fecha descendente.
     * 
     * @param tipo Tipo de movimiento (ENTRADA, SALIDA, AJUSTE)
     * @return Lista de movimientos del tipo especificado
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> findByTipo(MovimientoInventario.TipoMovimiento tipo) {
        log.debug("Buscando movimientos de tipo: {}", tipo);
        return movimientoRepository.findByTipoOrderByFechaDesc(tipo).stream()
            .map(MovimientoInventarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene movimientos en un rango de fechas.
     * 
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de movimientos en el rango
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> findByFechaRange(
            @NonNull LocalDateTime fechaInicio, 
            @NonNull LocalDateTime fechaFin) {
        log.debug("Buscando movimientos entre {} y {}", fechaInicio, fechaFin);
        return movimientoRepository.findByFechaBetween(fechaInicio, fechaFin).stream()
            .map(MovimientoInventarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Registra un movimiento de entrada (compra, recepción de mercancía).
     * 
     * <p>Este método aumenta el stock del producto y registra el movimiento.
     * Requiere un proveedor para entradas.</p>
     * 
     * @param dto DTO con los datos del movimiento
     * @return DTO del movimiento registrado
     * @throws ResourceNotFoundException si el producto, usuario o proveedor no existen
     */
    public MovimientoInventarioDTO registrarEntrada(@NonNull MovimientoInventarioDTO dto) {
        log.info("→ Registrando entrada de inventario para producto: {}", dto.getProductoId());

        // Validar que el tipo sea ENTRADA
        if (dto.getTipo() != MovimientoInventario.TipoMovimiento.ENTRADA) {
            throw new BusinessException("Este método solo puede registrar movimientos de tipo ENTRADA");
        }

        // Validar proveedor (requerido para entradas)
        if (dto.getProveedorId() == null) {
            throw new BusinessException("El proveedor es requerido para movimientos de entrada");
        }

        return registrarMovimiento(dto);
    }

    /**
     * Registra un movimiento de salida (venta, uso, pérdida).
     * 
     * <p>Este método disminuye el stock del producto y registra el movimiento.
     * Valida que haya stock suficiente antes de realizar la salida.</p>
     * 
     * @param dto DTO con los datos del movimiento
     * @return DTO del movimiento registrado
     * @throws ResourceNotFoundException si el producto o usuario no existen
     * @throws BusinessException si no hay stock suficiente
     */
    public MovimientoInventarioDTO registrarSalida(@NonNull MovimientoInventarioDTO dto) {
        log.info("→ Registrando salida de inventario para producto: {}", dto.getProductoId());

        // Validar que el tipo sea SALIDA
        if (dto.getTipo() != MovimientoInventario.TipoMovimiento.SALIDA) {
            throw new BusinessException("Este método solo puede registrar movimientos de tipo SALIDA");
        }

        // Validar stock suficiente
        Producto producto = productoRepository.findById(dto.getProductoId())
            .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", dto.getProductoId()));

        if (producto.getStockActual().compareTo(dto.getCantidad()) < 0) {
            log.warn("✗ Stock insuficiente. Stock actual: {}, Cantidad solicitada: {}", 
                producto.getStockActual(), dto.getCantidad());
            throw new BusinessException(
                String.format("Stock insuficiente. Stock actual: %s %s, Cantidad solicitada: %s %s",
                    producto.getStockActual(), producto.getUnidadMedida(),
                    dto.getCantidad(), producto.getUnidadMedida()));
        }

        return registrarMovimiento(dto);
    }

    /**
     * Registra un ajuste de inventario (corrección, inventario físico).
     * 
     * <p>Este método ajusta el stock del producto al valor especificado.
     * Útil para correcciones de inventario físico o corrección de errores.</p>
     * 
     * @param dto DTO con los datos del movimiento
     * @return DTO del movimiento registrado
     * @throws ResourceNotFoundException si el producto o usuario no existen
     */
    public MovimientoInventarioDTO registrarAjuste(@NonNull MovimientoInventarioDTO dto) {
        log.info("→ Registrando ajuste de inventario para producto: {}", dto.getProductoId());

        // Validar que el tipo sea AJUSTE
        if (dto.getTipo() != MovimientoInventario.TipoMovimiento.AJUSTE) {
            throw new BusinessException("Este método solo puede registrar movimientos de tipo AJUSTE");
        }

        return registrarMovimiento(dto);
    }

    /**
     * Método interno que registra cualquier tipo de movimiento y actualiza el stock.
     * 
     * @param dto DTO con los datos del movimiento
     * @return DTO del movimiento registrado
     */
    private MovimientoInventarioDTO registrarMovimiento(@NonNull MovimientoInventarioDTO dto) {
        // Validar y obtener entidades relacionadas
        Producto producto = productoRepository.findById(dto.getProductoId())
            .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", dto.getProductoId()));

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", dto.getUsuarioId()));

        Proveedor proveedor = null;
        if (dto.getProveedorId() != null) {
            proveedor = proveedorRepository.findById(dto.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", dto.getProveedorId()));
        }

        // Validar que el proveedor solo se use en entradas
        if (dto.getProveedorId() != null && dto.getTipo() != MovimientoInventario.TipoMovimiento.ENTRADA) {
            throw new BusinessException("El proveedor solo puede especificarse en movimientos de tipo ENTRADA");
        }

        // Obtener stock actual
        BigDecimal stockAnterior = producto.getStockActual();
        BigDecimal stockResultante;

        // Calcular nuevo stock según el tipo de movimiento
        switch (dto.getTipo()) {
            case ENTRADA:
                stockResultante = stockAnterior.add(dto.getCantidad());
                break;
            case SALIDA:
                stockResultante = stockAnterior.subtract(dto.getCantidad());
                // Validación adicional de stock (por si acaso)
                if (stockResultante.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException(
                        String.format("No se puede realizar la salida. Stock resultante sería negativo: %s",
                            stockResultante));
                }
                break;
            case AJUSTE:
                // Para ajustes, la cantidad representa el stock final deseado
                // Calculamos la diferencia
                BigDecimal diferencia = dto.getCantidad().subtract(stockAnterior);
                stockResultante = dto.getCantidad();
                // Actualizamos la cantidad en el DTO para reflejar la diferencia real
                dto.setCantidad(diferencia.abs());
                break;
            default:
                throw new BusinessException("Tipo de movimiento no válido: " + dto.getTipo());
        }

        // Crear y guardar el movimiento
        MovimientoInventario movimiento = MovimientoInventario.builder()
            .producto(producto)
            .tipo(dto.getTipo())
            .cantidad(dto.getCantidad())
            .precioUnitario(dto.getPrecioUnitario())
            .motivo(dto.getMotivo())
            .usuario(usuario)
            .proveedor(proveedor)
            .stockAnterior(stockAnterior)
            .stockResultante(stockResultante)
            .notas(dto.getNotas())
            .build();

        movimiento = movimientoRepository.save(movimiento);

        // Actualizar stock del producto
        productoService.actualizarStock(producto.getId(), stockResultante);

        log.info("✓ Movimiento registrado exitosamente. Stock anterior: {}, Stock resultante: {}", 
            stockAnterior, stockResultante);

        auditLogger.logCreate("MovimientoInventario", movimiento.getId(),
            String.format("Producto: %s, Tipo: %s, Cantidad: %s, Stock: %s -> %s",
                producto.getNombre(), dto.getTipo(), dto.getCantidad(), stockAnterior, stockResultante));

        return MovimientoInventarioDTO.fromEntity(movimiento);
    }

    /**
     * Obtiene el historial completo de movimientos de un producto en un rango de fechas.
     * 
     * @param productoId ID del producto
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Lista de movimientos en el rango
     */
    @Transactional(readOnly = true)
    public List<MovimientoInventarioDTO> obtenerHistorialProducto(
            @NonNull Long productoId,
            @NonNull LocalDateTime fechaInicio,
            @NonNull LocalDateTime fechaFin) {
        log.debug("Obteniendo historial del producto {} entre {} y {}", productoId, fechaInicio, fechaFin);
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", productoId));
        
        return movimientoRepository.findByProductoAndFechaBetween(producto, fechaInicio, fechaFin).stream()
            .map(MovimientoInventarioDTO::fromEntity)
            .collect(Collectors.toList());
    }
}

