package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.CategoriaProducto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad {@link CategoriaProducto}.
 * 
 * <p>Este DTO se utiliza para transferir datos de categorías de productos entre el cliente
 * y el servidor. Incluye información básica de la categoría y su estado.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Identificación:</b> ID, nombre</li>
 *   <li><b>Información:</b> Descripción</li>
 *   <li><b>Estado:</b> activo (true/false)</li>
 *   <li><b>Auditoría:</b> createdAt, updatedAt</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 100 caracteres</li>
 *   <li>Descripción: Opcional, máximo 500 caracteres</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see CategoriaProducto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaProductoDTO {

    private Long id;

    @NotBlank(message = "El nombre de la categoría es requerido")
    @Size(max = 100, message = "El nombre de la categoría no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    private Boolean activo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convierte una entidad CategoriaProducto a su DTO correspondiente.
     * 
     * @param categoria Entidad a convertir
     * @return DTO con los datos de la categoría
     */
    public static CategoriaProductoDTO fromEntity(CategoriaProducto categoria) {
        if (categoria == null) {
            return null;
        }
        return CategoriaProductoDTO.builder()
            .id(categoria.getId())
            .nombre(categoria.getNombre())
            .descripcion(categoria.getDescripcion())
            .activo(categoria.getActivo())
            .createdAt(categoria.getCreatedAt())
            .updatedAt(categoria.getUpdatedAt())
            .build();
    }

    /**
     * Convierte este DTO a una entidad CategoriaProducto.
     * 
     * @return Entidad CategoriaProducto con los datos del DTO
     */
    public CategoriaProducto toEntity() {
        return CategoriaProducto.builder()
            .id(this.id)
            .nombre(this.nombre)
            .descripcion(this.descripcion)
            .activo(this.activo != null ? this.activo : true)
            .build();
    }
}

