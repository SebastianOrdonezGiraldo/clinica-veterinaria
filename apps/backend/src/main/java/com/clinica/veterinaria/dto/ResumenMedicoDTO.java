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
 * DTO para el resumen médico del paciente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenMedicoDTO {
    private Integer totalConsultas;
    private LocalDateTime primeraConsulta;
    private LocalDateTime ultimaConsulta;
    private BigDecimal pesoInicial;
    private BigDecimal pesoActual;
    private BigDecimal variacionPeso;
    private BigDecimal temperaturaPromedio;
    private Integer frecuenciaCardiacaPromedio;
    private Integer frecuenciaRespiratoriaPromedio;
    private Integer totalPrescripciones;
    private List<String> diagnosticosFrecuentes;
    private List<String> medicamentosMasUsados;
}

