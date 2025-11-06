package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.PropietarioDTO;
import com.clinica.veterinaria.entity.Propietario;
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
 * Servicio para gestionar propietarios de mascotas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PropietarioService {

    private final PropietarioRepository propietarioRepository;

    /**
     * Obtiene todos los propietarios
     */
    @Transactional(readOnly = true)
    public List<PropietarioDTO> findAll() {
        log.debug("Obteniendo todos los propietarios");
        return propietarioRepository.findAll().stream()
            .map(PropietarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene propietarios con paginación
     */
    @Transactional(readOnly = true)
    public Page<PropietarioDTO> findAll(Pageable pageable) {
        log.debug("Obteniendo propietarios con paginación");
        return propietarioRepository.findAll(pageable)
            .map(PropietarioDTO::fromEntity);
    }

    /**
     * Obtiene un propietario por ID
     */
    @Transactional(readOnly = true)
    public PropietarioDTO findById(Long id) {
        log.debug("Buscando propietario con ID: {}", id);
        Propietario propietario = propietarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Propietario no encontrado con ID: " + id));
        return PropietarioDTO.fromEntity(propietario, true);
    }

    /**
     * Busca propietarios por nombre
     */
    @Transactional(readOnly = true)
    public List<PropietarioDTO> findByNombre(String nombre) {
        log.debug("Buscando propietarios con nombre: {}", nombre);
        return propietarioRepository.findByNombreContainingIgnoreCase(nombre).stream()
            .map(PropietarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo propietario
     */
    public PropietarioDTO create(PropietarioDTO dto) {
        log.info("Creando nuevo propietario: {}", dto.getNombre());
        
        // Validar documento único si se proporciona
        if (dto.getDocumento() != null 
            && propietarioRepository.existsByDocumento(dto.getDocumento())) {
            throw new RuntimeException("El documento ya está registrado: " + dto.getDocumento());
        }

        Propietario propietario = Propietario.builder()
            .nombre(dto.getNombre())
            .documento(dto.getDocumento())
            .email(dto.getEmail())
            .telefono(dto.getTelefono())
            .direccion(dto.getDireccion())
            .activo(true)
            .build();

        propietario = propietarioRepository.save(propietario);
        log.info("Propietario creado exitosamente con ID: {}", propietario.getId());
        
        return PropietarioDTO.fromEntity(propietario);
    }

    /**
     * Actualiza un propietario existente
     */
    public PropietarioDTO update(Long id, PropietarioDTO dto) {
        log.info("Actualizando propietario con ID: {}", id);
        
        Propietario propietario = propietarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Propietario no encontrado con ID: " + id));

        // Validar documento único si cambió
        if (dto.getDocumento() != null 
            && !dto.getDocumento().equals(propietario.getDocumento())
            && propietarioRepository.existsByDocumento(dto.getDocumento())) {
            throw new RuntimeException("El documento ya está registrado: " + dto.getDocumento());
        }

        propietario.setNombre(dto.getNombre());
        propietario.setDocumento(dto.getDocumento());
        propietario.setEmail(dto.getEmail());
        propietario.setTelefono(dto.getTelefono());
        propietario.setDireccion(dto.getDireccion());

        propietario = propietarioRepository.save(propietario);
        log.info("Propietario actualizado exitosamente con ID: {}", id);
        
        return PropietarioDTO.fromEntity(propietario);
    }

    /**
     * Elimina un propietario (soft delete)
     */
    public void delete(Long id) {
        log.info("Eliminando propietario con ID: {}", id);
        
        Propietario propietario = propietarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Propietario no encontrado con ID: " + id));
        
        propietario.setActivo(false);
        propietarioRepository.save(propietario);
        
        log.info("Propietario eliminado exitosamente con ID: {}", id);
    }

    /**
     * Cuenta propietarios activos
     */
    @Transactional(readOnly = true)
    public long countActivos() {
        return propietarioRepository.countActivos();
    }
}

