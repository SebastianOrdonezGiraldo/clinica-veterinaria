package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.CitaDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.entity.Cita.EstadoCita;
import com.clinica.veterinaria.exception.domain.InvalidDataException;
import com.clinica.veterinaria.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de citas médicas veterinarias.
 * 
 * <p>Este controlador expone endpoints HTTP para la gestión completa del ciclo de vida
 * de las citas médicas, incluyendo creación, consulta, actualización, cambio de estado
 * y eliminación. Implementa control de acceso basado en roles.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li><b>GET /api/citas:</b> Lista todas las citas</li>
 *   <li><b>GET /api/citas/{id}:</b> Obtiene una cita específica</li>
 *   <li><b>GET /api/citas/paciente/{id}:</b> Citas de un paciente</li>
 *   <li><b>GET /api/citas/profesional/{id}:</b> Citas de un veterinario</li>
 *   <li><b>GET /api/citas/estado/{estado}:</b> Filtra por estado</li>
 *   <li><b>GET /api/citas/rango:</b> Filtra por rango de fechas</li>
 *   <li><b>POST /api/citas:</b> Crea una nueva cita</li>
 *   <li><b>PUT /api/citas/{id}:</b> Actualiza una cita</li>
 *   <li><b>PATCH /api/citas/{id}/estado:</b> Cambia el estado de una cita</li>
 *   <li><b>DELETE /api/citas/{id}:</b> Elimina una cita</li>
 * </ul>
 * 
 * <p><strong>Control de acceso:</strong></p>
 * <ul>
 *   <li><b>Lectura:</b> Todos los usuarios autenticados</li>
 *   <li><b>Creación/Actualización:</b> ADMIN, RECEPCION, VET</li>
 *   <li><b>Eliminación:</b> Solo ADMIN y RECEPCION</li>
 * </ul>
 * 
 * <p><strong>Estados de cita:</b> PENDIENTE, CONFIRMADA, EN_PROCESO, COMPLETADA, CANCELADA</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see CitaService
 * @see CitaDTO
 * @see Cita
 */
