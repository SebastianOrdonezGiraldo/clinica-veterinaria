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
    private Long vacunacionesProximas;
    private Long vacunacionesVencidas;
    private Long productosStockBajo;
    private Long prescripcionesMes;

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
     * Citas por estado (para gráfico)
     */
    private List<CitasPorEstadoDTO> citasPorEstado;

    /**
     * Tendencias de consultas (últimos 30 días)
     */
    private List<TendenciaConsultaDTO> tendenciasConsultas;

    /**
     * Actividad reciente
     */
    private List<ActividadRecienteDTO> actividadReciente;

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

    /**
     * DTO para citas por estado
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CitasPorEstadoDTO {
        private String estado;
        private Long cantidad;
        private String color;
    }

    /**
     * DTO para tendencias de consultas
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TendenciaConsultaDTO {
        private String fecha;
        private Long consultas;
    }

    /**
     * DTO para actividad reciente
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActividadRecienteDTO {
        private String tipo; // CONSULTA, CITA, PACIENTE, PRESCRIPCION, VACUNACION
        private String descripcion;
        private String fecha;
        private String link;
    }
}

