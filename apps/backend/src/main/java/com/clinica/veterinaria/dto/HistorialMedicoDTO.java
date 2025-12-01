package com.clinica.veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para el historial médico completo de un paciente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialMedicoDTO {
    private Long pacienteId;
    private String pacienteNombre;
    
    @Builder.Default
    private List<ConsultaTimelineDTO> timelineConsultas = new ArrayList<>();
    
    @Builder.Default
    private List<EvolucionSignosVitalesDTO> evolucionSignosVitales = new ArrayList<>();
    
    @Builder.Default
    private List<HistorialMedicamentoDTO> historialMedicamentos = new ArrayList<>();
    
    private ResumenMedicoDTO resumen;
}

