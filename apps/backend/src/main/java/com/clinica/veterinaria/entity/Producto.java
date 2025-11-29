package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa un producto del inventario de la clínica veterinaria.
 * 
 * <p>Esta entidad modela todos los productos que pueden estar en el inventario:
 * medicamentos, insumos médicos, alimentos, equipos, etc. Incluye información de
 * precios, stock y control de inventario.</p>
 * 
 * <p><strong>Información gestionada:</strong></p>
 * <ul>
 *   <li><b>Identificación:</b> Nombre, código único, descripción</li>
 *   <li><b>Categorización:</b> Categoría del producto</li>
 *   <li><b>Precios:</b> Costo de compra, precio de venta</li>
 *   <li><b>Control de stock:</b> Stock actual, mínimo, máximo, unidad de medida</li>
 *   <li><b>Estado:</b> Activo/Inactivo (soft delete)</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li><b>Categoría:</b> Muchos a uno - Cada producto pertenece a una categoría</li>
 *   <li><b>Movimientos:</b> Uno a muchos - Historial de movimientos de inventario</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Código (único) - Para búsquedas rápidas y evitar duplicados</li>
 *   <li>Nombre - Para búsquedas por nombre</li>
 *   <li>Categoría ID - Para filtros por categoría</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 200 caracteres</li>
 *   <li>Código: Requerido, máximo 50 caracteres, único</li>
 *   <li>Categoría: Requerida</li>
 *   <li>Precios: Deben ser positivos o cero</li>
 *   <li>Stock: Debe ser positivo o cero</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 */
@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_producto_codigo", columnList = "codigo", unique = true),
    @Index(name = "idx_producto_nombre", columnList = "nombre"),
    @Index(name = "idx_producto_categoria", columnList = "categoria_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

    /**
     * Identificador único del producto (generado automáticamente).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Nombre del producto.
     * Ejemplo: "Amoxicilina 500mg", "Jeringas 5ml", "Alimento Premium para Perros"
     */
    @NotBlank(message = "El nombre del producto es requerido")
    @Size(max = 200, message = "El nombre del producto no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String nombre;

    /**
     * Código único del producto (SKU, código de barras, etc.).
     * Debe ser único en el sistema.
     */
    @NotBlank(message = "El código del producto es requerido")
    @Size(max = 50, message = "El código del producto no puede exceder 50 caracteres")
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    /**
     * Descripción detallada del producto.
     * Puede incluir información sobre uso, presentación, etc.
     */
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(length = 1000)
    private String descripcion;

    /**
     * Categoría a la que pertenece el producto.
     * Relación muchos a uno con CategoriaProducto.
     */
    @NotNull(message = "La categoría del producto es requerida")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false, foreignKey = @ForeignKey(name = "fk_producto_categoria"))
    private CategoriaProducto categoria;

    /**
     * Unidad de medida del producto.
     * Ejemplos: "unidad", "caja", "frasco", "kg", "litro", "ml"
     */
    @NotBlank(message = "La unidad de medida es requerida")
    @Size(max = 20, message = "La unidad de medida no puede exceder 20 caracteres")
    @Column(name = "unidad_medida", nullable = false, length = 20)
    @Builder.Default
    private String unidadMedida = "unidad";

    /**
     * Stock actual del producto.
     * Se actualiza automáticamente con cada movimiento de inventario.
     */
    @NotNull(message = "El stock actual es requerido")
    @PositiveOrZero(message = "El stock actual debe ser positivo o cero")
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal stockActual = BigDecimal.ZERO;

    /**
     * Stock mínimo permitido antes de generar alerta.
     * Cuando el stock actual cae por debajo de este valor, se genera una alerta.
     */
    @PositiveOrZero(message = "El stock mínimo debe ser positivo o cero")
    @Column(name = "stock_minimo", precision = 10, scale = 2)
    private BigDecimal stockMinimo;

    /**
     * Stock máximo recomendado.
     * Útil para controlar el nivel de inventario y evitar sobrestock.
     */
    @PositiveOrZero(message = "El stock máximo debe ser positivo o cero")
    @Column(name = "stock_maximo", precision = 10, scale = 2)
    private BigDecimal stockMaximo;

    /**
     * Costo de compra del producto (precio al que se compró).
     * Se usa para calcular el valor del inventario y márgenes.
     */
    @PositiveOrZero(message = "El costo debe ser positivo o cero")
    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costo = BigDecimal.ZERO;

    /**
     * Precio de venta del producto (si aplica).
     * Algunos productos pueden no tener precio de venta (insumos internos).
     */
    @PositiveOrZero(message = "El precio de venta debe ser positivo o cero")
    @Column(name = "precio_venta", precision = 10, scale = 2)
    private BigDecimal precioVenta;

    /**
     * Indica si el producto está activo.
     * Los productos inactivos no se muestran en listados pero se mantienen
     * para preservar el historial de movimientos.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean activo = true;

    /**
     * Fecha y hora de creación del registro (automático).
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha y hora de última actualización (automático).
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Lista de movimientos de inventario asociados a este producto.
     * Relación uno a muchos con MovimientoInventario.
     */
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<MovimientoInventario> movimientos = new ArrayList<>();
}

