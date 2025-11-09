package com.clinica.veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object (DTO) para estadísticas del dashboard.
 * 
 * <p>Este DTO contiene todas las estadísticas y datos necesarios para
 * mostrar el dashboard principal de la aplicación, incluyendo contadores,
 * próximas citas, gráficos y distribuciones.</p>
 * 
 * <p><strong>Campos principales:</strong></p>
 * <ul>
 *   <li><b>Estadísticas básicas:</b> Contadores de citas, pacientes, consultas, propietarios</li>
 *   <li><b>Próximas citas:</b> Lista de citas del día ordenadas por hora</li>
 *   <li><b>Consultas por día:</b> Datos para gráfico de barras (últimos 7 días)</li>
 *   <li><b>Distribución por especies:</b> Datos para gráfico de pastel</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    /**
     * Estadísticas básicas
     */
    private Long citasHoy;
    private Long pacientesActivos;
    private Long consultasPendientes;
    private Long totalPropietarios;

    /**
     * Próximas citas de hoy (máximo 4)
     */
    private List<ProximaCitaDTO> proximasCitas;

    /**
     * Consultas por día de la semana (últimos 7 días)
     */
    private List<ConsultasPorDiaDTO> consultasPorDia;

    /**
     * Distribución de pacientes por especie
     */
    private List<DistribucionEspecieDTO> distribucionEspecies;

    /**
     * DTO para próxima cita
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProximaCitaDTO {
        private Long id;
        private String hora;
        private String pacienteNombre;
        private String propietarioNombre;
        private String estado;
        private LocalDateTime fecha;
    }

    /**
     * DTO para consultas por día
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultasPorDiaDTO {
        private String dia;
        private Long consultas;
    }

    /**
     * DTO para distribución por especie
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistribucionEspecieDTO {
        private String nombre;
        private Long valor;
        private String color;
    }
}

