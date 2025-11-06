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
 * DTO para la entidad Propietario
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
     * Constructor desde entidad (sin pacientes)
     */
    public static PropietarioDTO fromEntity(Propietario propietario) {
        return fromEntity(propietario, false);
    }

    /**
     * Constructor desde entidad (con opción de incluir IDs de pacientes)
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

