package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ReporteDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.CitaRepository;
import com.clinica.veterinaria.repository.ConsultaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar reportes operativos de la clínica veterinaria.
 * 
 * <p>Este servicio proporciona métodos para calcular estadísticas y generar
 * reportes sobre el funcionamiento de la clínica, incluyendo citas, consultas,
 * pacientes, veterinarios y tendencias.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReporteService {

    private final CitaRepository citaRepository;
    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Genera un reporte completo con todas las estadísticas.
     * 
     * @param periodo Periodo del reporte: "hoy", "semana", "mes", "año"
     * @return DTO con todas las estadísticas del reporte
     */
    public ReporteDTO generarReporte(String periodo) {
        log.debug("Generando reporte para periodo: {}", periodo);
        
        LocalDateTime[] rango = calcularRango(periodo);
        LocalDateTime inicio = rango[0];
        LocalDateTime fin = rango[1];

        // Estadísticas generales
        long totalCitas = citaRepository.count();
        long totalConsultas = consultaRepository.count();
        long totalPacientes = pacienteRepository.findByActivo(true).size();
        long totalVeterinarios = usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true).size();

        // Citas por estado
        List<ReporteDTO.CitasPorEstadoDTO> citasPorEstado = getCitasPorEstado();

        // Tendencia de citas por mes
        List<ReporteDTO.TendenciaCitasDTO> tendenciaCitas = getTendenciaCitas();

        // Pacientes por especie
        List<ReporteDTO.PacientesPorEspecieDTO> pacientesPorEspecie = getPacientesPorEspecie();

        // Atenciones por veterinario
        List<ReporteDTO.AtencionesPorVeterinarioDTO> atencionesPorVeterinario = getAtencionesPorVeterinario();

        // Top motivos de consulta
        List<ReporteDTO.TopMotivoConsultaDTO> topMotivosConsulta = getTopMotivosConsulta(inicio, fin);

        return ReporteDTO.builder()
            .totalCitas(totalCitas)
            .totalConsultas(totalConsultas)
            .totalPacientes(totalPacientes)
            .totalVeterinarios(totalVeterinarios)
            .citasPorEstado(citasPorEstado)
            .tendenciaCitas(tendenciaCitas)
            .pacientesPorEspecie(pacientesPorEspecie)
            .atencionesPorVeterinario(atencionesPorVeterinario)
            .topMotivosConsulta(topMotivosConsulta)
            .build();
    }

    /**
     * Calcula el rango de fechas según el periodo especificado.
     */
    private LocalDateTime[] calcularRango(String periodo) {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicio;
        LocalDateTime fin = hoy.atTime(LocalTime.MAX);

        switch (periodo.toLowerCase()) {
            case "hoy":
                inicio = hoy.atStartOfDay();
                break;
            case "semana":
                inicio = hoy.minusDays(7).atStartOfDay();
                break;
            case "mes":
                inicio = hoy.minusMonths(1).atStartOfDay();
                break;
            case "año":
                inicio = hoy.minusYears(1).atStartOfDay();
                break;
            default:
                inicio = hoy.minusMonths(1).atStartOfDay();
        }

        return new LocalDateTime[]{inicio, fin};
    }

    /**
     * Obtiene la distribución de citas por estado.
     */
    private List<ReporteDTO.CitasPorEstadoDTO> getCitasPorEstado() {
        List<Cita.EstadoCita> estados = Arrays.asList(
            Cita.EstadoCita.PENDIENTE,
            Cita.EstadoCita.CONFIRMADA,
            Cita.EstadoCita.ATENDIDA,
            Cita.EstadoCita.CANCELADA
        );

        return estados.stream()
            .map(estado -> ReporteDTO.CitasPorEstadoDTO.builder()
                .estado(estado.name())
                .cantidad(citaRepository.countByEstado(estado))
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Obtiene la tendencia de citas por mes (últimos 6 meses).
     */
    private List<ReporteDTO.TendenciaCitasDTO> getTendenciaCitas() {
        LocalDate hoy = LocalDate.now();
        List<ReporteDTO.TendenciaCitasDTO> tendencia = new ArrayList<>();

        DateTimeFormatter mesFormatter = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("es"));

        for (int i = 5; i >= 0; i--) {
            LocalDate mes = hoy.minusMonths(i);
            LocalDateTime inicioMes = mes.withDayOfMonth(1).atStartOfDay();
            LocalDateTime finMes = mes.withDayOfMonth(mes.lengthOfMonth()).atTime(LocalTime.MAX);

            long cantidad = citaRepository.findByFechaBetween(inicioMes, finMes).size();
            
            tendencia.add(ReporteDTO.TendenciaCitasDTO.builder()
                .mes(mes.format(mesFormatter))
                .citas(cantidad)
                .build());
        }

        return tendencia;
    }

    /**
     * Obtiene la distribución de pacientes por especie.
     */
    private List<ReporteDTO.PacientesPorEspecieDTO> getPacientesPorEspecie() {
        List<com.clinica.veterinaria.entity.Paciente> pacientes = pacienteRepository.findByActivo(true);

        Map<String, Long> especiesMap = pacientes.stream()
            .collect(Collectors.groupingBy(
                paciente -> {
                    String especie = paciente.getEspecie().toLowerCase();
                    if (especie.contains("canino") || especie.contains("perro") || especie.contains("can")) {
                        return "Canino";
                    } else if (especie.contains("felino") || especie.contains("gato") || especie.contains("fel")) {
                        return "Felino";
                    } else {
                        return "Otro";
                    }
                },
                Collectors.counting()
            ));

        return Arrays.asList("Canino", "Felino", "Otro").stream()
            .map(especie -> ReporteDTO.PacientesPorEspecieDTO.builder()
                .especie(especie)
                .cantidad(especiesMap.getOrDefault(especie, 0L))
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Obtiene las atenciones (consultas) por veterinario.
     */
    private List<ReporteDTO.AtencionesPorVeterinarioDTO> getAtencionesPorVeterinario() {
        List<Usuario> veterinarios = usuarioRepository.findByRolAndActivo(Usuario.Rol.VET, true);

        return veterinarios.stream()
            .map(vet -> {
                long consultas = consultaRepository.findByProfesionalId(vet.getId()).size();
                String nombre = vet.getNombre().split(" ")[0]; // Solo primer nombre
                return ReporteDTO.AtencionesPorVeterinarioDTO.builder()
                    .nombre(nombre)
                    .consultas(consultas)
                    .build();
            })
            .filter(a -> a.getConsultas() > 0) // Solo veterinarios con consultas
            .sorted((a, b) -> Long.compare(b.getConsultas(), a.getConsultas())) // Ordenar por cantidad descendente
            .collect(Collectors.toList());
    }

    /**
     * Obtiene los top motivos de consulta.
     */
    private List<ReporteDTO.TopMotivoConsultaDTO> getTopMotivosConsulta(LocalDateTime inicio, LocalDateTime fin) {
        List<Cita> citas = citaRepository.findByFechaBetween(inicio, fin);

        // Agrupar por motivo y contar
        Map<String, Long> motivosMap = citas.stream()
            .filter(c -> c.getMotivo() != null && !c.getMotivo().trim().isEmpty())
            .collect(Collectors.groupingBy(
                cita -> {
                    String motivo = cita.getMotivo().toLowerCase();
                    // Normalizar motivos comunes
                    if (motivo.contains("vacun") || motivo.contains("vacuna")) {
                        return "Vacunación";
                    } else if (motivo.contains("consulta general") || motivo.contains("revisión")) {
                        return "Consulta General";
                    } else if (motivo.contains("control")) {
                        return "Control";
                    } else if (motivo.contains("desparasit") || motivo.contains("parásito")) {
                        return "Desparasitación";
                    } else if (motivo.contains("cirugía") || motivo.contains("cirugia")) {
                        return "Cirugía";
                    } else if (motivo.contains("emergencia") || motivo.contains("urgencia")) {
                        return "Emergencia";
                    } else {
                        // Tomar las primeras palabras del motivo como categoría
                        String[] palabras = motivo.split("\\s+");
                        if (palabras.length > 0) {
                            return palabras[0].substring(0, Math.min(palabras[0].length(), 20));
                        }
                        return "Otro";
                    }
                },
                Collectors.counting()
            ));

        long total = citas.size();
        
        return motivosMap.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue())) // Ordenar por cantidad descendente
            .limit(10) // Top 10
            .map(entry -> ReporteDTO.TopMotivoConsultaDTO.builder()
                .motivo(entry.getKey())
                .cantidad(entry.getValue())
                .porcentaje(total > 0 ? (entry.getValue().doubleValue() / total) * 100 : 0.0)
                .build())
            .collect(Collectors.toList());
    }
}

