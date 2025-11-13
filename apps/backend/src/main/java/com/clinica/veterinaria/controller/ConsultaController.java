package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.ConsultaDTO;
import com.clinica.veterinaria.service.ConsultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestión de consultas médicas (historia clínica).
 * 
 * <p>Este controlador expone endpoints HTTP para la gestión completa de las consultas
 * médicas veterinarias, que representan el registro detallado de cada atención médica.
 * Incluye signos vitales, examen físico, diagnóstico y tratamiento.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li><b>GET /api/consultas:</b> Lista todas las consultas (ADMIN, VET)</li>
 *   <li><b>GET /api/consultas/{id}:</b> Obtiene una consulta específica</li>
 *   <li><b>GET /api/consultas/paciente/{id}:</b> Historia clínica completa de un paciente</li>
 *   <li><b>GET /api/consultas/profesional/{id}:</b> Consultas de un veterinario (ADMIN, VET)</li>
 *   <li><b>GET /api/consultas/rango:</b> Consultas por rango de fechas (ADMIN, VET)</li>
 *   <li><b>POST /api/consultas:</b> Registra una nueva consulta (ADMIN, VET)</li>
 *   <li><b>PUT /api/consultas/{id}:</b> Actualiza una consulta (ADMIN, VET)</li>
 *   <li><b>DELETE /api/consultas/{id}:</b> Elimina una consulta (Solo ADMIN)</li>
 * </ul>
 * 
 * <p><strong>Control de acceso:</strong></p>
 * <ul>
 *   <li><b>Historia clínica por paciente:</b> Acceso general (para propietarios ver su mascota)</li>
 *   <li><b>Listados y búsquedas:</b> ADMIN, VET</li>
 *   <li><b>Creación/Actualización:</b> ADMIN, VET</li>
 *   <li><b>Eliminación:</b> Solo ADMIN (protección de historial médico)</li>
 * </ul>
 * 
 * <p><strong>Datos registrados en cada consulta:</b></p>
 * <ul>
 *   <li>Signos vitales (frecuencia cardíaca, respiratoria, temperatura, peso)</li>
 *   <li>Examen físico detallado</li>
 *   <li>Diagnóstico médico</li>
 *   <li>Tratamiento prescrito</li>
 *   <li>Observaciones y seguimiento</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see ConsultaService
 * @see ConsultaDTO
 */
