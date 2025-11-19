package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.DashboardStatsDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.repository.CitaRepository;
import com.clinica.veterinaria.repository.ConsultaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para calcular estadísticas del dashboard.
 * 
 * <p>Este servicio proporciona todas las estadísticas y datos necesarios
 * para el dashboard principal, incluyendo contadores, próximas citas,
 * gráficos y distribuciones.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final PropietarioRepository propietarioRepository;
    private final ConsultaRepository consultaRepository;

    /**
     * Obtiene todas las estadísticas del dashboard.
     * 
     * @return DTO con todas las estadísticas del dashboard
     */
    public DashboardStatsDTO getDashboardStats() {
        log.debug("Calculando estadísticas del dashboard");

        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioHoy = hoy.atStartOfDay();
        LocalDateTime finHoy = hoy.atTime(LocalTime.MAX);

        // Estadísticas básicas
        long citasHoy = citaRepository.findByFechaBetween(inicioHoy, finHoy).size();
        long pacientesActivos = pacienteRepository.findByActivo(true).size();
        long consultasPendientes = citaRepository.findByEstadoIn(
            Arrays.asList(Cita.EstadoCita.PENDIENTE, Cita.EstadoCita.CONFIRMADA)
        ).size();
        long totalPropietarios = propietarioRepository.findByActivo(true).size();

        // Próximas citas de hoy
        List<DashboardStatsDTO.ProximaCitaDTO> proximasCitas = getProximasCitas(inicioHoy, finHoy);

        // Consultas por día (últimos 7 días)
        List<DashboardStatsDTO.ConsultasPorDiaDTO> consultasPorDia = getConsultasPorDia();

        // Distribución por especies
        List<DashboardStatsDTO.DistribucionEspecieDTO> distribucionEspecies = getDistribucionEspecies();

        return DashboardStatsDTO.builder()
            .citasHoy(citasHoy)
            .pacientesActivos(pacientesActivos)
            .consultasPendientes(consultasPendientes)
            .totalPropietarios(totalPropietarios)
            .proximasCitas(proximasCitas)
            .consultasPorDia(consultasPorDia)
            .distribucionEspecies(distribucionEspecies)
            .build();
    }

    /**
     * Obtiene las próximas citas de hoy ordenadas por hora.
     */
    private List<DashboardStatsDTO.ProximaCitaDTO> getProximasCitas(LocalDateTime inicio, LocalDateTime fin) {
        List<Cita> citasHoy = citaRepository.findByFechaBetween(inicio, fin);
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return citasHoy.stream()
            .sorted(Comparator.comparing(Cita::getFecha))
            .limit(4)
            .map(cita -> {
                Paciente paciente = cita.getPaciente();
                return DashboardStatsDTO.ProximaCitaDTO.builder()
                    .id(cita.getId())
                    .hora(cita.getFecha().format(timeFormatter))
                    .pacienteNombre(paciente != null ? paciente.getNombre() : "N/A")
                    .propietarioNombre(cita.getPropietario() != null ? cita.getPropietario().getNombre() : "N/A")
                    .estado(cita.getEstado().name())
                    .fecha(cita.getFecha())
                    .build();
            })
            .toList();
    }

    /**
     * Obtiene las consultas por día de la semana (últimos 7 días).
     */
    private List<DashboardStatsDTO.ConsultasPorDiaDTO> getConsultasPorDia() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(6);
        
        LocalDateTime inicio = inicioSemana.atStartOfDay();
        LocalDateTime fin = hoy.atTime(LocalTime.MAX);

        List<com.clinica.veterinaria.entity.Consulta> consultas = consultaRepository.findByFechaBetween(inicio, fin);

        // Agrupar por día de la semana
        Map<DayOfWeek, Long> consultasPorDiaMap = consultas.stream()
            .collect(Collectors.groupingBy(
                consulta -> consulta.getFecha().getDayOfWeek(),
                Collectors.counting()
            ));

        // Mapear nombres de días en español
        Map<DayOfWeek, String> nombresDias = Map.of(
            DayOfWeek.MONDAY, "Lun",
            DayOfWeek.TUESDAY, "Mar",
            DayOfWeek.WEDNESDAY, "Mié",
            DayOfWeek.THURSDAY, "Jue",
            DayOfWeek.FRIDAY, "Vie",
            DayOfWeek.SATURDAY, "Sáb",
            DayOfWeek.SUNDAY, "Dom"
        );

        // Crear lista ordenada por día de la semana
        List<DashboardStatsDTO.ConsultasPorDiaDTO> resultado = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            DayOfWeek dia = inicioSemana.plusDays(i).getDayOfWeek();
            String nombreDia = nombresDias.get(dia);
            Long cantidad = consultasPorDiaMap.getOrDefault(dia, 0L);
            resultado.add(DashboardStatsDTO.ConsultasPorDiaDTO.builder()
                .dia(nombreDia)
                .consultas(cantidad)
                .build());
        }

        return resultado;
    }

    /**
     * Obtiene la distribución de pacientes por especie.
     */
    private List<DashboardStatsDTO.DistribucionEspecieDTO> getDistribucionEspecies() {
        List<Paciente> pacientes = pacienteRepository.findByActivo(true);

        // Constantes para categorías de especies
        final String CATEGORIA_CANINOS = "Caninos";
        final String CATEGORIA_FELINOS = "Felinos";
        final String CATEGORIA_OTROS = "Otros";

        // Contar por especie
        Map<String, Long> especiesMap = pacientes.stream()
            .collect(Collectors.groupingBy(
                paciente -> {
                    String especie = paciente.getEspecie();
                    if (especie == null || especie.trim().isEmpty()) {
                        return CATEGORIA_OTROS;
                    }
                    String especieLower = especie.toLowerCase();
                    if (especieLower.contains("canino") || especieLower.contains("perro") || especieLower.contains("can")) {
                        return CATEGORIA_CANINOS;
                    } else if (especieLower.contains("felino") || especieLower.contains("gato") || especieLower.contains("fel")) {
                        return CATEGORIA_FELINOS;
                    } else {
                        return CATEGORIA_OTROS;
                    }
                },
                Collectors.counting()
            ));

        // Crear lista con colores
        Map<String, String> colores = Map.of(
            CATEGORIA_CANINOS, "hsl(var(--primary))",
            CATEGORIA_FELINOS, "hsl(var(--secondary))",
            CATEGORIA_OTROS, "hsl(var(--info))"
        );

        return Arrays.asList(CATEGORIA_CANINOS, CATEGORIA_FELINOS, CATEGORIA_OTROS).stream()
            .map(especie -> DashboardStatsDTO.DistribucionEspecieDTO.builder()
                .nombre(especie)
                .valor(especiesMap.getOrDefault(especie, 0L))
                .color(colores.get(especie))
                .build())
            .toList();
    }
}

