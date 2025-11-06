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
 * DTO para la entidad Paciente
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

    @Size(max = 50, message = "El microchip no puede exceder 50 caracteres")
    private String microchip;

    private String notas;

    private Boolean activo;

    @NotNull(message = "El propietario es requerido")
    private Long propietarioId;

    // Información del propietario (para mostrar)
    private String propietarioNombre;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Constructor desde entidad (sin propietario cargado)
     */
    public static PacienteDTO fromEntity(Paciente paciente) {
        return fromEntity(paciente, false);
    }

    /**
     * Constructor desde entidad (con opción de cargar propietario)
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
            .microchip(paciente.getMicrochip())
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

