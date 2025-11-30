package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un pago realizado sobre una factura.
 * 
 * <p>Una factura puede tener múltiples pagos (pagos parciales).</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-30
 */
@Entity
@Table(name = "pagos", indexes = {
    @Index(name = "idx_pago_factura", columnList = "factura_id"),
    @Index(name = "idx_pago_fecha", columnList = "fecha_pago")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El monto es requerido")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @NotNull(message = "La fecha de pago es requerida")
    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA, CHEQUE

    @Column(name = "referencia", length = 200)
    private String referencia; // Número de transacción, cheque, etc.

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

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
     * Usuario que registró el pago
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @ToString.Exclude
    private Usuario registradoPor;
}

