package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Paciente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad {@link Paciente}.
 * 
 * <p>Este DTO se utiliza para transferir datos de pacientes (mascotas) entre el cliente
 * y el servidor. Incluye información de identificación, características físicas y relación
 * con el propietario.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Identificación:</b> Nombre</li>
 *   <li><b>Características:</b> Especie, raza, sexo (M/F), edad en meses</li>
 *   <li><b>Estado físico:</b> Peso actual (kg)</li>
 *   <li><b>Notas:</b> Información adicional, alergias, condiciones especiales</li>
 *   <li><b>Relación:</b> propietarioId (requerido), propietarioNombre (opcional)</li>
 *   <li><b>Estado:</b> activo (true/false)</li>
 * </ul>
 * 
 * <p><strong>Validaciones:</strong></p>
 * <ul>
 *   <li>Nombre: Requerido, máximo 100 caracteres</li>
 *   <li>Especie: Requerida, máximo 50 caracteres</li>
 *   <li>Edad: Si se proporciona, debe ser positiva</li>
 *   <li>Peso: Si se proporciona, debe ser positivo</li>
 *   <li>Propietario: ID requerido</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see Paciente
 * @see PacienteService
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteDTO {

    private Long id;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @NotBlank(message = "La especie es requerida")
    @Size(max = 50, message = "La especie no puede exceder 50 caracteres")
    private String especie;

    @Size(max = 100, message = "La raza no puede exceder 100 caracteres")
    private String raza;

    private String sexo;

    @Positive(message = "La edad debe ser positiva")
    private Integer edadMeses;

    @Positive(message = "El peso debe ser positivo")
    private BigDecimal pesoKg;

    private String notas;

    private Boolean activo;

    @NotNull(message = "El propietario es requerido")
    private Long propietarioId;

    // Información del propietario (para mostrar)
    private String propietarioNombre;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Convierte una entidad {@link Paciente} a su DTO equivalente.
     * 
     * <p>Versión simplificada que no incluye el nombre del propietario.
     * Útil cuando solo se necesita el ID del propietario.</p>
     * 
     * @param paciente Entidad Paciente a convertir. No puede ser null.
     * @return DTO con los datos del paciente.
     * @see #fromEntity(Paciente, boolean)
     */
    public static PacienteDTO fromEntity(Paciente paciente) {
        return fromEntity(paciente, false);
    }

    /**
     * Convierte una entidad {@link Paciente} a su DTO equivalente con opción de incluir
     * el nombre del propietario.
     * 
     * <p>Cuando {@code includePropietario} es true, se incluye el nombre del propietario
     * para facilitar la visualización en el frontend sin necesidad de hacer consultas adicionales.</p>
     * 
     * @param paciente Entidad Paciente a convertir. No puede ser null.
     * @param includePropietario Si es true, incluye el nombre del propietario.
     * @return DTO con los datos del paciente.
     */
    public static PacienteDTO fromEntity(Paciente paciente, boolean includePropietario) {
        PacienteDTOBuilder builder = PacienteDTO.builder()
            .id(paciente.getId())
            .nombre(paciente.getNombre())
            .especie(paciente.getEspecie())
            .raza(paciente.getRaza())
            .sexo(paciente.getSexo())
            .edadMeses(paciente.getEdadMeses())
            .pesoKg(paciente.getPesoKg())
            .notas(paciente.getNotas())
            .activo(paciente.getActivo())
            .propietarioId(paciente.getPropietario().getId())
            .createdAt(paciente.getCreatedAt())
            .updatedAt(paciente.getUpdatedAt());

        if (includePropietario && paciente.getPropietario() != null) {
            builder.propietarioNombre(paciente.getPropietario().getNombre());
        }

        return builder.build();
    }
}

