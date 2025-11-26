package com.clinica.veterinaria.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para solicitud pública de cita con registro opcional de propietario y paciente.
 * 
 * <p>Este DTO permite a los clientes agendar citas sin estar autenticados. Incluye
 * la opción de proporcionar datos del propietario y paciente, o usar IDs existentes
 * si ya están registrados en el sistema.</p>
 * 
 * <p><strong>Campos de la cita:</strong></p>
 * <ul>
 *   <li>fecha: Fecha y hora de la cita (requerido)</li>
 *   <li>motivo: Motivo de la consulta (requerido)</li>
 *   <li>observaciones: Observaciones adicionales (opcional)</li>
 *   <li>profesionalId: ID del veterinario (requerido)</li>
 * </ul>
 * 
 * <p><strong>Registro opcional:</strong></p>
 * <ul>
 *   <li>Si se proporcionan datos de propietario y paciente, se crearán automáticamente</li>
 *   <li>Si se proporcionan solo IDs, se usarán los registros existentes</li>
 *   <li>Si se proporciona email del propietario, se buscará si existe</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaPublicaRequestDTO {

    // Datos de la cita
    @NotNull(message = "La fecha es requerida")
    private LocalDateTime fecha;

    @NotBlank(message = "El motivo es requerido")
    @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
    private String motivo;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    @NotNull(message = "El profesional es requerido")
    private Long profesionalId;

    // Opción 1: Usar IDs existentes (si el cliente ya está registrado)
    private Long propietarioId;
    private Long pacienteId;

    // Opción 2: Crear nuevo propietario y paciente
    @Valid
    private PropietarioNuevoDTO propietarioNuevo;

    @Valid
    private PacienteNuevoDTO pacienteNuevo;

    /**
     * DTO anidado para datos de nuevo propietario
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropietarioNuevoDTO {
        @NotBlank(message = "El nombre del propietario es requerido")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        private String nombre;

        @Size(max = 20, message = "El documento no puede exceder 20 caracteres")
        private String documento;

        @NotBlank(message = "El email del propietario es requerido")
        @Email(message = "Email debe ser válido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        private String email;

        @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
        private String telefono;

        @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
        private String direccion;
    }

    /**
     * DTO anidado para datos de nuevo paciente
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PacienteNuevoDTO {
        @NotBlank(message = "El nombre del paciente es requerido")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        private String nombre;

        @NotBlank(message = "La especie es requerida")
        @Size(max = 50, message = "La especie no puede exceder 50 caracteres")
        private String especie;

        @Size(max = 100, message = "La raza no puede exceder 100 caracteres")
        private String raza;

        private String sexo; // M o F

        private Integer edadMeses;

        private java.math.BigDecimal pesoKg;

        @Size(max = 50, message = "El microchip no puede exceder 50 caracteres")
        private String microchip;

        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        private String notas;
    }
}

