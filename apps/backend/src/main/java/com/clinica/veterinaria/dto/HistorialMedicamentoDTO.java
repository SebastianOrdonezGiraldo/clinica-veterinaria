package com.clinica.veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para el historial de medicamentos prescritos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialMedicamentoDTO {
    private String medicamento;
    private String presentacion;
    private String dosis;
    private String frecuencia;
    private String duracion;
    private LocalDateTime fechaPrescripcion;
    private String diagnostico;
    private String profesionalNombre;
    private Long consultaId;
}

