package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.PacienteDTO;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar pacientes (mascotas)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PropietarioRepository propietarioRepository;

    /**
     * Obtiene todos los pacientes
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> findAll() {
        log.debug("Obteniendo todos los pacientes");
        return pacienteRepository.findAll().stream()
            .map(p -> PacienteDTO.fromEntity(p, true))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene pacientes con paginación
     */
    @Transactional(readOnly = true)
    public Page<PacienteDTO> findAll(Pageable pageable) {
        log.debug("Obteniendo pacientes con paginación");
        return pacienteRepository.findAll(pageable)
            .map(p -> PacienteDTO.fromEntity(p, true));
    }

    /**
     * Obtiene un paciente por ID
     */
    @Transactional(readOnly = true)
    public PacienteDTO findById(Long id) {
        log.debug("Buscando paciente con ID: {}", id);
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
        return PacienteDTO.fromEntity(paciente, true);
    }

    /**
     * Obtiene pacientes por propietario
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> findByPropietario(Long propietarioId) {
        log.debug("Buscando pacientes del propietario con ID: {}", propietarioId);
        return pacienteRepository.findByPropietarioId(propietarioId).stream()
            .map(p -> PacienteDTO.fromEntity(p, false))
            .collect(Collectors.toList());
    }

    /**
     * Busca pacientes por nombre
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> findByNombre(String nombre) {
        log.debug("Buscando pacientes con nombre: {}", nombre);
        return pacienteRepository.findByNombreContainingIgnoreCase(nombre).stream()
            .map(p -> PacienteDTO.fromEntity(p, true))
            .collect(Collectors.toList());
    }

    /**
     * Busca pacientes por especie
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> findByEspecie(String especie) {
        log.debug("Buscando pacientes de especie: {}", especie);
        return pacienteRepository.findByEspecie(especie).stream()
            .map(p -> PacienteDTO.fromEntity(p, true))
            .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo paciente
     */
    public PacienteDTO create(PacienteDTO dto) {
        log.info("Creando nuevo paciente: {}", dto.getNombre());
        
        // Validar que el propietario existe
        Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
            .orElseThrow(() -> new RuntimeException("Propietario no encontrado con ID: " + dto.getPropietarioId()));

        Paciente paciente = Paciente.builder()
            .nombre(dto.getNombre())
            .especie(dto.getEspecie())
            .raza(dto.getRaza())
            .sexo(dto.getSexo())
            .edadMeses(dto.getEdadMeses())
            .pesoKg(dto.getPesoKg())
            .microchip(dto.getMicrochip())
            .notas(dto.getNotas())
            .propietario(propietario)
            .activo(true)
            .build();

        paciente = pacienteRepository.save(paciente);
        log.info("Paciente creado exitosamente con ID: {}", paciente.getId());
        
        return PacienteDTO.fromEntity(paciente, true);
    }

    /**
     * Actualiza un paciente existente
     */
    public PacienteDTO update(Long id, PacienteDTO dto) {
        log.info("Actualizando paciente con ID: {}", id);
        
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));

        // Validar propietario si cambió
        if (!paciente.getPropietario().getId().equals(dto.getPropietarioId())) {
            Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
                .orElseThrow(() -> new RuntimeException("Propietario no encontrado con ID: " + dto.getPropietarioId()));
            paciente.setPropietario(propietario);
        }

        paciente.setNombre(dto.getNombre());
        paciente.setEspecie(dto.getEspecie());
        paciente.setRaza(dto.getRaza());
        paciente.setSexo(dto.getSexo());
        paciente.setEdadMeses(dto.getEdadMeses());
        paciente.setPesoKg(dto.getPesoKg());
        paciente.setMicrochip(dto.getMicrochip());
        paciente.setNotas(dto.getNotas());

        paciente = pacienteRepository.save(paciente);
        log.info("Paciente actualizado exitosamente con ID: {}", id);
        
        return PacienteDTO.fromEntity(paciente, true);
    }

    /**
     * Elimina un paciente (soft delete)
     */
    public void delete(Long id) {
        log.info("Eliminando paciente con ID: {}", id);
        
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado con ID: " + id));
        
        paciente.setActivo(false);
        pacienteRepository.save(paciente);
        
        log.info("Paciente eliminado exitosamente con ID: {}", id);
    }

    /**
     * Cuenta pacientes activos
     */
    @Transactional(readOnly = true)
    public long countActivos() {
        return pacienteRepository.countActivos();
    }

    /**
     * Cuenta pacientes por especie
     */
    @Transactional(readOnly = true)
    public long countByEspecie(String especie) {
        return pacienteRepository.countByEspecie(especie);
    }
}

