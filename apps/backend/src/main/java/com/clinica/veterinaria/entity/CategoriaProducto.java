package com.clinica.veterinaria.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA que representa las categorías de productos del inventario.
 * 
 * <p>Esta entidad permite organizar los productos del inventario en categorías
 * como Medicamentos, Insumos, Alimentos, Equipos, etc. Facilita la búsqueda,
 * filtrado y reportes de inventario.</p>
 * 
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li><b>Organización:</b> Agrupa productos relacionados</li>
 *   <li><b>Descripción:</b> Información adicional sobre la categoría</li>
 *   <li><b>Soft Delete:</b> Campo activo para desactivar sin eliminar</li>
 *   <li><b>Auditoría:</b> Timestamps automáticos de creación y actualización</li>
 * </ul>
 * 
 * <p><strong>Relaciones:</strong></p>
 * <ul>
 *   <li><b>Productos:</b> Uno a muchos - Una categoría puede tener múltiples productos</li>
 * </ul>
 * 
 * <p><strong>Índices de base de datos:</strong></p>
 * <ul>
 *   <li>Nombre (único) - Para búsquedas rápidas y evitar duplicados</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 100 caracteres, único</li>
 *   <li>Descripción: Opcional, máximo 500 caracteres</li>
 * </ul>
 * 
 * <p><strong>Ejemplos de categorías:</strong></p>
 * <ul>
 *   <li>Medicamentos</li>
 *   <li>Insumos Médicos</li>
 *   <li>Alimentos</li>
 *   <li>Equipos</li>
 *   <li>Material de Limpieza</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 */
@Entity
@Table(name = "categorias_producto", indexes = {
    @Index(name = "idx_categoria_nombre", columnList = "nombre", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CategoriaProducto {

    /**
     * Identificador único de la categoría (generado automáticamente).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**
     * Nombre de la categoría (único, requerido).
     * Ejemplos: "Medicamentos", "Insumos Médicos", "Alimentos"
     */
    @NotBlank(message = "El nombre de la categoría es requerido")
    @Size(max = 100, message = "El nombre de la categoría no puede exceder 100 caracteres")
    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    /**
     * Descripción opcional de la categoría.
     * Proporciona información adicional sobre qué tipo de productos incluye.
     */
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(length = 500)
    private String descripcion;

    /**
     * Indica si la categoría está activa.
     * Las categorías inactivas no se muestran en listados pero se mantienen
     * para preservar el historial de productos asociados.
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
     * Lista de productos asociados a esta categoría.
     * Relación uno a muchos con Producto.
     */
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    @Builder.Default
    private List<Producto> productos = new ArrayList<>();
}

