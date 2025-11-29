package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un movimiento de inventario (entrada, salida o ajuste).
 * 
 * <p>Esta entidad registra todos los movimientos de productos en el inventario:
 * compras (entradas), ventas/uso (salidas) y correcciones (ajustes). Mantiene
 * un historial completo y auditable de todos los cambios en el stock.</p>
 * 
 * <p><strong>Tipos de movimiento:</strong></p>
 * <ul>
 *   <li><b>ENTRADA:</b> Compra de productos, recepción de mercancía</li>
 *   <li><b>SALIDA:</b> Venta, uso en consulta, pérdida, vencimiento</li>
 *   <li><b>AJUSTE:</b> Corrección de inventario, inventario físico</li>
 * </ul>
 * 
 * <p><strong>Información gestionada:</strong></p>
 * <ul>
 *   <li><b>Producto:</b> Producto afectado por el movimiento</li>
 *   <li><b>Tipo:</b> ENTRADA, SALIDA o AJUSTE</li>
 *   <li><b>Cantidad:</b> Cantidad movida (positiva siempre)</li>
 *   <li><b>Motivo:</b> Razón del movimiento</li>
 *   <li><b>Usuario:</b> Usuario responsable del movimiento</li>
 *   <li><b>Proveedor:</b> Proveedor (solo para entradas)</li>
 *   <li><b>Precio unitario:</b> Precio al que se movió el producto</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li><b>Producto:</b> Muchos a uno - Cada movimiento afecta un producto</li>
 *   <li><b>Usuario:</b> Muchos a uno - Usuario que realizó el movimiento</li>
 *   <li><b>Proveedor:</b> Muchos a uno - Proveedor (opcional, solo para entradas)</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Producto ID - Para búsquedas por producto</li>
 *   <li>Fecha - Para búsquedas por rango de fechas</li>
 *   <li>Tipo - Para filtros por tipo de movimiento</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Producto: Requerido</li>
 *   <li>Tipo: Requerido, debe ser uno de los valores del enum</li>
 *   <li>Cantidad: Requerida, debe ser positiva</li>
 *   <li>Motivo: Requerido, máximo 500 caracteres</li>
 *   <li>Usuario: Requerido</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 */
@Entity
@Table(name = "movimientos_inventario", indexes = {
    @Index(name = "idx_movimiento_producto", columnList = "producto_id"),
    @Index(name = "idx_movimiento_fecha", columnList = "fecha"),
    @Index(name = "idx_movimiento_tipo", columnList = "tipo")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class MovimientoInventario {

    /**
     * Identificador único del movimiento (generado automáticamente).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Producto afectado por el movimiento.
     * Relación muchos a uno con Producto.
     */
    @NotNull(message = "El producto es requerido")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movimiento_producto"))
    private Producto producto;

    /**
     * Tipo de movimiento: ENTRADA, SALIDA o AJUSTE.
     */
    @NotNull(message = "El tipo de movimiento es requerido")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    /**
     * Cantidad movida (siempre positiva).
     * Para ENTRADA: cantidad que entra al inventario.
     * Para SALIDA: cantidad que sale del inventario.
     * Para AJUSTE: cantidad ajustada (puede ser incremento o decremento según el stock anterior).
     */
    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser positiva")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    /**
     * Precio unitario al momento del movimiento.
     * Para ENTRADAS: precio de compra.
     * Para SALIDAS: precio de venta o costo (según política).
     * Para AJUSTES: puede ser null.
     */
    @PositiveOrZero(message = "El precio unitario debe ser positivo o cero")
    @Column(name = "precio_unitario", precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /**
     * Motivo o razón del movimiento.
     * Ejemplos: "Compra a proveedor X", "Uso en consulta #123", "Ajuste por inventario físico"
     */
    @NotBlank(message = "El motivo del movimiento es requerido")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    @Column(nullable = false, length = 500)
    private String motivo;

    /**
     * Usuario que realizó el movimiento.
     * Relación muchos a uno con Usuario.
     */
    @NotNull(message = "El usuario responsable es requerido")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movimiento_usuario"))
    private Usuario usuario;

    /**
     * Proveedor (solo para movimientos de tipo ENTRADA).
     * Relación muchos a uno con Proveedor (opcional).
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", foreignKey = @ForeignKey(name = "fk_movimiento_proveedor"))
    private Proveedor proveedor;

    /**
     * Stock anterior antes del movimiento.
     * Se guarda para auditoría y para poder revertir movimientos si es necesario.
     */
    @Column(name = "stock_anterior", precision = 10, scale = 2)
    private BigDecimal stockAnterior;

    /**
     * Stock resultante después del movimiento.
     * Se calcula automáticamente: stockAnterior ± cantidad (según tipo).
     */
    @Column(name = "stock_resultante", precision = 10, scale = 2)
    private BigDecimal stockResultante;

    /**
     * Fecha y hora del movimiento (automático al crear).
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    /**
     * Notas adicionales sobre el movimiento.
     * Puede incluir información adicional relevante.
     */
    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    @Column(length = 1000)
    private String notas;

    /**
     * Enum que define los tipos de movimiento de inventario disponibles.
     * 
     * <p><strong>ENTRADA:</strong> Productos que ingresan al inventario (compras, donaciones).</p>
     * <p><strong>SALIDA:</strong> Productos que salen del inventario (ventas, uso, pérdidas).</p>
     * <p><strong>AJUSTE:</strong> Correcciones de inventario (inventario físico, correcciones).</p>
     */
    public enum TipoMovimiento {
        /**
         * Entrada de productos al inventario.
         * Aumenta el stock del producto.
         * Ejemplos: Compra a proveedor, recepción de mercancía, donación.
         */
        ENTRADA,

        /**
         * Salida de productos del inventario.
         * Disminuye el stock del producto.
         * Ejemplos: Venta, uso en consulta, pérdida, vencimiento, robo.
         */
        SALIDA,

        /**
         * Ajuste de inventario.
         * Puede aumentar o disminuir el stock según la diferencia encontrada.
         * Ejemplos: Inventario físico, corrección de errores, mermas no contabilizadas.
         */
        AJUSTE
    }
}

