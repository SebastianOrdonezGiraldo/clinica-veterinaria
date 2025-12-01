package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.*;
import com.clinica.veterinaria.entity.*;
import com.clinica.veterinaria.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar el historial médico completo de pacientes
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HistorialMedicoService {

    private final ConsultaRepository consultaRepository;
    private final PrescripcionRepository prescripcionRepository;
    private final PacienteRepository pacienteRepository;

    /**
     * Obtiene el historial médico completo de un paciente
     */
    public HistorialMedicoDTO getHistorialCompleto(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        List<Consulta> consultas = consultaRepository.findByPacienteIdOrderByFechaDesc(pacienteId);

        return HistorialMedicoDTO.builder()
            .pacienteId(pacienteId)
            .pacienteNombre(paciente.getNombre())
            .timelineConsultas(obtenerTimelineConsultas(consultas))
            .evolucionSignosVitales(obtenerEvolucionSignosVitales(consultas))
            .historialMedicamentos(obtenerHistorialMedicamentos(pacienteId))
            .resumen(calcularResumen(consultas, pacienteId))
            .build();
    }

    /**
     * Obtiene el timeline de consultas
     */
    private List<ConsultaTimelineDTO> obtenerTimelineConsultas(List<Consulta> consultas) {
        return consultas.stream()
            .map(consulta -> {
                List<Prescripcion> prescripciones = prescripcionRepository.findByConsultaId(consulta.getId());
                
                return ConsultaTimelineDTO.builder()
                    .id(consulta.getId())
                    .fecha(consulta.getFecha())
                    .profesionalNombre(consulta.getProfesional().getNombre())
                    .diagnostico(consulta.getDiagnostico())
                    .tratamiento(consulta.getTratamiento())
                    .temperatura(consulta.getTemperatura())
                    .pesoKg(consulta.getPesoKg())
                    .frecuenciaCardiaca(consulta.getFrecuenciaCardiaca())
                    .frecuenciaRespiratoria(consulta.getFrecuenciaRespiratoria())
                    .tienePrescripciones(!prescripciones.isEmpty())
                    .cantidadPrescripciones(prescripciones.size())
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Obtiene la evolución de signos vitales
     */
    private List<EvolucionSignosVitalesDTO> obtenerEvolucionSignosVitales(List<Consulta> consultas) {
        return consultas.stream()
            .filter(c -> c.getPesoKg() != null || c.getTemperatura() != null || 
                        c.getFrecuenciaCardiaca() != null || c.getFrecuenciaRespiratoria() != null)
            .map(consulta -> EvolucionSignosVitalesDTO.builder()
                .fecha(consulta.getFecha())
                .pesoKg(consulta.getPesoKg())
                .temperatura(consulta.getTemperatura())
                .frecuenciaCardiaca(consulta.getFrecuenciaCardiaca())
                .frecuenciaRespiratoria(consulta.getFrecuenciaRespiratoria())
                .build())
            .sorted(Comparator.comparing(EvolucionSignosVitalesDTO::getFecha))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene el historial de medicamentos
     */
    private List<HistorialMedicamentoDTO> obtenerHistorialMedicamentos(Long pacienteId) {
        List<Consulta> consultas = consultaRepository.findByPacienteIdOrderByFechaDesc(pacienteId);
        List<HistorialMedicamentoDTO> historial = new ArrayList<>();

        for (Consulta consulta : consultas) {
            List<Prescripcion> prescripciones = prescripcionRepository.findByConsultaId(consulta.getId());
            
            for (Prescripcion prescripcion : prescripciones) {
                for (ItemPrescripcion item : prescripcion.getItems()) {
                    historial.add(HistorialMedicamentoDTO.builder()
                        .medicamento(item.getMedicamento())
                        .presentacion(item.getPresentacion())
                        .dosis(item.getDosis())
                        .frecuencia(item.getFrecuencia())
                        .duracion(item.getDuracionDias() != null ? item.getDuracionDias() + " días" : null)
                        .fechaPrescripcion(prescripcion.getFechaEmision())
                        .diagnostico(consulta.getDiagnostico())
                        .profesionalNombre(consulta.getProfesional().getNombre())
                        .consultaId(consulta.getId())
                        .build());
                }
            }
        }

        return historial.stream()
            .sorted(Comparator.comparing(HistorialMedicamentoDTO::getFechaPrescripcion).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Calcula el resumen médico
     */
    private ResumenMedicoDTO calcularResumen(List<Consulta> consultas, Long pacienteId) {
        if (consultas.isEmpty()) {
            return ResumenMedicoDTO.builder()
                .totalConsultas(0)
                .totalPrescripciones(0)
                .diagnosticosFrecuentes(new ArrayList<>())
                .medicamentosMasUsados(new ArrayList<>())
                .build();
        }

        // Fechas
        LocalDateTime primeraConsulta = consultas.stream()
            .map(Consulta::getFecha)
            .min(LocalDateTime::compareTo)
            .orElse(null);
        
        LocalDateTime ultimaConsulta = consultas.stream()
            .map(Consulta::getFecha)
            .max(LocalDateTime::compareTo)
            .orElse(null);

        // Pesos
        List<BigDecimal> pesos = consultas.stream()
            .map(Consulta::getPesoKg)
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toList());
        
        BigDecimal pesoInicial = pesos.isEmpty() ? null : pesos.get(0);
        BigDecimal pesoActual = pesos.isEmpty() ? null : pesos.get(pesos.size() - 1);
        BigDecimal variacionPeso = (pesoInicial != null && pesoActual != null) ? 
            pesoActual.subtract(pesoInicial) : null;

        // Temperatura promedio
        BigDecimal temperaturaPromedio = consultas.stream()
            .map(Consulta::getTemperatura)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (consultas.stream().anyMatch(c -> c.getTemperatura() != null)) {
            long count = consultas.stream().filter(c -> c.getTemperatura() != null).count();
            temperaturaPromedio = temperaturaPromedio.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        } else {
            temperaturaPromedio = null;
        }

        // Frecuencia cardíaca promedio
        Integer frecuenciaCardiacaPromedio = consultas.stream()
            .map(Consulta::getFrecuenciaCardiaca)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .stream()
            .mapToInt(d -> (int) Math.round(d))
            .findFirst()
            .orElse(0);
        if (frecuenciaCardiacaPromedio == 0) {
            frecuenciaCardiacaPromedio = null;
        }

        // Frecuencia respiratoria promedio
        Integer frecuenciaRespiratoriaPromedio = consultas.stream()
            .map(Consulta::getFrecuenciaRespiratoria)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .average()
            .stream()
            .mapToInt(d -> (int) Math.round(d))
            .findFirst()
            .orElse(0);
        if (frecuenciaRespiratoriaPromedio == 0) {
            frecuenciaRespiratoriaPromedio = null;
        }

        // Total prescripciones
        int totalPrescripciones = (int) consultas.stream()
            .mapToLong(c -> prescripcionRepository.findByConsultaId(c.getId()).size())
            .sum();

        // Diagnósticos más frecuentes
        Map<String, Long> diagnosticosCount = consultas.stream()
            .map(Consulta::getDiagnostico)
            .filter(Objects::nonNull)
            .filter(d -> !d.trim().isEmpty())
            .collect(Collectors.groupingBy(d -> d, Collectors.counting()));
        
        List<String> diagnosticosFrecuentes = diagnosticosCount.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        // Medicamentos más usados
        List<Prescripcion> todasPrescripciones = consultas.stream()
            .flatMap(c -> prescripcionRepository.findByConsultaId(c.getId()).stream())
            .collect(Collectors.toList());
        
        Map<String, Long> medicamentosCount = todasPrescripciones.stream()
            .flatMap(p -> p.getItems().stream())
            .map(ItemPrescripcion::getMedicamento)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(m -> m, Collectors.counting()));
        
        List<String> medicamentosMasUsados = medicamentosCount.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        return ResumenMedicoDTO.builder()
            .totalConsultas(consultas.size())
            .primeraConsulta(primeraConsulta)
            .ultimaConsulta(ultimaConsulta)
            .pesoInicial(pesoInicial)
            .pesoActual(pesoActual)
            .variacionPeso(variacionPeso)
            .temperaturaPromedio(temperaturaPromedio)
            .frecuenciaCardiacaPromedio(frecuenciaCardiacaPromedio)
            .frecuenciaRespiratoriaPromedio(frecuenciaRespiratoriaPromedio)
            .totalPrescripciones(totalPrescripciones)
            .diagnosticosFrecuentes(diagnosticosFrecuentes)
            .medicamentosMasUsados(medicamentosMasUsados)
            .build();
    }
}

