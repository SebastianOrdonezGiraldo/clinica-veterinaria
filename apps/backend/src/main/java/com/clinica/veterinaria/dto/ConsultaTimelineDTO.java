package com.clinica.veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representar una consulta en el timeline
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaTimelineDTO {
    private Long id;
    private LocalDateTime fecha;
    private String profesionalNombre;
    private String diagnostico;
    private String tratamiento;
    private BigDecimal temperatura;
    private BigDecimal pesoKg;
    private Integer frecuenciaCardiaca;
    private Integer frecuenciaRespiratoria;
    private Boolean tienePrescripciones;
    private Integer cantidadPrescripciones;
}

