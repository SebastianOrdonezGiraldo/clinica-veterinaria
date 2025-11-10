package com.clinica.veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object (DTO) para reportes operativos de la clínica.
 * 
 * <p>Este DTO encapsula todos los datos necesarios para generar reportes
 * y estadísticas operativas de la clínica veterinaria.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDTO {

    // Estadísticas generales
    private Long totalCitas;
    private Long totalConsultas;
    private Long totalPacientes;
    private Long totalVeterinarios;

    // Citas por estado
    private List<CitasPorEstadoDTO> citasPorEstado;

    // Tendencia de citas por mes
    private List<TendenciaCitasDTO> tendenciaCitas;

    // Pacientes por especie
    private List<PacientesPorEspecieDTO> pacientesPorEspecie;

    // Atenciones por veterinario
    private List<AtencionesPorVeterinarioDTO> atencionesPorVeterinario;

    // Top motivos de consulta
    private List<TopMotivoConsultaDTO> topMotivosConsulta;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CitasPorEstadoDTO {
        private String estado;
        private Long cantidad;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TendenciaCitasDTO {
        private String mes;
        private Long citas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PacientesPorEspecieDTO {
        private String especie;
        private Long cantidad;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AtencionesPorVeterinarioDTO {
        private String nombre;
        private Long consultas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopMotivoConsultaDTO {
        private String motivo;
        private Long cantidad;
        private Double porcentaje;
    }
}

