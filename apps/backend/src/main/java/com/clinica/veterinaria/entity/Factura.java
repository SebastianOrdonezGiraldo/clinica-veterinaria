package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una factura de servicios veterinarios.
 * 
 * <p>Las facturas pueden estar asociadas a consultas y contener
 * múltiples items (servicios, medicamentos, procedimientos).</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-30
 */
@Entity
@Table(name = "facturas", indexes = {
    @Index(name = "idx_factura_numero", columnList = "numero_factura", unique = true),
    @Index(name = "idx_factura_fecha", columnList = "fecha_emision"),
    @Index(name = "idx_factura_propietario", columnList = "propietario_id"),
    @Index(name = "idx_factura_estado", columnList = "estado")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El número de factura es requerido")
    @Column(name = "numero_factura", nullable = false, unique = true, length = 50)
    private String numeroFactura;

    @NotNull(message = "La fecha de emisión es requerida")
    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @NotNull(message = "El subtotal es requerido")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Builder.Default
    @Column(name = "descuento", precision = 10, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "impuesto", precision = 10, scale = 2)
    private BigDecimal impuesto = BigDecimal.ZERO;

    @NotNull(message = "El total es requerido")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Builder.Default
    @Column(name = "monto_pagado", precision = 10, scale = 2)
    private BigDecimal montoPagado = BigDecimal.ZERO;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoFactura estado = EstadoFactura.PENDIENTE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relación muchos a uno con Propietario
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propietario_id", nullable = false)
    @NotNull(message = "El propietario es requerido")
    @ToString.Exclude
    private Propietario propietario;

    /**
     * Relación muchos a uno con Consulta (opcional)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id")
    @ToString.Exclude
    private Consulta consulta;

    /**
     * Relación uno a muchos con ItemsFactura
     */
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ItemFactura> items = new ArrayList<>();

    /**
     * Relación uno a muchos con Pagos
     */
    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Pago> pagos = new ArrayList<>();

    /**
     * Estados de la factura
     */
    public enum EstadoFactura {
        PENDIENTE,
        PARCIAL,
        PAGADA,
        CANCELADA,
        VENCIDA
    }

    /**
     * Calcula el monto pendiente
     */
    public BigDecimal getMontoPendiente() {
        return total.subtract(montoPagado);
    }

    /**
     * Verifica si la factura está pagada completamente
     */
    public boolean isPagadaCompletamente() {
        return montoPagado.compareTo(total) >= 0;
    }

    /**
     * Verifica si la factura está vencida
     */
    public boolean isVencida() {
        return fechaVencimiento != null && 
               LocalDate.now().isAfter(fechaVencimiento) && 
               estado != EstadoFactura.PAGADA &&
               estado != EstadoFactura.CANCELADA;
    }
}

