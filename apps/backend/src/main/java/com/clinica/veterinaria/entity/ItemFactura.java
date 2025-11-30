package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un item (línea) dentro de una factura.
 * 
 * <p>Cada item puede ser un servicio, medicamento, procedimiento, etc.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-30
 */
@Entity
@Table(name = "items_factura")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "La descripción es requerida")
    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(name = "tipo_item", length = 50)
    private String tipoItem; // SERVICIO, MEDICAMENTO, PROCEDIMIENTO, OTRO

    @Column(name = "codigo_producto", length = 100)
    private String codigoProducto; // Para medicamentos del inventario

    @NotNull(message = "La cantidad es requerida")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @NotNull(message = "El precio unitario es requerido")
    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Builder.Default
    @Column(name = "descuento", precision = 10, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @NotNull(message = "El subtotal es requerido")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "orden", nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Relación muchos a uno con Factura
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id", nullable = false)
    @NotNull(message = "La factura es requerida")
    @ToString.Exclude
    private Factura factura;

    /**
     * Calcula el subtotal del item
     */
    @PrePersist
    @PreUpdate
    public void calcularSubtotal() {
        if (cantidad != null && precioUnitario != null) {
            BigDecimal subtotalCalculado = cantidad.multiply(precioUnitario);
            if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
                subtotalCalculado = subtotalCalculado.subtract(descuento);
            }
            this.subtotal = subtotalCalculado;
        }
    }
}

