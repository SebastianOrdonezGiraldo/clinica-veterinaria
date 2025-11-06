package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.CitaDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.entity.Usuario;
import com.clinica.veterinaria.repository.CitaRepository;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import com.clinica.veterinaria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar citas médicas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final PropietarioRepository propietarioRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene todas las citas
     */
    @Transactional(readOnly = true)
    public List<CitaDTO> findAll() {
        log.debug("Obteniendo todas las citas");
        return citaRepository.findAll().stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene una cita por ID
     */
    @Transactional(readOnly = true)
    public CitaDTO findById(Long id) {
        log.debug("Buscando cita con ID: {}", id);
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        return CitaDTO.fromEntity(cita, true);
    }

    /**
     * Obtiene citas por paciente
     */
    @Transactional(readOnly = true)
    public List<CitaDTO> findByPaciente(Long pacienteId) {
        log.debug("Buscando citas del paciente con ID: {}", pacienteId);
        return citaRepository.findByPacienteId(pacienteId).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene citas por profesional
     */
    @Transactional(readOnly = true)
    public List<CitaDTO> findByProfesional(Long profesionalId) {
        log.debug("Buscando citas del profesional con ID: {}", profesionalId);
        return citaRepository.findByProfesionalId(profesionalId).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene citas por estado
     */
    @Transactional(readOnly = true)
    public List<CitaDTO> findByEstado(Cita.EstadoCita estado) {
        log.debug("Buscando citas con estado: {}", estado);
        return citaRepository.findByEstado(estado).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene citas por rango de fechas
     */
    @Transactional(readOnly = true)
    public List<CitaDTO> findByFechaRange(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Buscando citas entre {} y {}", inicio, fin);
        return citaRepository.findByFechaBetween(inicio, fin).stream()
            .map(c -> CitaDTO.fromEntity(c, true))
            .collect(Collectors.toList());
    }

    /**
     * Crea una nueva cita
     */
    public CitaDTO create(CitaDTO dto) {
        log.info("Creando nueva cita para paciente ID: {}", dto.getPacienteId());
        
        // Validar entidades relacionadas
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + dto.getPacienteId()));
        
        Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
            .orElseThrow(() -> new RuntimeException("Propietario no encontrado con ID: " + dto.getPropietarioId()));
        
        Usuario profesional = usuarioRepository.findById(dto.getProfesionalId())
            .orElseThrow(() -> new RuntimeException("Profesional no encontrado con ID: " + dto.getProfesionalId()));

        Cita cita = Cita.builder()
            .fecha(dto.getFecha())
            .motivo(dto.getMotivo())
            .estado(dto.getEstado() != null ? dto.getEstado() : Cita.EstadoCita.PENDIENTE)
            .observaciones(dto.getObservaciones())
            .paciente(paciente)
            .propietario(propietario)
            .profesional(profesional)
            .build();

        cita = citaRepository.save(cita);
        log.info("Cita creada exitosamente con ID: {}", cita.getId());
        
        return CitaDTO.fromEntity(cita, true);
    }

    /**
     * Actualiza una cita existente
     */
    public CitaDTO update(Long id, CitaDTO dto) {
        log.info("Actualizando cita con ID: {}", id);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));

        // Actualizar campos básicos
        cita.setFecha(dto.getFecha());
        cita.setMotivo(dto.getMotivo());
        cita.setEstado(dto.getEstado());
        cita.setObservaciones(dto.getObservaciones());

        // Actualizar relaciones si cambiaron
        if (!cita.getPaciente().getId().equals(dto.getPacienteId())) {
            Paciente paciente = pacienteRepository.findById(dto.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));
            cita.setPaciente(paciente);
        }

        if (!cita.getPropietario().getId().equals(dto.getPropietarioId())) {
            Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
                .orElseThrow(() -> new RuntimeException("Propietario no encontrado"));
            cita.setPropietario(propietario);
        }

        if (!cita.getProfesional().getId().equals(dto.getProfesionalId())) {
            Usuario profesional = usuarioRepository.findById(dto.getProfesionalId())
                .orElseThrow(() -> new RuntimeException("Profesional no encontrado"));
            cita.setProfesional(profesional);
        }

        cita = citaRepository.save(cita);
        log.info("Cita actualizada exitosamente con ID: {}", id);
        
        return CitaDTO.fromEntity(cita, true);
    }

    /**
     * Cambia el estado de una cita
     */
    public CitaDTO cambiarEstado(Long id, Cita.EstadoCita nuevoEstado) {
        log.info("Cambiando estado de cita ID: {} a {}", id, nuevoEstado);
        
        Cita cita = citaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita no encontrada con ID: " + id));
        
        cita.setEstado(nuevoEstado);
        cita = citaRepository.save(cita);
        
        return CitaDTO.fromEntity(cita, true);
    }

    /**
     * Elimina una cita
     */
    public void delete(Long id) {
        log.info("Eliminando cita con ID: {}", id);
        
        if (!citaRepository.existsById(id)) {
            throw new RuntimeException("Cita no encontrada con ID: " + id);
        }
        
        citaRepository.deleteById(id);
        log.info("Cita eliminada exitosamente con ID: {}", id);
    }
}