@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CitaController {

    private static final String LOG_SEPARATOR = "═══════════════════════════════════════════════════════════";
    
    private final CitaService citaService;

    /**
     * Obtener todas las citas
     * 
     * @deprecated Usar {@link #searchWithFilters} en su lugar para búsquedas paginadas con filtros
     * @since 1.0.0
     */
    @Deprecated(since = "1.0.0")
    @GetMapping
    public ResponseEntity<List<CitaDTO>> getAll() {
        log.info("GET /api/citas (DEPRECATED)");
        return ResponseEntity.ok(citaService.findAll());
    }

    /**
     * Obtener una cita por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaDTO> getById(@PathVariable @NonNull Long id) {
        log.info("GET /api/citas/{}", id);
        return ResponseEntity.ok(citaService.findById(id));
    }
    
    /**
     * Endpoint de búsqueda avanzada con filtros combinados y paginación del lado del servidor.
     * 
     * <p>Este endpoint implementa <strong>búsqueda multicritero</strong> para citas médicas,
     * soportando filtros por estado, profesional, paciente y rangos de fechas. La paginación
     * se realiza en la base de datos para manejar eficientemente grandes volúmenes.</p>
     * 
     * <p><strong>Parámetros de búsqueda (todos opcionales):</strong></p>
     * <ul>
     *   <li><b>estado:</b> Estado de cita (PENDIENTE, CONFIRMADA, EN_PROCESO, COMPLETADA, CANCELADA)</li>
     *   <li><b>profesionalId:</b> ID del veterinario para ver su agenda</li>
     *   <li><b>pacienteId:</b> ID del paciente para ver su historial</li>
     *   <li><b>fechaInicio:</b> Fecha inicial del rango (formato ISO 8601)</li>
     *   <li><b>fechaFin:</b> Fecha final del rango (formato ISO 8601)</li>
     * </ul>
     * 
     * <p><strong>Parámetros de paginación (Spring Data):</strong></p>
     * <ul>
     *   <li><b>page:</b> Número de página (0-indexed, default: 0)</li>
     *   <li><b>size:</b> Elementos por página (default: 20)</li>
     *   <li><b>sort:</b> Ordenamiento, ej: "fecha,asc" o "estado,desc"</li>
     * </ul>
     * 
     * <p><strong>Ejemplos de uso:</strong></p>
     * <pre>
     * // Caso 1: Todas las citas pendientes
     * GET /api/citas/search?estado=PENDIENTE&page=0&size=20&sort=fecha,asc
     * 
     * // Caso 2: Agenda del Dr. Smith (ID 5) esta semana
     * GET /api/citas/search?profesionalId=5&fechaInicio=2024-01-08T00:00:00&fechaFin=2024-01-14T23:59:59
     * 
     * // Caso 3: Historial de mascota ID 10
     * GET /api/citas/search?pacienteId=10&page=0&size=15&sort=fecha,desc
     * 
     * // Caso 4: Citas completadas del último mes
     * GET /api/citas/search?estado=COMPLETADA&fechaInicio=2024-01-01T00:00:00&fechaFin=2024-01-31T23:59:59
     * 
     * // Caso 5: Todas las citas con paginación
     * GET /api/citas/search?page=0&size=20&sort=fecha,desc
     * </pre>
     * 
     * <p><strong>Formato de respuesta:</strong> Objeto Page de Spring con:</p>
     * <ul>
     *   <li><b>content:</b> Array de CitaDTO</li>
     *   <li><b>totalElements:</b> Total de citas que cumplen los filtros</li>
     *   <li><b>totalPages:</b> Total de páginas</li>
     *   <li><b>size:</b> Tamaño de página solicitado</li>
     *   <li><b>number:</b> Número de página actual (0-indexed)</li>
     * </ul>
     * 
     * @param estado Filtro opcional por estado de cita
     * @param profesionalId Filtro opcional por ID de profesional (veterinario)
     * @param pacienteId Filtro opcional por ID de paciente (mascota)
     * @param fechaInicio Filtro opcional por fecha inicial (formato ISO 8601)
     * @param fechaFin Filtro opcional por fecha final (formato ISO 8601)
     * @param pageable Parámetros automáticos de paginación y orden de Spring
     * @return Page con citas que cumplen los criterios de búsqueda
     */
    @GetMapping("/search")
    public ResponseEntity<Page<CitaDTO>> searchWithFilters(
            @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) Long profesionalId,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {
        
        log.info("GET /api/citas/search - estado: {}, profesional: {}, paciente: {}, fechas: {} - {}, page: {}, size: {}", 
            estado, profesionalId, pacienteId, fechaInicio, fechaFin, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<CitaDTO> result = citaService.searchWithFilters(
            estado, profesionalId, pacienteId, fechaInicio, fechaFin, pageable);
        
        log.info("✓ Encontradas {} citas | Página {}/{} | Total: {}", 
            result.getNumberOfElements(), 
            result.getNumber() + 1, 
            result.getTotalPages(), 
            result.getTotalElements());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Obtener citas por paciente
     * 
     * @deprecated Usar {@link #searchWithFilters} con pacienteId
     * @since 1.0.0
     */
    @Deprecated(since = "1.0.0")
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<CitaDTO>> getByPaciente(@PathVariable @NonNull Long pacienteId) {
        log.info("GET /api/citas/paciente/{} (DEPRECATED)", pacienteId);
        return ResponseEntity.ok(citaService.findByPaciente(pacienteId));
    }

    /**
     * Obtener citas por profesional
     * 
     * @deprecated Usar {@link #searchWithFilters} con profesionalId
     * @since 1.0.0
     */
    @Deprecated(since = "1.0.0")
    @GetMapping("/profesional/{profesionalId}")
    public ResponseEntity<List<CitaDTO>> getByProfesional(@PathVariable @NonNull Long profesionalId) {
        log.info("GET /api/citas/profesional/{} (DEPRECATED)", profesionalId);
        return ResponseEntity.ok(citaService.findByProfesional(profesionalId));
    }

    /**
     * Obtener citas por estado
     * 
     * @deprecated Usar {@link #searchWithFilters} con estado
     * @since 1.0.0
     */
    @Deprecated(since = "1.0.0")
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CitaDTO>> getByEstado(@PathVariable @NonNull Cita.EstadoCita estado) {
        log.info("GET /api/citas/estado/{} (DEPRECATED)", estado);
        return ResponseEntity.ok(citaService.findByEstado(estado));
    }

    /**
     * Obtener citas por rango de fechas
     * 
     * @deprecated Usar {@link #searchWithFilters} con fechaInicio y fechaFin
     * @since 1.0.0
     */
    @Deprecated(since = "1.0.0")
    @GetMapping("/rango")
    public ResponseEntity<List<CitaDTO>> getByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @NonNull LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @NonNull LocalDateTime fin) {
        log.info("GET /api/citas/rango?inicio={}&fin={} (DEPRECATED)", inicio, fin);
        return ResponseEntity.ok(citaService.findByFechaRange(inicio, fin));
    }

    /**
     * Crear una nueva cita
     * ADMIN, RECEPCION, VET
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'VET')")
    public ResponseEntity<CitaDTO> create(@Valid @RequestBody CitaDTO dto) {
        log.info("POST /api/citas - paciente: {}", dto.getPacienteId());
        CitaDTO created = citaService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualizar una cita
     * ADMIN, RECEPCION, VET
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'VET')")
    public ResponseEntity<CitaDTO> update(@PathVariable @NonNull Long id, @Valid @RequestBody @NonNull CitaDTO dto) {
        log.info("PUT /api/citas/{}", id);
        return ResponseEntity.ok(citaService.update(id, dto));
    }

    /**
     * Cambiar estado de una cita
     * ADMIN, RECEPCION, VET
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'VET')")
    public ResponseEntity<CitaDTO> cambiarEstado(
            @PathVariable @NonNull Long id, 
            @RequestParam(required = false) String estado,
            @RequestBody(required = false) Map<String, String> body) {
        log.info(LOG_SEPARATOR);
        log.info("PATCH /api/citas/{}/estado - INICIANDO CAMBIO DE ESTADO", id);
        log.info(LOG_SEPARATOR);
        
        // Obtener el estado del body o del query param
        String estadoStr = null;
        if (body != null && body.containsKey("estado")) {
            estadoStr = body.get("estado");
            log.info("📥 Estado recibido desde body: {}", estadoStr);
        } else if (estado != null) {
            estadoStr = estado;
            log.info("📥 Estado recibido desde query param: {}", estadoStr);
        }
        
        if (estadoStr == null || estadoStr.isEmpty()) {
            log.error("❌ ERROR: El estado es requerido");
            throw new InvalidDataException("estado", estadoStr, "El estado es requerido");
        }
        
        // Convertir String a enum
        Cita.EstadoCita estadoEnum;
        try {
            estadoEnum = Cita.EstadoCita.valueOf(estadoStr.toUpperCase());
            log.info("✅ Estado convertido correctamente: {}", estadoEnum);
        } catch (IllegalArgumentException e) {
            log.error("❌ ERROR: Estado inválido: {}", estadoStr);
            throw new InvalidDataException("estado", estadoStr, 
                "Estado inválido. Valores válidos: PENDIENTE, CONFIRMADA, EN_PROCESO, COMPLETADA, CANCELADA");
        }
        
        log.info("🔄 Llamando a citaService.cambiarEstado({}, {})", id, estadoEnum);
        CitaDTO resultado = citaService.cambiarEstado(id, estadoEnum);
        log.info("✅ Cambio de estado completado. Retornando resultado.");
        log.info(LOG_SEPARATOR);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Eliminar una cita
     * ADMIN, RECEPCION
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<Void> delete(@PathVariable @NonNull Long id) {
        log.info("DELETE /api/citas/{}", id);
        citaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

