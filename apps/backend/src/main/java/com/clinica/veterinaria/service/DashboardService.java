package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.DashboardStatsDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Consulta;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Prescripcion;
import com.clinica.veterinaria.repository.*;
import com.clinica.veterinaria.entity.Vacunacion;
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
    private final VacunacionRepository vacunacionRepository;
    private final ProductoRepository productoRepository;
    private final PrescripcionRepository prescripcionRepository;

    /**
     * Obtiene todas las estadísticas del dashboard.
     * 
     * @return DTO con todas las estadísticas del dashboard
     */
    public DashboardStatsDTO getDashboardStats() {
        return getDashboardStats(null, null);
    }

    /**
     * Obtiene todas las estadísticas del dashboard con filtros de fecha opcionales.
     * 
     * @param fechaInicio Fecha de inicio para filtros (opcional)
     * @param fechaFin Fecha de fin para filtros (opcional)
     * @return DTO con todas las estadísticas del dashboard
     */
    public DashboardStatsDTO getDashboardStats(LocalDate fechaInicio, LocalDate fechaFin) {
        log.debug("Calculando estadísticas del dashboard - desde: {}, hasta: {}", fechaInicio, fechaFin);

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

        // Nuevas métricas
        long vacunacionesProximas = vacunacionRepository.findProximasAVencer(hoy, hoy.plusDays(30)).size();
        long vacunacionesVencidas = vacunacionRepository.findVencidas(hoy).size();
        long productosStockBajo = productoRepository.findProductosConStockBajo().size();
        
        // Prescripciones del mes actual
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = hoy.atTime(LocalTime.MAX);
        long prescripcionesMes = prescripcionRepository.findByFechaEmisionBetween(inicioMes, finMes).size();

        // Próximas citas de hoy
        List<DashboardStatsDTO.ProximaCitaDTO> proximasCitas = getProximasCitas(inicioHoy, finHoy);

        // Consultas por día (últimos 7 días o rango personalizado)
        List<DashboardStatsDTO.ConsultasPorDiaDTO> consultasPorDia = getConsultasPorDia(fechaInicio, fechaFin);

        // Distribución por especies
        List<DashboardStatsDTO.DistribucionEspecieDTO> distribucionEspecies = getDistribucionEspecies();

        // Citas por estado
        List<DashboardStatsDTO.CitasPorEstadoDTO> citasPorEstado = getCitasPorEstado();

        // Tendencias de consultas (últimos 30 días)
        List<DashboardStatsDTO.TendenciaConsultaDTO> tendenciasConsultas = getTendenciasConsultas();

        // Actividad reciente
        List<DashboardStatsDTO.ActividadRecienteDTO> actividadReciente = getActividadReciente();

        return DashboardStatsDTO.builder()
            .citasHoy(citasHoy)
            .pacientesActivos(pacientesActivos)
            .consultasPendientes(consultasPendientes)
            .totalPropietarios(totalPropietarios)
            .vacunacionesProximas(vacunacionesProximas)
            .vacunacionesVencidas(vacunacionesVencidas)
            .productosStockBajo(productosStockBajo)
            .prescripcionesMes(prescripcionesMes)
            .proximasCitas(proximasCitas)
            .consultasPorDia(consultasPorDia)
            .distribucionEspecies(distribucionEspecies)
            .citasPorEstado(citasPorEstado)
            .tendenciasConsultas(tendenciasConsultas)
            .actividadReciente(actividadReciente)
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
     * Obtiene las consultas por día (últimos 7 días o rango personalizado).
     */
    private List<DashboardStatsDTO.ConsultasPorDiaDTO> getConsultasPorDia(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana;
        LocalDate finSemana;
        
        if (fechaInicio != null && fechaFin != null) {
            inicioSemana = fechaInicio;
            finSemana = fechaFin;
        } else {
            inicioSemana = hoy.minusDays(6);
            finSemana = hoy;
        }
        
        LocalDateTime inicio = inicioSemana.atStartOfDay();
        LocalDateTime fin = finSemana.atTime(LocalTime.MAX);

        List<Consulta> consultas = consultaRepository.findByFechaBetween(inicio, fin);

        // Agrupar por día
        Map<LocalDate, Long> consultasPorDiaMap = consultas.stream()
            .collect(Collectors.groupingBy(
                consulta -> consulta.getFecha().toLocalDate(),
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

        // Crear lista ordenada por fecha
        List<DashboardStatsDTO.ConsultasPorDiaDTO> resultado = new ArrayList<>();
        LocalDate fechaActual = inicioSemana;
        while (!fechaActual.isAfter(finSemana)) {
            DayOfWeek diaSemana = fechaActual.getDayOfWeek();
            String nombreDia = nombresDias.get(diaSemana);
            Long cantidad = consultasPorDiaMap.getOrDefault(fechaActual, 0L);
            resultado.add(DashboardStatsDTO.ConsultasPorDiaDTO.builder()
                .dia(nombreDia + " " + fechaActual.getDayOfMonth())
                .consultas(cantidad)
                .build());
            fechaActual = fechaActual.plusDays(1);
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

    /**
     * Obtiene la distribución de citas por estado.
     */
    private List<DashboardStatsDTO.CitasPorEstadoDTO> getCitasPorEstado() {
        List<Cita> todasLasCitas = citaRepository.findAll();
        
        Map<Cita.EstadoCita, Long> citasPorEstadoMap = todasLasCitas.stream()
            .collect(Collectors.groupingBy(
                Cita::getEstado,
                Collectors.counting()
            ));

        Map<Cita.EstadoCita, String> colores = Map.of(
            Cita.EstadoCita.PENDIENTE, "hsl(var(--status-pending))",
            Cita.EstadoCita.CONFIRMADA, "hsl(var(--status-confirmed))",
            Cita.EstadoCita.ATENDIDA, "hsl(var(--status-completed))",
            Cita.EstadoCita.CANCELADA, "hsl(var(--status-cancelled))"
        );

        return Arrays.stream(Cita.EstadoCita.values())
            .map(estado -> DashboardStatsDTO.CitasPorEstadoDTO.builder()
                .estado(estado.name())
                .cantidad(citasPorEstadoMap.getOrDefault(estado, 0L))
                .color(colores.getOrDefault(estado, "hsl(var(--muted-foreground))"))
                .build())
            .toList();
    }

    /**
     * Obtiene las tendencias de consultas (últimos 30 días).
     */
    private List<DashboardStatsDTO.TendenciaConsultaDTO> getTendenciasConsultas() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusDays(29);
        
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = hoy.atTime(LocalTime.MAX);

        List<Consulta> consultas = consultaRepository.findByFechaBetween(inicioDateTime, finDateTime);

        // Agrupar por fecha
        Map<LocalDate, Long> consultasPorFecha = consultas.stream()
            .collect(Collectors.groupingBy(
                consulta -> consulta.getFecha().toLocalDate(),
                Collectors.counting()
            ));

        // Crear lista para los últimos 30 días
        List<DashboardStatsDTO.TendenciaConsultaDTO> resultado = new ArrayList<>();
        LocalDate fechaActual = inicio;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        
        while (!fechaActual.isAfter(hoy)) {
            Long cantidad = consultasPorFecha.getOrDefault(fechaActual, 0L);
            resultado.add(DashboardStatsDTO.TendenciaConsultaDTO.builder()
                .fecha(fechaActual.format(formatter))
                .consultas(cantidad)
                .build());
            fechaActual = fechaActual.plusDays(1);
        }

        return resultado;
    }

    /**
     * Obtiene la actividad reciente del sistema.
     */
    private List<DashboardStatsDTO.ActividadRecienteDTO> getActividadReciente() {
        List<DashboardStatsDTO.ActividadRecienteDTO> actividades = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime hace7Dias = ahora.minusDays(7);

        // Últimas consultas (últimas 3 del último mes)
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Consulta> consultasRecientes = consultaRepository.findByFechaBetween(inicioMes, ahora);
        List<Consulta> ultimasConsultas = consultasRecientes.stream()
            .sorted(Comparator.comparing(Consulta::getFecha).reversed())
            .limit(3)
            .toList();

        for (Consulta consulta : ultimasConsultas) {
            Paciente paciente = consulta.getPaciente();
            actividades.add(DashboardStatsDTO.ActividadRecienteDTO.builder()
                .tipo("CONSULTA")
                .descripcion("Consulta completada - " + (paciente != null ? paciente.getNombre() : "N/A"))
                .fecha(consulta.getFecha().format(formatter))
                .link("/historias/" + (paciente != null ? paciente.getId() : ""))
                .build());
        }

        // Últimas prescripciones (últimas 2 del último mes)
        List<Prescripcion> prescripcionesRecientes = prescripcionRepository.findByFechaEmisionBetween(inicioMes, ahora);
        List<Prescripcion> ultimasPrescripciones = prescripcionesRecientes.stream()
            .sorted(Comparator.comparing(Prescripcion::getFechaEmision).reversed())
            .limit(2)
            .toList();

        for (Prescripcion prescripcion : ultimasPrescripciones) {
            actividades.add(DashboardStatsDTO.ActividadRecienteDTO.builder()
                .tipo("PRESCRIPCION")
                .descripcion("Nueva prescripción registrada")
                .fecha(prescripcion.getFechaEmision().format(formatter))
                .link("/prescripciones/" + prescripcion.getId())
                .build());
        }

        // Últimas citas creadas (últimas 2)
        List<Cita> citasRecientes = citaRepository.findByFechaBetween(hace7Dias, ahora);
        List<Cita> ultimasCitas = citasRecientes.stream()
            .sorted((c1, c2) -> {
                LocalDateTime fecha1 = c1.getCreatedAt() != null ? c1.getCreatedAt() : c1.getFecha();
                LocalDateTime fecha2 = c2.getCreatedAt() != null ? c2.getCreatedAt() : c2.getFecha();
                return fecha2.compareTo(fecha1);
            })
            .limit(2)
            .toList();

        for (Cita cita : ultimasCitas) {
            Paciente paciente = cita.getPaciente();
            LocalDateTime fechaCita = cita.getCreatedAt() != null ? cita.getCreatedAt() : cita.getFecha();
            actividades.add(DashboardStatsDTO.ActividadRecienteDTO.builder()
                .tipo("CITA")
                .descripcion("Nueva cita agendada - " + (paciente != null ? paciente.getNombre() : "N/A"))
                .fecha(fechaCita.format(formatter))
                .link("/agenda")
                .build());
        }

        // Ordenar por fecha descendente y limitar a 5
        return actividades.stream()
            .sorted((a1, a2) -> {
                try {
                    LocalDateTime fecha1 = LocalDateTime.parse(a1.getFecha(), formatter);
                    LocalDateTime fecha2 = LocalDateTime.parse(a2.getFecha(), formatter);
                    return fecha2.compareTo(fecha1);
                } catch (Exception e) {
                    return 0;
                }
            })
            .limit(5)
            .toList();
    }
}

