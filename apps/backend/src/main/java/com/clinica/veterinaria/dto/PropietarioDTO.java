package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Propietario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object (DTO) para la entidad {@link Propietario}.
 * 
 * <p>Este DTO se utiliza para transferir datos de propietarios (clientes) entre el cliente
 * y el servidor. Incluye información de contacto y datos personales.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Datos personales:</b> Nombre, documento de identidad</li>
 *   <li><b>Contacto:</b> Email, teléfono, dirección</li>
 *   <li><b>Estado:</b> activo (true/false)</li>
 *   <li><b>Relación (opcional):</b> Lista de IDs de pacientes asociados</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 100 caracteres</li>
 *   <li>Email: Si se proporciona, debe ser válido, máximo 100 caracteres</li>
 *   <li>Documento: Opcional, máximo 20 caracteres (debe ser único si se proporciona)</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Propietario
 * @see PropietarioService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropietarioDTO {

    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 20, message = "El documento no puede exceder 20 caracteres")
    private String documento;

    @Email(message = "Email debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    private Boolean activo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Lista opcional de IDs de pacientes (para casos específicos)
    private List<Long> pacientesIds;

    /**
     * Convierte una entidad {@link Propietario} a su DTO equivalente.
     * 
     * <p>Versión simplificada que no incluye IDs de pacientes asociados.</p>
     * 
     * @param propietario Entidad Propietario a convertir. No puede ser null.
     * @return DTO con los datos del propietario.
     * @see #fromEntity(Propietario, boolean)
     */
    public static PropietarioDTO fromEntity(Propietario propietario) {
        return fromEntity(propietario, false);
    }

    /**
     * Convierte una entidad {@link Propietario} a su DTO equivalente con opción de incluir
     * los IDs de los pacientes asociados.
     * 
     * <p>Cuando {@code includePacientes} es true, se incluye la lista de IDs de todas las
     * mascotas del propietario.</p>
     * 
     * @param propietario Entidad Propietario a convertir. No puede ser null.
     * @param includePacientes Si es true, incluye la lista de IDs de pacientes.
     * @return DTO con los datos del propietario.
     */
    public static PropietarioDTO fromEntity(Propietario propietario, boolean includePacientes) {
        PropietarioDTOBuilder builder = PropietarioDTO.builder()
            .id(propietario.getId())
            .nombre(propietario.getNombre())
            .documento(propietario.getDocumento())
            .email(propietario.getEmail())
            .telefono(propietario.getTelefono())
            .direccion(propietario.getDireccion())
            .activo(propietario.getActivo())
            .createdAt(propietario.getCreatedAt())
            .updatedAt(propietario.getUpdatedAt());

        if (includePacientes && propietario.getPacientes() != null) {
            builder.pacientesIds(
                propietario.getPacientes().stream()
                    .map(p -> p.getId())
                    .collect(Collectors.toList())
            );
        }

        return builder.build();
    }
}

