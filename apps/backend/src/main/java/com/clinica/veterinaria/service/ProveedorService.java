package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.ProveedorDTO;
import com.clinica.veterinaria.entity.Proveedor;
import com.clinica.veterinaria.exception.domain.DuplicateResourceException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar proveedores de productos.
 * 
 * <p>Este servicio centraliza toda la lógica de negocio relacionada con los proveedores.
 * Proporciona operaciones CRUD completas y funcionalidades de auditoría.</p>
 * 
 * <p><strong>Funcionalidades principales:</strong></p>
 * <ul>
 *   <li>Crear, actualizar y eliminar (soft delete) proveedores</li>
 *   <li>Búsqueda por nombre, email, RUC</li>
 *   <li>Listado de proveedores activos</li>
 *   <li>Validación de duplicados</li>
 *   <li>Auditoría completa de operaciones</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-29
 * @see Proveedor
 * @see ProveedorDTO
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final IAuditLogger auditLogger;

    /**
     * Obtiene todos los proveedores activos, ordenados por nombre.
     * 
     * @return Lista de proveedores activos
     */
    @Transactional(readOnly = true)
    public List<ProveedorDTO> findAllActivos() {
        log.debug("Obteniendo todos los proveedores activos");
        return proveedorRepository.findByActivoTrueOrderByNombreAsc().stream()
            .map(ProveedorDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todos los proveedores (activos e inactivos).
     * 
     * @return Lista completa de proveedores
     */
    @Transactional(readOnly = true)
    public List<ProveedorDTO> findAll() {
        log.debug("Obteniendo todos los proveedores");
        return proveedorRepository.findAll().stream()
            .map(ProveedorDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Busca un proveedor por su ID.
     * 
     * @param id ID del proveedor
     * @return DTO del proveedor
     * @throws ResourceNotFoundException si no existe el proveedor
     */
    @Transactional(readOnly = true)
    public ProveedorDTO findById(@NonNull Long id) {
        log.debug("Buscando proveedor con ID: {}", id);
        Proveedor proveedor = proveedorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));
        return ProveedorDTO.fromEntity(proveedor);
    }

    /**
     * Busca proveedores por nombre (búsqueda parcial, case-insensitive).
     * 
     * @param nombre Texto a buscar en el nombre
     * @param soloActivos Si es true, solo busca proveedores activos
     * @return Lista de proveedores que coinciden
     */
    @Transactional(readOnly = true)
    public List<ProveedorDTO> buscarPorNombre(String nombre, boolean soloActivos) {
        log.debug("Buscando proveedores con nombre: {}", nombre);
        return proveedorRepository.findByNombreContainingIgnoreCaseAndActivoOrderByNombreAsc(nombre, soloActivos)
            .stream()
            .map(ProveedorDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo proveedor.
     * 
     * @param dto DTO con los datos del proveedor
     * @return DTO del proveedor creado
     * @throws DuplicateResourceException si ya existe un proveedor con ese email o RUC
     */
    public ProveedorDTO create(@NonNull ProveedorDTO dto) {
        log.info("→ Creando nuevo proveedor: {}", dto.getNombre());

        // Validar email único (si se proporciona)
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty() &&
            proveedorRepository.existsByEmail(dto.getEmail())) {
            log.warn("✗ Proveedor con email duplicado: {}", dto.getEmail());
            throw new DuplicateResourceException("Proveedor", "email", dto.getEmail());
        }

        // Validar RUC único (si se proporciona)
        if (dto.getRuc() != null && !dto.getRuc().trim().isEmpty() &&
            proveedorRepository.existsByRuc(dto.getRuc())) {
            log.warn("✗ Proveedor con RUC duplicado: {}", dto.getRuc());
            throw new DuplicateResourceException("Proveedor", "ruc", dto.getRuc());
        }

        Proveedor proveedor = dto.toEntity();
        proveedor = proveedorRepository.save(proveedor);
        log.info("✓ Proveedor creado exitosamente con ID: {}", proveedor.getId());

        auditLogger.logCreate("Proveedor", proveedor.getId(), proveedor.getNombre());
        return ProveedorDTO.fromEntity(proveedor);
    }

    /**
     * Actualiza un proveedor existente.
     * 
     * @param id ID del proveedor a actualizar
     * @param dto DTO con los nuevos datos
     * @return DTO del proveedor actualizado
     * @throws ResourceNotFoundException si no existe el proveedor
     * @throws DuplicateResourceException si el nuevo email o RUC ya existe en otro proveedor
     */
    public ProveedorDTO update(@NonNull Long id, @NonNull ProveedorDTO dto) {
        log.info("→ Actualizando proveedor con ID: {}", id);

        Proveedor proveedor = proveedorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));

        // Validar email único (si cambió)
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty() &&
            !dto.getEmail().equalsIgnoreCase(proveedor.getEmail()) &&
            proveedorRepository.existsByEmail(dto.getEmail())) {
            log.warn("✗ Email de proveedor duplicado: {}", dto.getEmail());
            throw new DuplicateResourceException("Proveedor", "email", dto.getEmail());
        }

        // Validar RUC único (si cambió)
        if (dto.getRuc() != null && !dto.getRuc().trim().isEmpty() &&
            !dto.getRuc().equals(proveedor.getRuc()) &&
            proveedorRepository.existsByRuc(dto.getRuc())) {
            log.warn("✗ RUC de proveedor duplicado: {}", dto.getRuc());
            throw new DuplicateResourceException("Proveedor", "ruc", dto.getRuc());
        }

        proveedor.setNombre(dto.getNombre());
        proveedor.setRuc(dto.getRuc());
        proveedor.setEmail(dto.getEmail());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setDireccion(dto.getDireccion());
        proveedor.setNotas(dto.getNotas());
        if (dto.getActivo() != null) {
            proveedor.setActivo(dto.getActivo());
        }

        String oldData = String.format("Nombre: %s, Email: %s", proveedor.getNombre(), proveedor.getEmail());
        proveedor = proveedorRepository.save(proveedor);
        String newData = String.format("Nombre: %s, Email: %s", proveedor.getNombre(), proveedor.getEmail());
        log.info("✓ Proveedor actualizado exitosamente");

        auditLogger.logUpdate("Proveedor", proveedor.getId(), oldData, newData);
        return ProveedorDTO.fromEntity(proveedor);
    }

    /**
     * Desactiva un proveedor (soft delete).
     * 
     * @param id ID del proveedor a desactivar
     * @throws ResourceNotFoundException si no existe el proveedor
     */
    public void delete(@NonNull Long id) {
        log.info("→ Desactivando proveedor con ID: {}", id);

        Proveedor proveedor = proveedorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Proveedor", "id", id));

        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
        log.info("✓ Proveedor desactivado exitosamente");

        auditLogger.logDelete("Proveedor", proveedor.getId());
    }
}

