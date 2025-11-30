package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.VacunaDTO;
import com.clinica.veterinaria.entity.Vacuna;
import com.clinica.veterinaria.exception.domain.InvalidDataException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.VacunaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar tipos de vacunas disponibles en la clínica.
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VacunaService {

    private final VacunaRepository vacunaRepository;
    private final IAuditLogger auditLogger;

    @Transactional(readOnly = true)
    public List<VacunaDTO> findAll() {
        log.debug("Obteniendo todas las vacunas");
        return vacunaRepository.findAll().stream()
            .map(VacunaDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<VacunaDTO> findAll(@NonNull Pageable pageable) {
        log.debug("Obteniendo vacunas con paginación");
        return vacunaRepository.findAll(pageable)
            .map(VacunaDTO::fromEntity);
    }

    @Cacheable(value = "vacunas", key = "#id")
    @Transactional(readOnly = true)
    public VacunaDTO findById(@NonNull Long id) {
        log.debug("Buscando vacuna con ID: {}", id);
        Vacuna vacuna = vacunaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Vacuna", "id", id));
        return VacunaDTO.fromEntity(vacuna);
    }

    @Transactional(readOnly = true)
    public List<VacunaDTO> findByEspecie(@NonNull String especie) {
        log.debug("Buscando vacunas para especie: {}", especie);
        return vacunaRepository.findByEspecieAndActivo(especie, true).stream()
            .map(VacunaDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<VacunaDTO> findActivas() {
        log.debug("Buscando vacunas activas");
        return vacunaRepository.findByActivo(true).stream()
            .map(VacunaDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @CacheEvict(value = "vacunas", allEntries = true)
    public VacunaDTO create(@NonNull VacunaDTO dto) {
        log.info("→ Creando nueva vacuna: {}", dto.getNombre());

        // Validación: número de dosis debe ser positivo
        if (dto.getNumeroDosis() != null && dto.getNumeroDosis() <= 0) {
            throw new InvalidDataException("numeroDosis", dto.getNumeroDosis(),
                "El número de dosis debe ser mayor a 0");
        }

        // Validación: intervalo debe ser positivo si se proporciona
        if (dto.getIntervaloDias() != null && dto.getIntervaloDias() <= 0) {
            throw new InvalidDataException("intervaloDias", dto.getIntervaloDias(),
                "El intervalo entre dosis debe ser mayor a 0");
        }

        Vacuna vacuna = Vacuna.builder()
            .nombre(dto.getNombre())
            .especie(dto.getEspecie())
            .numeroDosis(dto.getNumeroDosis())
            .intervaloDias(dto.getIntervaloDias())
            .descripcion(dto.getDescripcion())
            .fabricante(dto.getFabricante())
            .activo(dto.getActivo() != null ? dto.getActivo() : true)
            .build();

        vacuna = vacunaRepository.save(vacuna);
        log.info("✓ Vacuna creada exitosamente con ID: {} | Nombre: {}", vacuna.getId(), vacuna.getNombre());

        auditLogger.logCreate("Vacuna", vacuna.getId(),
            String.format("Nombre: %s, Especie: %s, Dosis: %d", vacuna.getNombre(), vacuna.getEspecie(), vacuna.getNumeroDosis()));

        return VacunaDTO.fromEntity(vacuna);
    }

    @CacheEvict(value = "vacunas", allEntries = true)
    public VacunaDTO update(@NonNull Long id, @NonNull VacunaDTO dto) {
        log.info("→ Actualizando vacuna con ID: {}", id);

        Vacuna vacuna = vacunaRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Vacuna no encontrada con ID: {}", id);
                return new ResourceNotFoundException("Vacuna", "id", id);
            });

        String oldData = String.format("Nombre: %s, Especie: %s, Dosis: %d",
            vacuna.getNombre(), vacuna.getEspecie(), vacuna.getNumeroDosis());

        // Validaciones
        if (dto.getNumeroDosis() != null && dto.getNumeroDosis() <= 0) {
            throw new InvalidDataException("numeroDosis", dto.getNumeroDosis(),
                "El número de dosis debe ser mayor a 0");
        }

        if (dto.getIntervaloDias() != null && dto.getIntervaloDias() <= 0) {
            throw new InvalidDataException("intervaloDias", dto.getIntervaloDias(),
                "El intervalo entre dosis debe ser mayor a 0");
        }

        vacuna.setNombre(dto.getNombre());
        vacuna.setEspecie(dto.getEspecie());
        vacuna.setNumeroDosis(dto.getNumeroDosis());
        vacuna.setIntervaloDias(dto.getIntervaloDias());
        vacuna.setDescripcion(dto.getDescripcion());
        vacuna.setFabricante(dto.getFabricante());
        if (dto.getActivo() != null) {
            vacuna.setActivo(dto.getActivo());
        }

        vacuna = vacunaRepository.save(vacuna);

        String newData = String.format("Nombre: %s, Especie: %s, Dosis: %d",
            vacuna.getNombre(), vacuna.getEspecie(), vacuna.getNumeroDosis());

        log.info("✓ Vacuna actualizada exitosamente con ID: {}", id);
        auditLogger.logUpdate("Vacuna", id, oldData, newData);

        return VacunaDTO.fromEntity(vacuna);
    }

    @CacheEvict(value = "vacunas", allEntries = true)
    public void delete(@NonNull Long id) {
        log.warn("→ Eliminando vacuna con ID: {}", id);

        Vacuna vacuna = vacunaRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Vacuna no encontrada con ID: {}", id);
                return new ResourceNotFoundException("Vacuna", "id", id);
            });

        String vacunaInfo = String.format("%s (Especie: %s)", vacuna.getNombre(), vacuna.getEspecie());

        vacuna.setActivo(false);
        vacunaRepository.save(vacuna);

        log.warn("⚠ Vacuna desactivada: {}", vacunaInfo);
        auditLogger.logDelete("Vacuna", id);
    }

    @Transactional(readOnly = true)
    public Page<VacunaDTO> searchWithFilters(String nombre, String especie, @NonNull Pageable pageable) {
        log.debug("Buscando vacunas con filtros - nombre: {}, especie: {}, page: {}",
            nombre, especie, pageable.getPageNumber());

        Page<Vacuna> vacunas = vacunaRepository.findByFilters(
            nombre != null && !nombre.trim().isEmpty() ? nombre.trim() : null,
            especie != null && !especie.trim().isEmpty() ? especie.trim() : null,
            true, // Solo activas
            pageable
        );

        return vacunas.map(VacunaDTO::fromEntity);
    }
}

