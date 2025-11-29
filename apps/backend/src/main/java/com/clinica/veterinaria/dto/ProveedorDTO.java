package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Proveedor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad {@link Proveedor}.
 * 
 * <p>Este DTO se utiliza para transferir datos de proveedores entre el cliente
 * y el servidor. Incluye información de contacto y datos comerciales.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Identificación:</b> ID, nombre, RUC/NIT</li>
 *   <li><b>Contacto:</b> Email, teléfono, dirección</li>
 *   <li><b>Información adicional:</b> Notas</li>
 *   <li><b>Estado:</b> activo (true/false)</li>
 *   <li><b>Auditoría:</b> createdAt, updatedAt</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 200 caracteres</li>
 *   <li>Email: Formato válido, máximo 100 caracteres</li>
 *   <li>Teléfono: Opcional, máximo 20 caracteres</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see Proveedor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorDTO {

    private Long id;

    @NotBlank(message = "El nombre del proveedor es requerido")
    @Size(max = 200, message = "El nombre del proveedor no puede exceder 200 caracteres")
    private String nombre;

    @Size(max = 50, message = "El RUC/NIT no puede exceder 50 caracteres")
    private String ruc;

    @Email(message = "El email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    private String direccion;

    @Size(max = 1000, message = "Las notas no pueden exceder 1000 caracteres")
    private String notas;

    private Boolean activo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convierte una entidad Proveedor a su DTO correspondiente.
     * 
     * @param proveedor Entidad a convertir
     * @return DTO con los datos del proveedor
     */
    public static ProveedorDTO fromEntity(Proveedor proveedor) {
        if (proveedor == null) {
            return null;
        }
        return ProveedorDTO.builder()
            .id(proveedor.getId())
            .nombre(proveedor.getNombre())
            .ruc(proveedor.getRuc())
            .email(proveedor.getEmail())
            .telefono(proveedor.getTelefono())
            .direccion(proveedor.getDireccion())
            .notas(proveedor.getNotas())
            .activo(proveedor.getActivo())
            .createdAt(proveedor.getCreatedAt())
            .updatedAt(proveedor.getUpdatedAt())
            .build();
    }

    /**
     * Convierte este DTO a una entidad Proveedor.
     * 
     * @return Entidad Proveedor con los datos del DTO
     */
    public Proveedor toEntity() {
        return Proveedor.builder()
            .id(this.id)
            .nombre(this.nombre)
            .ruc(this.ruc)
            .email(this.email)
            .telefono(this.telefono)
            .direccion(this.direccion)
            .notas(this.notas)
            .activo(this.activo != null ? this.activo : true)
            .build();
    }
}

