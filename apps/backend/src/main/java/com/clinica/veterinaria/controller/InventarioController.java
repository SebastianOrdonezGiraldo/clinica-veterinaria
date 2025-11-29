package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.MovimientoInventarioDTO;
import com.clinica.veterinaria.entity.MovimientoInventario;
import com.clinica.veterinaria.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestión de movimientos de inventario.
 * 
 * <p>Expone endpoints HTTP para la gestión completa de movimientos de inventario,
 * incluyendo entradas, salidas, ajustes y consultas de historial.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li><b>GET /api/inventario/movimientos/producto/{productoId}:</b> Movimientos de un producto</li>
 *   <li><b>GET /api/inventario/movimientos/tipo/{tipo}:</b> Movimientos por tipo</li>
 *   <li><b>GET /api/inventario/movimientos/fecha:</b> Movimientos en rango de fechas</li>
 *   <li><b>GET /api/inventario/movimientos/historial:</b> Historial de un producto en rango de fechas</li>
 *   <li><b>POST /api/inventario/movimientos/entrada:</b> Registra una entrada (compra)</li>
 *   <li><b>POST /api/inventario/movimientos/salida:</b> Registra una salida (venta/uso)</li>
 *   <li><b>POST /api/inventario/movimientos/ajuste:</b> Registra un ajuste (corrección)</li>
 * </ul>
 * 
 * <p><strong>Control de acceso:</strong> Todos los endpoints requieren autenticación.
 * Registro de movimientos requiere roles ADMIN, VET o RECEPCION.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see InventarioService
 * @see MovimientoInventarioDTO
 */
@RestController
@RequestMapping("/api/inventario/movimientos")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InventarioController {

    private final InventarioService inventarioService;

    /**
     * Obtiene todos los movimientos de un producto.
     */
    @GetMapping("/producto/{productoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<MovimientoInventarioDTO>> getByProducto(@PathVariable Long productoId) {
        log.info("GET /api/inventario/movimientos/producto/{}", productoId);
        return ResponseEntity.ok(inventarioService.findByProducto(productoId));
    }

    /**
     * Obtiene movimientos por tipo (ENTRADA, SALIDA, AJUSTE).
     */
    @GetMapping("/tipo/{tipo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<MovimientoInventarioDTO>> getByTipo(
            @PathVariable MovimientoInventario.TipoMovimiento tipo) {
        log.info("GET /api/inventario/movimientos/tipo/{}", tipo);
        return ResponseEntity.ok(inventarioService.findByTipo(tipo));
    }

    /**
     * Obtiene movimientos en un rango de fechas.
     */
    @GetMapping("/fecha")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<MovimientoInventarioDTO>> getByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        log.info("GET /api/inventario/movimientos/fecha?fechaInicio={}&fechaFin={}", fechaInicio, fechaFin);
        return ResponseEntity.ok(inventarioService.findByFechaRange(fechaInicio, fechaFin));
    }

    /**
     * Obtiene el historial de un producto en un rango de fechas.
     */
    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<MovimientoInventarioDTO>> getHistorial(
            @RequestParam Long productoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        log.info("GET /api/inventario/movimientos/historial?productoId={}&fechaInicio={}&fechaFin={}", 
            productoId, fechaInicio, fechaFin);
        return ResponseEntity.ok(inventarioService.obtenerHistorialProducto(productoId, fechaInicio, fechaFin));
    }

    /**
     * Registra una entrada de inventario (compra, recepción de mercancía).
     */
    @PostMapping("/entrada")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<MovimientoInventarioDTO> registrarEntrada(
            @Valid @RequestBody MovimientoInventarioDTO dto) {
        log.info("POST /api/inventario/movimientos/entrada");
        dto.setTipo(MovimientoInventario.TipoMovimiento.ENTRADA);
        MovimientoInventarioDTO created = inventarioService.registrarEntrada(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Registra una salida de inventario (venta, uso, pérdida).
     */
    @PostMapping("/salida")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<MovimientoInventarioDTO> registrarSalida(
            @Valid @RequestBody MovimientoInventarioDTO dto) {
        log.info("POST /api/inventario/movimientos/salida");
        dto.setTipo(MovimientoInventario.TipoMovimiento.SALIDA);
        MovimientoInventarioDTO created = inventarioService.registrarSalida(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Registra un ajuste de inventario (corrección, inventario físico).
     */
    @PostMapping("/ajuste")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<MovimientoInventarioDTO> registrarAjuste(
            @Valid @RequestBody MovimientoInventarioDTO dto) {
        log.info("POST /api/inventario/movimientos/ajuste");
        dto.setTipo(MovimientoInventario.TipoMovimiento.AJUSTE);
        MovimientoInventarioDTO created = inventarioService.registrarAjuste(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}