@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ConsultaController {

    private final ConsultaService consultaService;

    /**
     * Obtener todas las consultas
     * ADMIN, VET
     * 
     * @deprecated Usar {@link #searchWithFilters} en su lugar para búsquedas paginadas con filtros
     */
    @Deprecated
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<ConsultaDTO>> getAll() {
        log.info("GET /api/consultas (DEPRECATED)");
        return ResponseEntity.ok(consultaService.findAll());
    }

    /**
     * Obtener una consulta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConsultaDTO> getById(@PathVariable Long id) {
        log.info("GET /api/consultas/{}", id);
        return ResponseEntity.ok(consultaService.findById(id));
    }
    
    /**
     * Endpoint de búsqueda avanzada con filtros combinados y paginación del lado del servidor.
     * 
     * <p>Este endpoint implementa <strong>búsqueda multicritero</strong> para consultas médicas,
     * soportando filtros por paciente, profesional y rangos de fechas. Ideal para historiales
     * médicos extensos y reportes estadísticos.</p>
     * 
     * <p><strong>Parámetros de búsqueda (todos opcionales):</strong></p>
     * <ul>
     *   <li><b>pacienteId:</b> ID del paciente para ver su historial médico completo</li>
     *   <li><b>profesionalId:</b> ID del veterinario para ver sus consultas</li>
     *   <li><b>fechaInicio:</b> Fecha inicial del rango (formato ISO 8601)</li>
     *   <li><b>fechaFin:</b> Fecha final del rango (formato ISO 8601)</li>
     * </ul>
     * 
     * <p><strong>Parámetros de paginación (Spring Data):</strong></p>
     * <ul>
     *   <li><b>page:</b> Número de página (0-indexed, default: 0)</li>
     *   <li><b>size:</b> Elementos por página (default: 20)</li>
     *   <li><b>sort:</b> Ordenamiento, ej: "fecha,desc" o "id,asc"</li>
     * </ul>
     * 
     * <p><strong>Ejemplos de uso:</strong></p>
     * <pre>
     * // Caso 1: Historial completo de mascota ID 10
     * GET /api/consultas/search?pacienteId=10&page=0&size=20&sort=fecha,desc
     * 
     * // Caso 2: Consultas del Dr. Smith este mes
     * GET /api/consultas/search?profesionalId=5&fechaInicio=2024-01-01T00:00:00&fechaFin=2024-01-31T23:59:59
     * 
     * // Caso 3: Historial de mascota del último año
     * GET /api/consultas/search?pacienteId=10&fechaInicio=2023-01-01T00:00:00&fechaFin=2024-01-01T00:00:00
     * 
     * // Caso 4: Todas las consultas del último mes
     * GET /api/consultas/search?fechaInicio=2024-01-01T00:00:00&fechaFin=2024-01-31T23:59:59&page=0&size=50
     * 
     * // Caso 5: Todas las consultas con paginación
     * GET /api/consultas/search?page=0&size=20&sort=fecha,desc
     * </pre>
     * 
     * <p><strong>Formato de respuesta:</strong> Objeto Page de Spring con:</p>
     * <ul>
     *   <li><b>content:</b> Array de ConsultaDTO (incluye paciente y profesional)</li>
     *   <li><b>totalElements:</b> Total de consultas que cumplen los filtros</li>
     *   <li><b>totalPages:</b> Total de páginas</li>
     *   <li><b>size:</b> Tamaño de página solicitado</li>
     *   <li><b>number:</b> Número de página actual (0-indexed)</li>
     * </ul>
     * 
     * @param pacienteId Filtro opcional por ID de paciente (historia clínica)
     * @param profesionalId Filtro opcional por ID de profesional (veterinario)
     * @param fechaInicio Filtro opcional por fecha inicial (formato ISO 8601)
     * @param fechaFin Filtro opcional por fecha final (formato ISO 8601)
     * @param pageable Parámetros automáticos de paginación y orden de Spring
     * @return Page con consultas que cumplen los criterios de búsqueda
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<Page<ConsultaDTO>> searchWithFilters(
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long profesionalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {
        
        log.info("GET /api/consultas/search - paciente: {}, profesional: {}, fechas: {} - {}, page: {}, size: {}", 
            pacienteId, profesionalId, fechaInicio, fechaFin, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<ConsultaDTO> result = consultaService.searchWithFilters(
            pacienteId, profesionalId, fechaInicio, fechaFin, pageable);
        
        log.info("✓ Encontradas {} consultas | Página {}/{} | Total: {}", 
            result.getNumberOfElements(), 
            result.getNumber() + 1, 
            result.getTotalPages(), 
            result.getTotalElements());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Obtener consultas por paciente (historia clínica)
     * 
     * @deprecated Usar {@link #searchWithFilters} con pacienteId
     */
    @Deprecated
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ConsultaDTO>> getByPaciente(@PathVariable Long pacienteId) {
        log.info("GET /api/consultas/paciente/{} (DEPRECATED)", pacienteId);
        return ResponseEntity.ok(consultaService.findByPaciente(pacienteId));
    }

    /**
     * Obtener consultas por profesional
     * ADMIN, VET
     * 
     * @deprecated Usar {@link #searchWithFilters} con profesionalId
     */
    @Deprecated
    @GetMapping("/profesional/{profesionalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<ConsultaDTO>> getByProfesional(@PathVariable Long profesionalId) {
        log.info("GET /api/consultas/profesional/{} (DEPRECATED)", profesionalId);
        return ResponseEntity.ok(consultaService.findByProfesional(profesionalId));
    }

    /**
     * Obtener consultas por rango de fechas
     * ADMIN, VET
     * 
     * @deprecated Usar {@link #searchWithFilters} con fechaInicio y fechaFin
     */
    @Deprecated
    @GetMapping("/rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<ConsultaDTO>> getByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("GET /api/consultas/rango?inicio={}&fin={} (DEPRECATED)", inicio, fin);
        return ResponseEntity.ok(consultaService.findByFechaRange(inicio, fin));
    }

    /**
     * Crear una nueva consulta
     * ADMIN, VET
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<ConsultaDTO> create(@Valid @RequestBody ConsultaDTO dto) {
        log.info("POST /api/consultas - paciente: {}", dto.getPacienteId());
        ConsultaDTO created = consultaService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualizar una consulta
     * ADMIN, VET
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<ConsultaDTO> update(@PathVariable Long id, @Valid @RequestBody ConsultaDTO dto) {
        log.info("PUT /api/consultas/{}", id);
        return ResponseEntity.ok(consultaService.update(id, dto));
    }

    /**
     * Eliminar una consulta
     * Solo ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/consultas/{}", id);
        consultaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

