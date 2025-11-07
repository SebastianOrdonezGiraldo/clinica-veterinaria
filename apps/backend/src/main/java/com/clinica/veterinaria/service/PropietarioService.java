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
 * Servicio para gestionar propietarios de mascotas (clientes de la clínica).
 * 
 * <p>Este servicio centraliza la lógica de negocio relacionada con los propietarios,
 * que son los dueños de las mascotas atendidas en la clínica. Proporciona operaciones
 * CRUD completas con validaciones de unicidad de documento.</p>
 * 
 * <p><strong>Información gestionada:</strong></p>
 * <ul>
 *   <li><b>Datos personales:</b> Nombre completo</li>
 *   <li><b>Identificación:</b> Documento (cédula, pasaporte) - único</li>
 *   <li><b>Contacto:</b> Email, teléfono, dirección</li>
 *   <li><b>Estado:</b> Activo/Inactivo (soft delete)</li>
 * </ul>
 * 
 * <p><strong>Validaciones de negocio:</strong></p>
 * <ul>
 *   <li>El documento de identidad debe ser único en el sistema</li>
 *   <li>El nombre es requerido</li>
 *   <li>Soft delete para preservar historial de pacientes asociados</li>
 * </ul>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Búsqueda parcial por nombre (case-insensitive)</li>
 *   <li>Paginación para listados grandes</li>
 *   <li>Validación de documento único en create/update</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see PropietarioDTO
 * @see Propietario
 * @see PropietarioRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PropietarioService {

    private final PropietarioRepository propietarioRepository;

    /**
     * Obtiene todos los propietarios registrados.
     * 
     * @return Lista completa de propietarios. Nunca es null, puede ser vacía.
     */
    @Transactional(readOnly = true)
    public List<PropietarioDTO> findAll() {
        log.debug("Obteniendo todos los propietarios");
        return propietarioRepository.findAll().stream()
            .map(PropietarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene propietarios con soporte de paginación.
     * 
     * @param pageable Configuración de paginación. No puede ser null.
     * @return Página de propietarios con metadatos de paginación.
     */
    @Transactional(readOnly = true)
    public Page<PropietarioDTO> findAll(Pageable pageable) {
        log.debug("Obteniendo propietarios con paginación");
        return propietarioRepository.findAll(pageable)
            .map(PropietarioDTO::fromEntity);
    }

    /**
     * Busca un propietario por su identificador.
     * 
     * @param id ID del propietario. No puede ser null.
     * @return DTO con la información completa del propietario, incluyendo lista de pacientes.
     * @throws RuntimeException si el propietario no existe.
     */
    @Transactional(readOnly = true)
    public PropietarioDTO findById(Long id) {
        log.debug("Buscando propietario con ID: {}", id);
        Propietario propietario = propietarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Propietario no encontrado con ID: " + id));
        return PropietarioDTO.fromEntity(propietario, true);
    }

    /**
     * Búsqueda parcial de propietarios por nombre (case-insensitive).
     * 
     * @param nombre Texto a buscar. No puede ser null.
     * @return Lista de propietarios cuyos nombres contienen el texto. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<PropietarioDTO> findByNombre(String nombre) {
        log.debug("Buscando propietarios con nombre: {}", nombre);
        return propietarioRepository.findByNombreContainingIgnoreCase(nombre).stream()
            .map(PropietarioDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Registra un nuevo propietario en el sistema.
     * 
     * <p>Valida que el documento sea único antes de crear el registro.</p>
     * 
     * @param dto Datos del nuevo propietario. No puede ser null.
     * @return DTO con los datos del propietario creado, incluyendo ID asignado.
     * @throws RuntimeException si el documento ya está registrado.
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
     * Actualiza la información de un propietario existente.
     * 
     * <p>Valida que el documento sea único si se modifica.</p>
     * 
     * @param id ID del propietario a actualizar. No puede ser null.
     * @param dto Nuevos datos del propietario. No puede ser null.
     * @return DTO con los datos actualizados.
     * @throws RuntimeException si el propietario no existe o el documento ya está registrado.
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
     * Desactiva un propietario del sistema (Soft Delete).
     * 
     * <p>Los propietarios no se eliminan físicamente para preservar la relación
     * con sus mascotas y el historial asociado.</p>
     * 
     * @param id ID del propietario a desactivar. No puede ser null.
     * @throws RuntimeException si el propietario no existe.
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
     * Cuenta el número de propietarios activos.
     * 
     * @return Número de propietarios activos en el sistema.
     */
    @Transactional(readOnly = true)
    public long countActivos() {
        return propietarioRepository.countActivos();
    }
}
