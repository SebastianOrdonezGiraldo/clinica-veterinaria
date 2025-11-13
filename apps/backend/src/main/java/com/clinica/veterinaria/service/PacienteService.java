package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.PacienteDTO;
import com.clinica.veterinaria.entity.Paciente;
import com.clinica.veterinaria.entity.Propietario;
import com.clinica.veterinaria.exception.domain.DuplicateResourceException;
import com.clinica.veterinaria.exception.domain.InvalidDataException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.logging.IAuditLogger;
import com.clinica.veterinaria.repository.PacienteRepository;
import com.clinica.veterinaria.repository.PropietarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar pacientes veterinarios (mascotas).
 * 
 * <p>Este servicio centraliza toda la lógica de negocio relacionada con los pacientes,
 * que son los animales atendidos en la clínica veterinaria. Proporciona operaciones CRUD
 * completas, búsquedas especializadas, y funcionalidades de auditoría integrada.</p>
 * 
 * <p><strong>Información gestionada de cada paciente:</strong></p>
 * <ul>
 *   <li><b>Identificación:</b> Nombre, microchip (opcional)</li>
 *   <li><b>Características:</b> Especie, raza, sexo, edad (en meses)</li>
 *   <li><b>Estado:</b> Peso actual (kg), estado activo/inactivo</li>
 *   <li><b>Relaciones:</b> Propietario asociado</li>
 *   <li><b>Observaciones:</b> Notas clínicas y administrativas</li>
 * </ul>
 * 
 * <p><strong>Características especiales:</strong></p>
 * <ul>
 *   <li><b>Soft Delete:</b> Los pacientes no se eliminan físicamente, solo se marcan como inactivos
 *       para preservar el historial médico y cumplir regulaciones</li>
 *   <li><b>Auditoría completa:</b> Todas las operaciones CUD (Create, Update, Delete) son registradas
 *       automáticamente en el log de auditoría</li>
 *   <li><b>Paginación:</b> Soporte para consultas paginadas en listados grandes</li>
 *   <li><b>Búsquedas flexibles:</b> Por nombre (parcial, case-insensitive), especie, propietario</li>
 *   <li><b>Estadísticas:</b> Conteo de pacientes por especie y estado</li>
 * </ul>
 * 
 * <p><strong>Validaciones de negocio:</strong></p>
 * <ul>
 *   <li>El propietario debe existir y estar registrado</li>
 *   <li>El nombre del paciente es requerido</li>
 *   <li>La especie es requerida (ej: Canino, Felino, Ave, etc.)</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see PacienteDTO
 * @see Paciente
 * @see PacienteRepository
 * @see AuditLogger
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PropietarioRepository propietarioRepository;
    private final IAuditLogger auditLogger;

    /**
     * Obtiene todos los pacientes registrados en el sistema.
     * 
     * <p>Retorna la lista completa de pacientes (activos e inactivos) con información
     * completa del propietario. Útil para listados administrativos completos.</p>
     * 
     * <p><strong>Nota:</strong> Para listados grandes, considere usar {@link #findAll(Pageable)}.</p>
     * 
     * @return Lista completa de todos los pacientes. Nunca es null, puede ser vacía.
     * @see #findAll(Pageable)
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> findAll() {
        log.debug("Obteniendo todos los pacientes");
        return pacienteRepository.findAll().stream()
            .map(p -> PacienteDTO.fromEntity(p, true))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene pacientes con soporte de paginación.
     * 
     * <p>Método recomendado para listados grandes. Permite navegación eficiente
     * a través de grandes cantidades de registros sin sobrecargar memoria.</p>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("nombre"));
     * Page&lt;PacienteDTO&gt; page = pacienteService.findAll(pageable);
     * // page.getContent() = pacientes de la página actual
     * // page.getTotalElements() = total de pacientes
     * // page.getTotalPages() = número total de páginas
     * </pre>
     * 
     * @param pageable Configuración de paginación (página, tamaño, ordenamiento). No puede ser null.
     * @return Página de pacientes con metadatos de paginación.
     */
    @Transactional(readOnly = true)
    public Page<PacienteDTO> findAll(Pageable pageable) {
        log.debug("Obteniendo pacientes con paginación");
        return pacienteRepository.findAll(pageable)
            .map(p -> PacienteDTO.fromEntity(p, true));
    }

    /**
     * Busca y retorna un paciente específico por su identificador.
     * 
     * <p>Incluye información completa del propietario en el DTO retornado.</p>
     * 
     * <p><strong>CACHE:</strong> Almacena el resultado por 5 minutos usando el ID como key.</p>
     * 
     * @param id Identificador único del paciente. No puede ser null.
     * @return DTO con la información completa del paciente.
     * @throws ResourceNotFoundException si no existe un paciente con el ID especificado.
     */
    @Cacheable(value = "pacientes", key = "#id")
    @Transactional(readOnly = true)
    public PacienteDTO findById(Long id) {
        log.debug("Buscando paciente con ID: {}", id);
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Paciente", "id", id));
        return PacienteDTO.fromEntity(paciente, true);
    }

    /**
     * Obtiene todas las mascotas asociadas a un propietario específico.
     * 
     * <p>Útil para mostrar todas las mascotas de un cliente. La información del
     * propietario NO se incluye en los DTOs retornados para evitar redundancia.</p>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * Long propietarioId = 123L;
     * List&lt;PacienteDTO&gt; mascotas = pacienteService.findByPropietario(propietarioId);
     * // Retorna todas las mascotas del propietario
     * </pre>
     * 
     * @param propietarioId ID del propietario. No puede ser null.
     * @return Lista de pacientes del propietario. Puede estar vacía si no tiene mascotas.
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> findByPropietario(Long propietarioId) {
        log.debug("Buscando pacientes del propietario con ID: {}", propietarioId);
        return pacienteRepository.findByPropietarioId(propietarioId).stream()
            .map(p -> PacienteDTO.fromEntity(p, false))
            .collect(Collectors.toList());
    }

    /**
     * Realiza búsqueda parcial de pacientes por nombre.
     * 
     * <p>La búsqueda es case-insensitive y busca coincidencias parciales, permitiendo
     * encontrar pacientes escribiendo solo parte del nombre.</p>
     * 
     * <p><strong>Ejemplos:</strong></p>
     * <ul>
     *   <li>"max" encontrará: "Max", "Maximus", "Maxine"</li>
     *   <li>"lun" encontrará: "Luna", "Lunita"</li>
     * </ul>
     * 
     * @param nombre Texto a buscar en el nombre (parcial o completo). No puede ser null.
     * @return Lista de pacientes cuyos nombres contienen el texto buscado. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> findByNombre(String nombre) {
        log.debug("Buscando pacientes con nombre: {}", nombre);
        return pacienteRepository.findByNombreContainingIgnoreCase(nombre).stream()
            .map(p -> PacienteDTO.fromEntity(p, true))
            .collect(Collectors.toList());
    }

    /**
     * Filtra pacientes por especie.
     * 
     * <p>Útil para estadísticas, reportes o filtrado en listados. Especies comunes:
     * Canino, Felino, Ave, Roedor, Reptil, etc.</p>
     * 
     * @param especie Nombre de la especie (ej: "Canino", "Felino"). No puede ser null.
     * @return Lista de pacientes de la especie especificada. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<PacienteDTO> findByEspecie(String especie) {
        log.debug("Buscando pacientes de especie: {}", especie);
        return pacienteRepository.findByEspecie(especie).stream()
            .map(p -> PacienteDTO.fromEntity(p, true))
            .collect(Collectors.toList());
    }

    /**
     * Registra un nuevo paciente (mascota) en el sistema.
     * 
     * <p>Crea un registro completo del paciente asociándolo a un propietario existente.
     * El paciente se crea con estado activo por defecto. La operación es registrada
     * automáticamente en el log de auditoría.</p>
     * 
     * <p><strong>Datos requeridos:</strong></p>
     * <ul>
     *   <li>Nombre del paciente</li>
     *   <li>Especie (Canino, Felino, etc.)</li>
     *   <li>ID del propietario (debe existir)</li>
     * </ul>
     * 
     * <p><strong>Datos opcionales:</strong></p>
     * <ul>
     *   <li>Raza, sexo, edad (meses)</li>
     *   <li>Peso actual (kg)</li>
     *   <li>Número de microchip</li>
     *   <li>Notas clínicas o administrativas</li>
     * </ul>
     * 
     * <p><strong>Validaciones de negocio:</strong></p>
     * <ul>
     *   <li>Propietario debe existir</li>
     *   <li>Microchip debe ser único (si se proporciona)</li>
     *   <li>Edad debe ser razonable (máximo 300 meses = 25 años)</li>
     *   <li>Peso debe ser positivo y razonable</li>
     * </ul>
     * 
     * <p><strong>Registro de auditoría:</strong> Se registra automáticamente la creación
     * con información del paciente y propietario para trazabilidad.</p>
     * 
     * <p><strong>CACHE:</strong> Invalida el caché de pacientes para mantener consistencia.</p>
     * 
     * @param dto Datos del nuevo paciente. No puede ser null. Debe incluir nombre,
     *            especie y propietarioId válido.
     * @return DTO con los datos del paciente creado, incluyendo ID asignado.
     * @throws ResourceNotFoundException si el propietario no existe.
     * @throws DuplicateResourceException si el microchip ya está registrado.
     * @throws InvalidDataException si los datos no cumplen reglas de negocio.
     * @see AuditLogger#logCreate(String, Long, String)
     */
    @CacheEvict(value = "pacientes", allEntries = true)
    public PacienteDTO create(PacienteDTO dto) {
        log.info("→ Creando nuevo paciente: {} (Especie: {})", dto.getNombre(), dto.getEspecie());
        
        // VALIDACIÓN 1: Propietario debe existir
        Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
            .orElseThrow(() -> {
                log.error("✗ Propietario no encontrado con ID: {}", dto.getPropietarioId());
                return new ResourceNotFoundException("Propietario", "id", dto.getPropietarioId());
            });
        
        // VALIDACIÓN 2: Microchip único (si se proporciona)
        if (dto.getMicrochip() != null && !dto.getMicrochip().trim().isEmpty()) {
            pacienteRepository.findByMicrochip(dto.getMicrochip()).ifPresent(p -> {
                log.error("✗ Microchip duplicado: {}", dto.getMicrochip());
                throw new DuplicateResourceException("Paciente", "microchip", dto.getMicrochip());
            });
        }
        
        // VALIDACIÓN 3: Edad razonable (máximo 25 años = 300 meses)
        if (dto.getEdadMeses() != null && dto.getEdadMeses() > 300) {
            log.error("✗ Edad inválida: {} meses", dto.getEdadMeses());
            throw new InvalidDataException("edadMeses", dto.getEdadMeses(), 
                "La edad no puede exceder 300 meses (25 años)");
        }
        
        // VALIDACIÓN 4: Peso razonable (máximo 500 kg para animales grandes)
        if (dto.getPesoKg() != null && (dto.getPesoKg().doubleValue() > 500 || dto.getPesoKg().doubleValue() <= 0)) {
            log.error("✗ Peso inválido: {} kg", dto.getPesoKg());
            throw new InvalidDataException("pesoKg", dto.getPesoKg(), 
                "El peso debe estar entre 0 y 500 kg");
        }

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
        log.info("✓ Paciente creado exitosamente con ID: {} | Nombre: {} | Propietario: {}", 
                paciente.getId(), paciente.getNombre(), propietario.getNombre());
        
        // Auditar creación
        auditLogger.logCreate("Paciente", paciente.getId(), 
            String.format("Nombre: %s, Especie: %s, Propietario: %s", 
                paciente.getNombre(), paciente.getEspecie(), propietario.getNombre()));
        
        return PacienteDTO.fromEntity(paciente, true);
    }

    /**
     * Actualiza la información de un paciente existente.
     * 
     * <p>Permite modificar todos los datos del paciente, incluyendo la reasignación
     * a otro propietario si es necesario (útil cuando cambia de dueño). Captura el
     * estado anterior y posterior para el log de auditoría.</p>
     * 
     * <p><strong>Campos actualizables:</strong></p>
     * <ul>
     *   <li>Todos los datos de identificación y características</li>
     *   <li>Peso actual (importante para seguimiento médico)</li>
     *   <li>Propietario (si cambia de dueño)</li>
     *   <li>Notas y observaciones</li>
     * </ul>
     * 
     * <p><strong>Validaciones de negocio:</strong></p>
     * <ul>
     *   <li>Paciente debe existir</li>
     *   <li>Propietario (si se cambia) debe existir</li>
     *   <li>Microchip debe ser único (si se modifica)</li>
     *   <li>Edad y peso deben ser razonables</li>
     * </ul>
     * 
     * <p><strong>Auditoría:</strong> Registra los valores anteriores y nuevos para
     * mantener trazabilidad completa de los cambios.</p>
     * 
     * <p><strong>CACHE:</strong> Invalida el caché de pacientes para reflejar cambios.</p>
     * 
     * @param id ID del paciente a actualizar. No puede ser null.
     * @param dto Nuevos datos del paciente. No puede ser null.
     * @return DTO con los datos actualizados del paciente.
     * @throws ResourceNotFoundException si el paciente o el nuevo propietario no existen.
     * @throws DuplicateResourceException si el microchip ya está registrado (en otro paciente).
     * @throws InvalidDataException si los datos no cumplen reglas de negocio.
     * @see AuditLogger#logUpdate(String, Long, String, String)
     */
    @CacheEvict(value = "pacientes", allEntries = true)
    public PacienteDTO update(Long id, PacienteDTO dto) {
        log.info("→ Actualizando paciente con ID: {}", id);
        
        // VALIDACIÓN 1: Paciente debe existir
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Paciente no encontrado con ID: {}", id);
                return new ResourceNotFoundException("Paciente", "id", id);
            });

        // Capturar datos antiguos para auditoría
        String oldData = String.format("Nombre: %s, Especie: %s, Peso: %.2fkg", 
            paciente.getNombre(), paciente.getEspecie(), paciente.getPesoKg());

        // VALIDACIÓN 2: Propietario (si cambió) debe existir
        if (!paciente.getPropietario().getId().equals(dto.getPropietarioId())) {
            Propietario propietario = propietarioRepository.findById(dto.getPropietarioId())
                .orElseThrow(() -> {
                    log.error("✗ Propietario no encontrado con ID: {}", dto.getPropietarioId());
                    return new ResourceNotFoundException("Propietario", "id", dto.getPropietarioId());
                });
            log.info("  ↻ Cambio de propietario: {} → {}", 
                paciente.getPropietario().getNombre(), propietario.getNombre());
            paciente.setPropietario(propietario);
        }
        
        // VALIDACIÓN 3: Microchip único (si se modificó y se proporciona)
        if (dto.getMicrochip() != null && !dto.getMicrochip().trim().isEmpty()) {
            if (!dto.getMicrochip().equals(paciente.getMicrochip())) {
                pacienteRepository.findByMicrochip(dto.getMicrochip()).ifPresent(p -> {
                    log.error("✗ Microchip duplicado: {}", dto.getMicrochip());
                    throw new DuplicateResourceException("Paciente", "microchip", dto.getMicrochip());
                });
            }
        }
        
        // VALIDACIÓN 4: Edad razonable
        if (dto.getEdadMeses() != null && dto.getEdadMeses() > 300) {
            log.error("✗ Edad inválida: {} meses", dto.getEdadMeses());
            throw new InvalidDataException("edadMeses", dto.getEdadMeses(), 
                "La edad no puede exceder 300 meses (25 años)");
        }
        
        // VALIDACIÓN 5: Peso razonable
        if (dto.getPesoKg() != null && (dto.getPesoKg().doubleValue() > 500 || dto.getPesoKg().doubleValue() <= 0)) {
            log.error("✗ Peso inválido: {} kg", dto.getPesoKg());
            throw new InvalidDataException("pesoKg", dto.getPesoKg(), 
                "El peso debe estar entre 0 y 500 kg");
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
        
        String newData = String.format("Nombre: %s, Especie: %s, Peso: %.2fkg", 
            paciente.getNombre(), paciente.getEspecie(), paciente.getPesoKg());
        
        log.info("✓ Paciente actualizado exitosamente con ID: {}", id);
        
        // Auditar actualización
        auditLogger.logUpdate("Paciente", id, oldData, newData);
        
        return PacienteDTO.fromEntity(paciente, true);
    }

    /**
     * Desactiva un paciente del sistema (Soft Delete).
     * 
     * <p><strong>IMPORTANTE:</strong> Este método NO elimina físicamente el paciente
     * de la base de datos. En su lugar, lo marca como inactivo (soft delete). Esta
     * estrategia es crucial para:</p>
     * 
     * <ul>
     *   <li><b>Preservar historial médico:</b> Las consultas y citas pasadas mantienen
     *       la referencia al paciente</li>
     *   <li><b>Cumplimiento normativo:</b> Requisitos legales de retención de registros
     *       médicos</li>
     *   <li><b>Recuperación:</b> Posibilidad de reactivar el paciente si fue desactivado
     *       por error</li>
     *   <li><b>Auditoría:</b> Trazabilidad completa de qué pacientes fueron desactivados
     *       y cuándo</li>
     * </ul>
     * 
     * <p>Un paciente inactivo no aparecerá en búsquedas regulares pero su historial
     * permanece accesible.</p>
     * 
     * <p><strong>CACHE:</strong> Invalida el caché de pacientes.</p>
     * 
     * @param id ID del paciente a desactivar. No puede ser null.
     * @throws ResourceNotFoundException si el paciente no existe.
     * @see AuditLogger#logDelete(String, Long)
     */
    @CacheEvict(value = "pacientes", allEntries = true)
    public void delete(Long id) {
        log.warn("→ Eliminando paciente con ID: {}", id);
        
        Paciente paciente = pacienteRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Paciente no encontrado con ID: {}", id);
                return new ResourceNotFoundException("Paciente", "id", id);
            });
        
        String pacienteInfo = String.format("%s (Especie: %s, Propietario: %s)", 
            paciente.getNombre(), paciente.getEspecie(), paciente.getPropietario().getNombre());
        
        paciente.setActivo(false);
        pacienteRepository.save(paciente);
        
        log.warn("⚠ Paciente desactivado: {}", pacienteInfo);
        
        // Auditar eliminación
        auditLogger.logDelete("Paciente", id);
        
    }

    /**
     * Cuenta el número total de pacientes activos en el sistema.
     * 
     * <p>Útil para estadísticas del dashboard, reportes o verificaciones de capacidad.
     * Solo cuenta pacientes con estado activo=true.</p>
     * 
     * @return Número de pacientes activos.
     */
    @Transactional(readOnly = true)
    public long countActivos() {
        return pacienteRepository.countActivos();
    }

    /**
     * Cuenta pacientes de una especie específica.
     * 
     * <p>Útil para estadísticas y análisis demográfico de la clínica. Permite
     * identificar qué tipos de animales son más atendidos.</p>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * long caninos = pacienteService.countByEspecie("Canino");
     * long felinos = pacienteService.countByEspecie("Felino");
     * // Para generar gráficos o reportes
     * </pre>
     * 
     * @param especie Nombre de la especie a contar. No puede ser null.
     * @return Número de pacientes de la especie especificada.
     */
    @Transactional(readOnly = true)
    public long countByEspecie(String especie) {
        return pacienteRepository.countByEspecie(especie);
    }
    
    /**
     * Busca pacientes con filtros y paginación.
     * 
     * <p>Este método permite búsquedas combinadas con paginación del lado del servidor,
     * optimizando el rendimiento para datasets grandes. Soporta búsqueda por nombre
     * (case-insensitive) y filtrado por especie.</p>
     * 
     * <p><strong>Casos de uso:</strong></p>
     * <ul>
     *   <li>Listar todos los pacientes paginados (sin filtros)</li>
     *   <li>Buscar por nombre con paginación</li>
     *   <li>Filtrar por especie con paginación</li>
     *   <li>Combinar búsqueda y filtro de especie</li>
     * </ul>
     * 
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>
     * // Página 0, 20 items, ordenados por nombre
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("nombre"));
     * 
     * // Buscar "max" en cualquier especie
     * Page&lt;PacienteDTO&gt; result = pacienteService.searchWithFilters("max", null, pageable);
     * 
     * // Buscar "max" solo en caninos
     * Page&lt;PacienteDTO&gt; result = pacienteService.searchWithFilters("max", "Canino", pageable);
     * 
     * // Todos los felinos
     * Page&lt;PacienteDTO&gt; result = pacienteService.searchWithFilters(null, "Felino", pageable);
     * </pre>
     * 
     * @param nombre Texto a buscar en el nombre (opcional, null = sin filtro de nombre)
     * @param especie Especie a filtrar (opcional, null = todas las especies)
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de pacientes que cumplen los filtros
     */
    @Transactional(readOnly = true)
    public Page<PacienteDTO> searchWithFilters(String nombre, String especie, Pageable pageable) {
        log.debug("Buscando pacientes con filtros - nombre: {}, especie: {}, page: {}", 
            nombre, especie, pageable.getPageNumber());
        
        Page<Paciente> pacientes;
        
        if (nombre != null && !nombre.trim().isEmpty()) {
            // Búsqueda por nombre (con o sin filtro de especie)
            if (especie != null && !especie.trim().isEmpty()) {
                // Búsqueda con ambos filtros
                pacientes = pacienteRepository.findByNombreContainingIgnoreCaseAndEspecie(
                    nombre, especie, pageable);
            } else {
                // Solo búsqueda por nombre
                pacientes = pacienteRepository.findByNombreContainingIgnoreCase(nombre, pageable);
            }
        } else if (especie != null && !especie.trim().isEmpty()) {
            // Solo filtro de especie
            pacientes = pacienteRepository.findByEspecie(especie, pageable);
        } else {
            // Sin filtros, solo paginación
            pacientes = pacienteRepository.findAll(pageable);
        }
        
        return pacientes.map(p -> PacienteDTO.fromEntity(p, true));
    }
}

