package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.PropietarioDTO;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.exception.domain.DuplicateResourceException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.PropietarioRepository;
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
    public Page<PropietarioDTO> findAll(@NonNull Pageable pageable) {
        log.debug("Obteniendo propietarios con paginación");
        return propietarioRepository.findAll(pageable)
            .map(PropietarioDTO::fromEntity);
    }

    /**
     * Busca un propietario por su identificador.
     * 
     * @param id ID del propietario. No puede ser null.
     * @return DTO con la información completa del propietario, incluyendo lista de pacientes.
     * @throws ResourceNotFoundException si el propietario no existe.
     */
    @Cacheable(value = "propietarios", key = "#id")
    @Transactional(readOnly = true)
    public PropietarioDTO findById(@NonNull Long id) {
        log.debug("Buscando propietario con ID: {}", id);
        Propietario propietario = propietarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Propietario", "id", id));
        return PropietarioDTO.fromEntity(propietario, true);
    }

    /**
     * Búsqueda parcial de propietarios por nombre (case-insensitive).
     * 
     * @param nombre Texto a buscar. No puede ser null.
     * @return Lista de propietarios cuyos nombres contienen el texto. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<PropietarioDTO> findByNombre(@NonNull String nombre) {
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
     * <p><strong>CACHE:</strong> Invalida el caché de propietarios.</p>
     * 
     * @param dto Datos del nuevo propietario. No puede ser null.
     * @return DTO con los datos del propietario creado, incluyendo ID asignado.
     * @throws DuplicateResourceException si el documento ya está registrado.
     */
    @CacheEvict(value = "propietarios", allEntries = true)
    @SuppressWarnings("null") // Los valores del DTO son validados antes de usar
    public PropietarioDTO create(@NonNull PropietarioDTO dto) {
        log.info("→ Creando nuevo propietario: {}", dto.getNombre());
        
        // VALIDACIÓN: Documento único si se proporciona
        if (dto.getDocumento() != null && !dto.getDocumento().trim().isEmpty()) {
            if (propietarioRepository.existsByDocumento(dto.getDocumento())) {
                log.error("✗ Documento duplicado: {}", dto.getDocumento());
                throw new DuplicateResourceException("Propietario", "documento", dto.getDocumento());
            }
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
        log.info("✓ Propietario creado exitosamente con ID: {} | Nombre: {}", 
                propietario.getId(), propietario.getNombre());
        
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
     * @throws ResourceNotFoundException si el propietario no existe.
     * @throws DuplicateResourceException si el documento ya está registrado.
     */
    @CacheEvict(value = "propietarios", allEntries = true)
    @SuppressWarnings("null") // Los valores del DTO son validados antes de usar
    public PropietarioDTO update(@NonNull Long id, @NonNull PropietarioDTO dto) {
        log.info("→ Actualizando propietario con ID: {}", id);
        
        Propietario propietario = propietarioRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Propietario no encontrado con ID: {}", id);
                return new ResourceNotFoundException("Propietario", "id", id);
            });

        // VALIDACIÓN: Documento único si cambió
        if (dto.getDocumento() != null && !dto.getDocumento().trim().isEmpty()) {
            if (!dto.getDocumento().equals(propietario.getDocumento())
                && propietarioRepository.existsByDocumento(dto.getDocumento())) {
                log.error("✗ Documento duplicado: {}", dto.getDocumento());
                throw new DuplicateResourceException("Propietario", "documento", dto.getDocumento());
            }
        }

        propietario.setNombre(dto.getNombre());
        propietario.setDocumento(dto.getDocumento());
        propietario.setEmail(dto.getEmail());
        propietario.setTelefono(dto.getTelefono());
        propietario.setDireccion(dto.getDireccion());

        propietario = propietarioRepository.save(propietario);
        log.info("✓ Propietario actualizado exitosamente con ID: {}", id);
        
        return PropietarioDTO.fromEntity(propietario);
    }

    /**
     * Desactiva un propietario del sistema (Soft Delete).
     * 
     * <p>Los propietarios no se eliminan físicamente para preservar la relación
     * con sus mascotas y el historial asociado.</p>
     * 
     * <p><strong>CACHE:</strong> Invalida el caché de propietarios.</p>
     * 
     * @param id ID del propietario a desactivar. No puede ser null.
     * @throws ResourceNotFoundException si el propietario no existe.
     */
    @CacheEvict(value = "propietarios", allEntries = true)
    public void delete(@NonNull Long id) {
        log.warn("→ Eliminando propietario con ID: {}", id);
        
        Propietario propietario = propietarioRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Propietario no encontrado con ID: {}", id);
                return new ResourceNotFoundException("Propietario", "id", id);
            });
        
        propietario.setActivo(false);
        propietarioRepository.save(propietario);
        
        log.warn("⚠ Propietario desactivado con ID: {}", id);
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
    
    /**
     * Busca propietarios con filtros combinados y paginación del lado del servidor.
     * 
     * <p><strong>PATRÓN STRATEGY:</strong> Este método implementa el patrón Strategy para
     * seleccionar dinámicamente el query apropiado según los filtros proporcionados.
     * Evita múltiples ifs anidados y código duplicado al delegar la decisión de qué
     * método del repository usar basándose en los parámetros no nulos.</p>
     * 
     * <p><strong>Optimización:</strong> La paginación se realiza en la base de datos,
     * no en memoria, lo que permite manejar eficientemente datasets grandes. Solo se
     * transfieren y procesan los registros de la página solicitada.</p>
     * 
     * <p><strong>Casos de uso soportados:</strong></p>
     * <ul>
     *   <li><b>Sin filtros:</b> Retorna todos los propietarios paginados</li>
     *   <li><b>Por nombre:</b> Búsqueda parcial case-insensitive</li>
     *   <li><b>Por documento:</b> Búsqueda parcial en documento de identidad</li>
     *   <li><b>Por teléfono:</b> Búsqueda parcial en número telefónico</li>
     *   <li><b>Nombre + documento:</b> Búsqueda combinada para mayor precisión</li>
     * </ul>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * // Caso 1: Buscar por nombre
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("nombre"));
     * Page&lt;PropietarioDTO&gt; result = service.searchWithFilters("Juan", null, null, pageable);
     * 
     * // Caso 2: Buscar por documento
     * result = service.searchWithFilters(null, "12345", null, pageable);
     * 
     * // Caso 3: Buscar por nombre y documento (más preciso)
     * result = service.searchWithFilters("Juan", "12345", null, pageable);
     * 
     * // Caso 4: Buscar por teléfono
     * result = service.searchWithFilters(null, null, "555", pageable);
     * 
     * // Caso 5: Todos los propietarios (sin filtros)
     * result = service.searchWithFilters(null, null, null, pageable);
     * </pre>
     * 
     * <p><strong>Ventajas del enfoque Strategy:</strong></p>
     * <ul>
     *   <li>Código más limpio y mantenible</li>
     *   <li>Fácil agregar nuevos filtros sin modificar la lógica existente</li>
     *   <li>Cada query está optimizado para su caso de uso específico</li>
     *   <li>Evita queries complejos con múltiples OR que afectan performance</li>
     * </ul>
     * 
     * @param nombre Texto a buscar en el nombre (opcional, null = sin filtro)
     * @param documento Texto a buscar en el documento (opcional, null = sin filtro)
     * @param telefono Texto a buscar en el teléfono (opcional, null = sin filtro)
     * @param pageable Configuración de paginación y ordenamiento. No puede ser null.
     * @return Página de propietarios que cumplen los criterios de búsqueda
     */
    @Transactional(readOnly = true)
    public Page<PropietarioDTO> searchWithFilters(
            String nombre, 
            String documento, 
            String telefono,
            Pageable pageable) {
        
        log.debug("Buscando propietarios con filtros - nombre: {}, documento: {}, telefono: {}, page: {}", 
            nombre, documento, telefono, pageable.getPageNumber());
        
        Page<Propietario> propietarios;
        
        // STRATEGY PATTERN: Selección dinámica del query apropiado
        
        // Estrategia 1: Búsqueda combinada nombre + documento (más precisa)
        if (isNotEmpty(nombre) && isNotEmpty(documento)) {
            log.debug("Estrategia: Búsqueda por nombre y documento");
            propietarios = propietarioRepository
                .findByNombreContainingIgnoreCaseAndDocumentoContaining(nombre, documento, pageable);
        }
        // Estrategia 2: Búsqueda solo por nombre
        else if (isNotEmpty(nombre)) {
            log.debug("Estrategia: Búsqueda solo por nombre");
            propietarios = propietarioRepository
                .findByNombreContainingIgnoreCase(nombre, pageable);
        }
        // Estrategia 3: Búsqueda solo por documento
        else if (isNotEmpty(documento)) {
            log.debug("Estrategia: Búsqueda solo por documento");
            propietarios = propietarioRepository
                .findByDocumentoContaining(documento, pageable);
        }
        // Estrategia 4: Búsqueda solo por teléfono
        else if (isNotEmpty(telefono)) {
            log.debug("Estrategia: Búsqueda solo por teléfono");
            propietarios = propietarioRepository
                .findByTelefonoContaining(telefono, pageable);
        }
        // Estrategia 5: Sin filtros, todos los propietarios
        else {
            log.debug("Estrategia: Sin filtros, retornar todos");
            propietarios = propietarioRepository.findAll(pageable);
        }
        
        log.debug("Propietarios encontrados: {} en página {} de {}", 
            propietarios.getNumberOfElements(), 
            propietarios.getNumber() + 1, 
            propietarios.getTotalPages());
        
        return propietarios.map(PropietarioDTO::fromEntity);
    }
    
    /**
     * Método helper para validar strings no vacíos.
     * 
     * <p>Parte del patrón Null Object: evita NullPointerException
     * y simplifica las validaciones de strings.</p>
     * 
     * @param value String a validar
     * @return true si el string no es null y no está vacío (después de trim)
     */
    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
