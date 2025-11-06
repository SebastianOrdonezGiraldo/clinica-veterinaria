package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ConsultaDTO;
import com.clinica.veterinaria.entity.Consulta;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.ConsultaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar consultas médicas (historia clínica)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene todas las consultas
     */
    @Transactional(readOnly = true)
    public List<ConsultaDTO> findAll() {
        log.debug("Obteniendo todas las consultas");
        return consultaRepository.findAll().stream()
            .map(c -> ConsultaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene una consulta por ID
     */
    @Transactional(readOnly = true)
    public ConsultaDTO findById(Long id) {
        log.debug("Buscando consulta con ID: {}", id);
        Consulta consulta = consultaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Consulta no encontrada con ID: " + id));
        return ConsultaDTO.fromEntity(consulta, true);
    }

    /**
     * Obtiene consultas por paciente (historia clínica)
     */
    @Transactional(readOnly = true)
    public List<ConsultaDTO> findByPaciente(Long pacienteId) {
        log.debug("Obteniendo historia clínica del paciente con ID: {}", pacienteId);
        return consultaRepository.findByPacienteIdOrderByFechaDesc(pacienteId).stream()
            .map(c -> ConsultaDTO.fromEntity(c, false))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene consultas por profesional
     */
    @Transactional(readOnly = true)
    public List<ConsultaDTO> findByProfesional(Long profesionalId) {
        log.debug("Buscando consultas del profesional con ID: {}", profesionalId);
        return consultaRepository.findByProfesionalId(profesionalId).stream()
            .map(c -> ConsultaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene consultas por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<ConsultaDTO> findByFechaRange(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Buscando consultas entre {} y {}", inicio, fin);
        return consultaRepository.findByFechaBetween(inicio, fin).stream()
            .map(c -> ConsultaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Crea una nueva consulta
     */
    public ConsultaDTO create(ConsultaDTO dto) {
        log.info("Creando nueva consulta para paciente ID: {}", dto.getPacienteId());
        
        // Validar entidades relacionadas
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + dto.getPacienteId()));
        
        Usuario profesional = usuarioRepository.findById(dto.getProfesionalId())
            .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + dto.getProfesionalId()));

        Consulta consulta = Consulta.builder()
            .fecha(dto.getFecha() != null ? dto.getFecha() : LocalDateTime.now())
            .frecuenciaCardiaca(dto.getFrecuenciaCardiaca())
            .frecuenciaRespiratoria(dto.getFrecuenciaRespiratoria())
            .temperatura(dto.getTemperatura())
            .pesoKg(dto.getPesoKg())
            .examenFisico(dto.getExamenFisico())
            .diagnostico(dto.getDiagnostico())
            .tratamiento(dto.getTratamiento())
            .observaciones(dto.getObservaciones())
            .paciente(paciente)
            .profesional(profesional)
            .build();

        consulta = consultaRepository.save(consulta);
        log.info("Consulta creada exitosamente con ID: {}", consulta.getId());
        
        return ConsultaDTO.fromEntity(consulta, true);
    }

    /**
     * Actualiza una consulta existente
     */
    public ConsultaDTO update(Long id, ConsultaDTO dto) {
        log.info("Actualizando consulta con ID: {}", id);
        
        Consulta consulta = consultaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Consulta no encontrada con ID: " + id));

        // Actualizar campos
        consulta.setFecha(dto.getFecha());
        consulta.setFrecuenciaCardiaca(dto.getFrecuenciaCardiaca());
        consulta.setFrecuenciaRespiratoria(dto.getFrecuenciaRespiratoria());
        consulta.setTemperatura(dto.getTemperatura());
        consulta.setPesoKg(dto.getPesoKg());
        consulta.setExamenFisico(dto.getExamenFisico());
        consulta.setDiagnostico(dto.getDiagnostico());
        consulta.setTratamiento(dto.getTratamiento());
        consulta.setObservaciones(dto.getObservaciones());

        // Actualizar profesional si cambió
        if (!consulta.getProfesional().getId().equals(dto.getProfesionalId())) {
            Usuario profesional = usuarioRepository.findById(dto.getProfesionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));
            consulta.setProfesional(profesional);
        }

        consulta = consultaRepository.save(consulta);
        log.info("Consulta actualizada exitosamente con ID: {}", id);
        
        return ConsultaDTO.fromEntity(consulta, true);
    }

    /**
     * Elimina una consulta
     */
    public void delete(Long id) {
        log.info("Eliminando consulta con ID: {}", id);
        
        if (!consultaRepository.existsById(id)) {
            throw new RuntimeException("Consulta no encontrada con ID: " + id);
        }
        
        consultaRepository.deleteById(id);
        log.info("Consulta eliminada exitosamente con ID: {}", id);
    }
}

