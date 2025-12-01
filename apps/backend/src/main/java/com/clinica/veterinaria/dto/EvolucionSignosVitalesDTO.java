package com.clinica.veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para la evolución de signos vitales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvolucionSignosVitalesDTO {
    private LocalDateTime fecha;
    private BigDecimal pesoKg;
    private BigDecimal temperatura;
    private Integer frecuenciaCardiaca;
    private Integer frecuenciaRespiratoria;
}

