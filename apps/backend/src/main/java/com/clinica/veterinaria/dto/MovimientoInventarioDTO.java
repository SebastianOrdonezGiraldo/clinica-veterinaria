package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.MovimientoInventario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad {@link MovimientoInventario}.
 * 
 * <p>Este DTO se utiliza para transferir datos de movimientos de inventario entre el cliente
 * y el servidor. Incluye información completa del movimiento: producto, tipo, cantidad, motivo, etc.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Identificación:</b> ID</li>
 *   <li><b>Producto:</b> productoId, productoNombre, productoCodigo</li>
 *   <li><b>Tipo:</b> ENTRADA, SALIDA o AJUSTE</li>
 *   <li><b>Cantidad:</b> Cantidad movida (siempre positiva)</li>
 *   <li><b>Precio:</b> precioUnitario</li>
 *   <li><b>Motivo:</b> Razón del movimiento</li>
 *   <li><b>Usuario:</b> usuarioId, usuarioNombre</li>
 *   <li><b>Proveedor:</b> proveedorId, proveedorNombre (opcional, solo para entradas)</li>
 *   <li><b>Stock:</b> stockAnterior, stockResultante</li>
 *   <li><b>Auditoría:</b> fecha</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Producto: ID requerido</li>
 *   <li>Tipo: Requerido, debe ser ENTRADA, SALIDA o AJUSTE</li>
 *   <li>Cantidad: Requerida, debe ser positiva</li>
 *   <li>Motivo: Requerido, máximo 500 caracteres</li>
 *   <li>Usuario: ID requerido</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see MovimientoInventario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventarioDTO {

    private Long id;

    @NotNull(message = "El producto es requerido")
    private Long productoId;

    private String productoNombre; // Para mostrar en listados
    private String productoCodigo; // Para mostrar en listados

    @NotNull(message = "El tipo de movimiento es requerido")
    private MovimientoInventario.TipoMovimiento tipo;

    @NotNull(message = "La cantidad es requerida")
    @Positive(message = "La cantidad debe ser positiva")
    private BigDecimal cantidad;

    @PositiveOrZero(message = "El precio unitario debe ser positivo o cero")
    private BigDecimal precioUnitario;

    @NotBlank(message = "El motivo del movimiento es requerido")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;

    @NotNull(message = "El usuario responsable es requerido")
    private Long usuarioId;

    private String usuarioNombre; // Para mostrar en listados

    private Long proveedorId; // Opcional, solo para entradas
    private String proveedorNombre; // Para mostrar en listados

    private BigDecimal stockAnterior;
    private BigDecimal stockResultante;

    private String notas;

    private LocalDateTime fecha;

    /**
     * Convierte una entidad MovimientoInventario a su DTO correspondiente.
     * 
     * @param movimiento Entidad a convertir
     * @return DTO con los datos del movimiento
     */
    public static MovimientoInventarioDTO fromEntity(MovimientoInventario movimiento) {
        if (movimiento == null) {
            return null;
        }
        return MovimientoInventarioDTO.builder()
            .id(movimiento.getId())
            .productoId(movimiento.getProducto() != null ? movimiento.getProducto().getId() : null)
            .productoNombre(movimiento.getProducto() != null ? movimiento.getProducto().getNombre() : null)
            .productoCodigo(movimiento.getProducto() != null ? movimiento.getProducto().getCodigo() : null)
            .tipo(movimiento.getTipo())
            .cantidad(movimiento.getCantidad())
            .precioUnitario(movimiento.getPrecioUnitario())
            .motivo(movimiento.getMotivo())
            .usuarioId(movimiento.getUsuario() != null ? movimiento.getUsuario().getId() : null)
            .usuarioNombre(movimiento.getUsuario() != null ? movimiento.getUsuario().getNombre() : null)
            .proveedorId(movimiento.getProveedor() != null ? movimiento.getProveedor().getId() : null)
            .proveedorNombre(movimiento.getProveedor() != null ? movimiento.getProveedor().getNombre() : null)
            .stockAnterior(movimiento.getStockAnterior())
            .stockResultante(movimiento.getStockResultante())
            .notas(movimiento.getNotas())
            .fecha(movimiento.getFecha())
            .build();
    }
}

