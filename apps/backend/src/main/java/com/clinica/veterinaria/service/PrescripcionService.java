package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.PrescripcionDTO;
import com.clinica.veterinaria.entity.Consulta;
import com.clinica.veterinaria.entity.ItemPrescripcion;
import com.clinica.veterinaria.entity.Prescripcion;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import com.clinica.veterinaria.repository.ConsultaRepository;
import com.clinica.veterinaria.repository.PrescripcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar prescripciones médicas (recetas veterinarias).
 * 
 * <p>Este servicio centraliza toda la lógica de negocio relacionada con las prescripciones
 * médicas, que representan las recetas emitidas durante las consultas. Cada prescripción
 * incluye una lista de medicamentos prescritos con sus dosis, frecuencias y duraciones.</p>
 * 
 * <p><strong>Información gestionada:</strong></p>
 * <ul>
 *   <li><b>Prescripción:</b> Fecha de emisión, indicaciones generales, consulta asociada</li>
 *   <li><b>Items (Medicamentos):</b> Nombre, presentación, dosis, frecuencia, duración, vía de administración</li>
 * </ul>
 * 
 * <p><strong>Características principales:</strong></p>
 * <ul>
 *   <li>Validación de consulta existente</li>
 *   <li>Registro automático de fecha de emisión si no se especifica</li>
 *   <li>Gestión de items (medicamentos) con cascade</li>
 *   <li>Trazabilidad completa mediante logs de auditoría</li>
 *   <li>Gestión transaccional para garantizar integridad de datos</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see PrescripcionDTO
 * @see Prescripcion
 * @see PrescripcionRepository
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrescripcionService {

    private final PrescripcionRepository prescripcionRepository;
    private final ConsultaRepository consultaRepository;

    /**
     * Obtiene todas las prescripciones registradas en el sistema.
     * 
     * @return Lista completa de todas las prescripciones con sus items. Nunca es null, puede ser vacía.
     */
    @Transactional(readOnly = true)
    public List<PrescripcionDTO> findAll() {
        log.debug("Obteniendo todas las prescripciones");
        return prescripcionRepository.findAll().stream()
            .map(p -> PrescripcionDTO.fromEntity(p, true))
            .collect(Collectors.toList());
    }

    /**
     * Busca y retorna una prescripción específica por su identificador.
     * 
     * @param id Identificador único de la prescripción. No puede ser null.
     * @return DTO con la información completa de la prescripción incluyendo items.
     * @throws RuntimeException si no existe una prescripción con el ID especificado.
     */
    @Transactional(readOnly = true)
    public PrescripcionDTO findById(Long id) {
        log.debug("Buscando prescripción con ID: {}", id);
        Prescripcion prescripcion = prescripcionRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Prescripción no encontrada con ID: {}", id);
                return new ResourceNotFoundException("Prescripción", "id", id);
            });
        return PrescripcionDTO.fromEntity(prescripcion, true);
    }

    /**
     * Obtiene todas las prescripciones de una consulta específica.
     * 
     * @param consultaId ID de la consulta. No puede ser null.
     * @return Lista de prescripciones de esa consulta. Puede estar vacía.
     */
    @Transactional(readOnly = true)
    public List<PrescripcionDTO> findByConsulta(Long consultaId) {
        log.debug("Buscando prescripciones de consulta ID: {}", consultaId);
        return prescripcionRepository.findByConsultaId(consultaId).stream()
            .map(p -> PrescripcionDTO.fromEntity(p, true))
            .collect(Collectors.toList());
    }

    /**
     * Obtiene todas las prescripciones de un paciente.
     * 
     * @param pacienteId ID del paciente. No puede ser null.
     * @return Lista de prescripciones del paciente ordenadas por fecha descendente.
     */
    @Transactional(readOnly = true)
    public List<PrescripcionDTO> findByPaciente(Long pacienteId) {
        log.debug("Buscando prescripciones de paciente ID: {}", pacienteId);
        return prescripcionRepository.findByPacienteId(pacienteId).stream()
            .map(p -> PrescripcionDTO.fromEntity(p, true))
            .collect(Collectors.toList());
    }

    /**
     * Crea una nueva prescripción médica con sus items (medicamentos).
     * 
     * <p>Valida que la consulta exista y crea la prescripción junto con todos sus
     * items de medicamentos en una transacción única.</p>
     * 
     * @param dto Datos de la prescripción incluyendo items. No puede ser null.
     * @return DTO con los datos de la prescripción creada, incluyendo IDs asignados.
     * @throws RuntimeException si la consulta no existe.
     */
    public PrescripcionDTO create(PrescripcionDTO dto) {
        log.info("→ Creando nueva prescripción para consulta ID: {}", dto.getConsultaId());
        
        // VALIDACIÓN: Consulta existe
        Consulta consulta = consultaRepository.findById(dto.getConsultaId())
            .orElseThrow(() -> {
                log.error("✗ Consulta no encontrada con ID: {}", dto.getConsultaId());
                return new ResourceNotFoundException("Consulta", "id", dto.getConsultaId());
            });

        // Crear prescripción
        Prescripcion prescripcion = Prescripcion.builder()
            .fechaEmision(dto.getFechaEmision() != null ? dto.getFechaEmision() : LocalDateTime.now())
            .indicacionesGenerales(dto.getIndicacionesGenerales())
            .consulta(consulta)
            .build();

        // Guardar primero para obtener el ID
        prescripcion = prescripcionRepository.save(prescripcion);

        // Crear items de prescripción
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            final Prescripcion finalPrescripcion = prescripcion;
            List<ItemPrescripcion> items = dto.getItems().stream()
                .map(itemDto -> ItemPrescripcion.builder()
                    .medicamento(itemDto.getMedicamento())
                    .presentacion(itemDto.getPresentacion())
                    .dosis(itemDto.getDosis())
                    .frecuencia(itemDto.getFrecuencia())
                    .duracionDias(itemDto.getDuracionDias())
                    .viaAdministracion(itemDto.getViaAdministracion())
                    .indicaciones(itemDto.getIndicaciones())
                    .prescripcion(finalPrescripcion)
                    .build())
                .collect(Collectors.toList());
            prescripcion.setItems(items);
            prescripcion = prescripcionRepository.save(prescripcion);
        }
        log.info("✓ Prescripción creada exitosamente con ID: {} | {} items", 
                prescripcion.getId(), prescripcion.getItems().size());
        
        return PrescripcionDTO.fromEntity(prescripcion, true);
    }

    /**
     * Actualiza una prescripción existente y sus items.
     * 
     * <p>Reemplaza completamente los items existentes con los nuevos proporcionados.
     * Si se proporciona una lista vacía, se eliminan todos los items.</p>
     * 
     * @param id ID de la prescripción a actualizar. No puede ser null.
     * @param dto Nuevos datos para la prescripción. No puede ser null.
     * @return DTO con los datos actualizados de la prescripción.
     * @throws RuntimeException si la prescripción no existe.
     */
    public PrescripcionDTO update(Long id, PrescripcionDTO dto) {
        log.info("→ Actualizando prescripción con ID: {}", id);
        
        Prescripcion prescripcion = prescripcionRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Prescripción no encontrada con ID: {}", id);
                return new ResourceNotFoundException("Prescripción", "id", id);
            });

        // Actualizar campos básicos
        prescripcion.setFechaEmision(dto.getFechaEmision());
        prescripcion.setIndicacionesGenerales(dto.getIndicacionesGenerales());

        // Reemplazar items existentes
        prescripcion.getItems().clear();
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            final Prescripcion finalPrescripcion = prescripcion;
            List<ItemPrescripcion> items = dto.getItems().stream()
                .map(itemDto -> ItemPrescripcion.builder()
                    .medicamento(itemDto.getMedicamento())
                    .presentacion(itemDto.getPresentacion())
                    .dosis(itemDto.getDosis())
                    .frecuencia(itemDto.getFrecuencia())
                    .duracionDias(itemDto.getDuracionDias())
                    .viaAdministracion(itemDto.getViaAdministracion())
                    .indicaciones(itemDto.getIndicaciones())
                    .prescripcion(finalPrescripcion)
                    .build())
                .collect(Collectors.toList());
            prescripcion.getItems().addAll(items);
        }

        prescripcion = prescripcionRepository.save(prescripcion);
        log.info("✓ Prescripción actualizada exitosamente con ID: {}", prescripcion.getId());
        
        return PrescripcionDTO.fromEntity(prescripcion, true);
    }

    /**
     * Elimina una prescripción del sistema.
     * 
     * <p>Elimina la prescripción y todos sus items asociados (cascade delete).</p>
     * 
     * @param id ID de la prescripción a eliminar. No puede ser null.
     * @throws ResourceNotFoundException si la prescripción no existe.
     */
    public void delete(Long id) {
        log.warn("→ Eliminando prescripción con ID: {}", id);
        
        Prescripcion prescripcion = prescripcionRepository.findById(id)
            .orElseThrow(() -> {
                log.error("✗ Prescripción no encontrada con ID: {}", id);
                return new ResourceNotFoundException("Prescripción", "id", id);
            });
        
        prescripcionRepository.delete(prescripcion);
        log.warn("⚠ Prescripción eliminada exitosamente con ID: {}", id);
    }
    
    /**
     * Busca prescripciones con filtros combinados y paginación del lado del servidor.
     * 
     * <p><strong>PATRÓN STRATEGY:</strong> Selecciona dinámicamente el query apropiado
     * según los filtros proporcionados.</p>
     * 
     * @param pacienteId Filtro opcional por ID de paciente
     * @param consultaId Filtro opcional por ID de consulta
     * @param fechaInicio Filtro opcional por fecha inicial
     * @param fechaFin Filtro opcional por fecha final
     * @param pageable Configuración de paginación y ordenamiento
     * @return Página de prescripciones que cumplen los criterios
     */
    @Transactional(readOnly = true)
    public Page<PrescripcionDTO> searchWithFilters(
            Long pacienteId,
            Long consultaId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable) {
        
        log.debug("Buscando prescripciones - paciente: {}, consulta: {}, fechas: {} - {}", 
            pacienteId, consultaId, fechaInicio, fechaFin);
        
        Page<Prescripcion> prescripciones;
        
        // STRATEGY PATTERN: Selección dinámica del query apropiado
        if (pacienteId != null && fechaInicio != null && fechaFin != null) {
            prescripciones = prescripcionRepository.findByPacienteIdAndFechaEmisionBetween(
                pacienteId, fechaInicio, fechaFin, pageable);
        } else if (consultaId != null) {
            prescripciones = prescripcionRepository.findByConsultaId(consultaId, pageable);
        } else if (pacienteId != null) {
            prescripciones = prescripcionRepository.findByPacienteId(pacienteId, pageable);
        } else if (fechaInicio != null && fechaFin != null) {
            prescripciones = prescripcionRepository.findByFechaEmisionBetween(fechaInicio, fechaFin, pageable);
        } else {
            prescripciones = prescripcionRepository.findAll(pageable);
        }
        
        log.debug("Prescripciones encontradas: {} en página {}/{}", 
            prescripciones.getNumberOfElements(), 
            prescripciones.getNumber() + 1, 
            prescripciones.getTotalPages());
        
        return prescripciones.map(p -> PrescripcionDTO.fromEntity(p, true));
    }
}

