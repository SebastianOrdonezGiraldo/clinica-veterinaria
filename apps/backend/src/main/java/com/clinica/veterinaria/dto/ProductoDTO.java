package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Producto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad {@link Producto}.
 * 
 * <p>Este DTO se utiliza para transferir datos de productos del inventario entre el cliente
 * y el servidor. Incluye información completa del producto, precios y control de stock.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Identificación:</b> ID, nombre, código</li>
 *   <li><b>Categorización:</b> categoriaId, categoriaNombre</li>
 *   <li><b>Control de stock:</b> stockActual, stockMinimo, stockMaximo, unidadMedida</li>
 *   <li><b>Precios:</b> costo, precioVenta</li>
 *   <li><b>Estado:</b> activo (true/false)</li>
 *   <li><b>Auditoría:</b> createdAt, updatedAt</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 200 caracteres</li>
 *   <li>Código: Requerido, máximo 50 caracteres</li>
 *   <li>Categoría: ID requerido</li>
 *   <li>Precios: Deben ser positivos o cero</li>
 *   <li>Stock: Debe ser positivo o cero</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see Producto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    private Long id;

    @NotBlank(message = "El nombre del producto es requerido")
    @Size(max = 200, message = "El nombre del producto no puede exceder 200 caracteres")
    private String nombre;

    @NotBlank(message = "El código del producto es requerido")
    @Size(max = 50, message = "El código del producto no puede exceder 50 caracteres")
    private String codigo;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;

    @NotNull(message = "La categoría del producto es requerida")
    private Long categoriaId;

    private String categoriaNombre; // Para mostrar en listados

    @NotBlank(message = "La unidad de medida es requerida")
    @Size(max = 20, message = "La unidad de medida no puede exceder 20 caracteres")
    @Builder.Default
    private String unidadMedida = "unidad";

    @NotNull(message = "El stock actual es requerido")
    @PositiveOrZero(message = "El stock actual debe ser positivo o cero")
    @Builder.Default
    private BigDecimal stockActual = BigDecimal.ZERO;

    @PositiveOrZero(message = "El stock mínimo debe ser positivo o cero")
    private BigDecimal stockMinimo;

    @PositiveOrZero(message = "El stock máximo debe ser positivo o cero")
    private BigDecimal stockMaximo;

    @NotNull(message = "El costo es requerido")
    @PositiveOrZero(message = "El costo debe ser positivo o cero")
    @Builder.Default
    private BigDecimal costo = BigDecimal.ZERO;

    @PositiveOrZero(message = "El precio de venta debe ser positivo o cero")
    private BigDecimal precioVenta;

    private Boolean activo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Indica si el producto tiene stock bajo (stock actual <= stock mínimo).
     * 
     * @return true si tiene stock bajo, false en caso contrario
     */
    public boolean tieneStockBajo() {
        return stockMinimo != null && stockActual != null && stockActual.compareTo(stockMinimo) <= 0;
    }

    /**
     * Indica si el producto tiene sobrestock (stock actual > stock máximo).
     * 
     * @return true si tiene sobrestock, false en caso contrario
     */
    public boolean tieneSobrestock() {
        return stockMaximo != null && stockActual != null && stockActual.compareTo(stockMaximo) > 0;
    }

    /**
     * Convierte una entidad Producto a su DTO correspondiente.
     * 
     * @param producto Entidad a convertir
     * @return DTO con los datos del producto
     */
    public static ProductoDTO fromEntity(Producto producto) {
        if (producto == null) {
            return null;
        }
        return ProductoDTO.builder()
            .id(producto.getId())
            .nombre(producto.getNombre())
            .codigo(producto.getCodigo())
            .descripcion(producto.getDescripcion())
            .categoriaId(producto.getCategoria() != null ? producto.getCategoria().getId() : null)
            .categoriaNombre(producto.getCategoria() != null ? producto.getCategoria().getNombre() : null)
            .unidadMedida(producto.getUnidadMedida())
            .stockActual(producto.getStockActual())
            .stockMinimo(producto.getStockMinimo())
            .stockMaximo(producto.getStockMaximo())
            .costo(producto.getCosto())
            .precioVenta(producto.getPrecioVenta())
            .activo(producto.getActivo())
            .createdAt(producto.getCreatedAt())
            .updatedAt(producto.getUpdatedAt())
            .build();
    }
}

