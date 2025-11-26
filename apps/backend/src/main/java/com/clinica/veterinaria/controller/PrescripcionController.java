package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.ConsultaDTO;
import com.clinica.veterinaria.dto.PacienteDTO;
import com.clinica.veterinaria.dto.PrescripcionDTO;
import com.clinica.veterinaria.dto.PropietarioDTO;
import com.clinica.veterinaria.dto.UsuarioDTO;
import com.clinica.veterinaria.service.ConsultaService;
import com.clinica.veterinaria.service.PacienteService;
import com.clinica.veterinaria.service.PdfReportService;
import com.clinica.veterinaria.service.PrescripcionService;
import com.clinica.veterinaria.service.PropietarioService;
import com.clinica.veterinaria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para gestión de prescripciones médicas (recetas veterinarias).
 * 
 * <p>Este controlador expone endpoints HTTP para la gestión completa de las prescripciones
 * médicas, que representan las recetas emitidas durante las consultas. Incluye la creación,
 * consulta, actualización y eliminación de prescripciones con sus medicamentos.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li><b>GET /api/prescripciones:</b> Lista todas las prescripciones (ADMIN, VET)</li>
 *   <li><b>GET /api/prescripciones/{id}:</b> Obtiene una prescripción específica</li>
 *   <li><b>GET /api/prescripciones/consulta/{id}:</b> Prescripciones de una consulta</li>
 *   <li><b>GET /api/prescripciones/paciente/{id}:</b> Prescripciones de un paciente</li>
 *   <li><b>GET /api/prescripciones/rango:</b> Prescripciones por rango de fechas (ADMIN, VET)</li>
 *   <li><b>POST /api/prescripciones:</b> Crea una nueva prescripción (ADMIN, VET)</li>
 *   <li><b>PUT /api/prescripciones/{id}:</b> Actualiza una prescripción (ADMIN, VET)</li>
 *   <li><b>DELETE /api/prescripciones/{id}:</b> Elimina una prescripción (Solo ADMIN)</li>
 * </ul>
 * 
 * <p><strong>Control de acceso:</strong></p>
 * <ul>
 *   <li><b>Lectura por consulta/paciente:</b> Acceso general (para propietarios ver recetas de su mascota)</li>
 *   <li><b>Listados y búsquedas:</b> ADMIN, VET</li>
 *   <li><b>Creación/Actualización:</b> ADMIN, VET</li>
 *   <li><b>Eliminación:</b> Solo ADMIN (protección de historial médico)</li>
 * </ul>
 * 
 * <p><strong>Datos registrados en cada prescripción:</b></p>
 * <ul>
 *   <li>Fecha de emisión</li>
 *   <li>Indicaciones generales</li>
 *   <li>Lista de medicamentos (items) con dosis, frecuencia, duración y vía de administración</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see PrescripcionService
 * @see PrescripcionDTO
 */
@RestController
@RequestMapping("/api/prescripciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PrescripcionController {

    private final PrescripcionService prescripcionService;
    private final ConsultaService consultaService;
    private final PacienteService pacienteService;
    private final PropietarioService propietarioService;
    private final UsuarioService usuarioService;
    private final PdfReportService pdfReportService;

    /**
     * Obtener todas las prescripciones
     * ADMIN, VET
     * @deprecated Usar {@link #searchWithFilters}
     */
    @Deprecated
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<PrescripcionDTO>> getAll() {
        log.info("GET /api/prescripciones (DEPRECATED)");
        return ResponseEntity.ok(prescripcionService.findAll());
    }

    /**
     * Obtener una prescripción por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PrescripcionDTO> getById(@PathVariable Long id) {
        log.info("GET /api/prescripciones/{}", id);
        return ResponseEntity.ok(prescripcionService.findById(id));
    }
    
    /**
     * Endpoint de búsqueda avanzada con filtros combinados y paginación del lado del servidor.
     * 
     * @param pacienteId Filtro opcional por ID de paciente
     * @param consultaId Filtro opcional por ID de consulta
     * @param fechaInicio Filtro opcional por fecha inicial (formato ISO 8601)
     * @param fechaFin Filtro opcional por fecha final (formato ISO 8601)
     * @param pageable Parámetros automáticos de paginación y orden de Spring
     * @return Page con prescripciones que cumplen los criterios de búsqueda
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<Page<PrescripcionDTO>> searchWithFilters(
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long consultaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            Pageable pageable) {
        
        log.info("GET /api/prescripciones/search - paciente: {}, consulta: {}, fechas: {} - {}",
            pacienteId, consultaId, fechaInicio, fechaFin);
        
        Page<PrescripcionDTO> result = prescripcionService.searchWithFilters(
            pacienteId, consultaId, fechaInicio, fechaFin, pageable);
        
        log.info("✓ Encontradas {} prescripciones | Página {}/{} | Total: {}", 
            result.getNumberOfElements(), 
            result.getNumber() + 1, 
            result.getTotalPages(), 
            result.getTotalElements());
        
        return ResponseEntity.ok(result);
    }

    /**
     * Obtener prescripciones por consulta
     * @deprecated Usar {@link #searchWithFilters} con consultaId
     */
    @Deprecated
    @GetMapping("/consulta/{consultaId}")
    public ResponseEntity<List<PrescripcionDTO>> getByConsulta(@PathVariable Long consultaId) {
        log.info("GET /api/prescripciones/consulta/{} (DEPRECATED)", consultaId);
        return ResponseEntity.ok(prescripcionService.findByConsulta(consultaId));
    }

    /**
     * Obtener prescripciones por paciente
     * @deprecated Usar {@link #searchWithFilters} con pacienteId
     */
    @Deprecated
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<PrescripcionDTO>> getByPaciente(@PathVariable Long pacienteId) {
        log.info("GET /api/prescripciones/paciente/{} (DEPRECATED)", pacienteId);
        return ResponseEntity.ok(prescripcionService.findByPaciente(pacienteId));
    }

    /**
     * Obtener prescripciones por rango de fechas
     * ADMIN, VET
     * @deprecated Usar {@link #searchWithFilters} con fechaInicio y fechaFin
     */
    @Deprecated
    @GetMapping("/rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<PrescripcionDTO>> getByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("GET /api/prescripciones/rango?inicio={}&fin={} (DEPRECATED)", inicio, fin);
        return ResponseEntity.ok(List.of());
    }

    /**
     * Crear una nueva prescripción
     * ADMIN, VET
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<PrescripcionDTO> create(@Valid @RequestBody PrescripcionDTO dto) {
        log.info("POST /api/prescripciones - consulta: {}", dto.getConsultaId());
        PrescripcionDTO created = prescripcionService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualizar una prescripción
     * ADMIN, VET
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<PrescripcionDTO> update(@PathVariable Long id, @Valid @RequestBody PrescripcionDTO dto) {
        log.info("PUT /api/prescripciones/{}", id);
        return ResponseEntity.ok(prescripcionService.update(id, dto));
    }

    /**
     * Eliminar una prescripción
     * Solo ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/prescripciones/{}", id);
        prescripcionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Descargar PDF de una prescripción médica (receta veterinaria)
     * 
     * @param id ID de la prescripción
     * @return PDF como array de bytes con headers apropiados para descarga
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        log.info("GET /api/prescripciones/{}/pdf", id);
        
        try {
            // Obtener prescripción
            PrescripcionDTO prescripcion = prescripcionService.findById(id);
            
            // Obtener consulta
            ConsultaDTO consulta = consultaService.findById(prescripcion.getConsultaId());
            
            // Obtener paciente
            PacienteDTO paciente = pacienteService.findById(consulta.getPacienteId());
            
            // Obtener propietario (puede ser null)
            String propietarioNombre = null;
            if (paciente.getPropietarioId() != null) {
                try {
                    PropietarioDTO propietario = propietarioService.findById(paciente.getPropietarioId());
                    propietarioNombre = propietario.getNombre();
                } catch (Exception e) {
                    log.warn("No se pudo obtener propietario para paciente {}: {}", paciente.getId(), e.getMessage());
                }
            }
            
            // Obtener profesional (puede ser null)
            String profesionalNombre = null;
            if (consulta.getProfesionalId() != null) {
                try {
                    UsuarioDTO profesional = usuarioService.findById(consulta.getProfesionalId());
                    profesionalNombre = profesional.getNombre();
                } catch (Exception e) {
                    log.warn("No se pudo obtener profesional para consulta {}: {}", consulta.getId(), e.getMessage());
                }
            }
            
            // Generar PDF
            byte[] pdfBytes = pdfReportService.generarPrescripcionPdf(
                prescripcion,
                paciente.getNombre(),
                paciente.getEspecie(),
                paciente.getRaza(),
                propietarioNombre,
                consulta.getFecha(),
                profesionalNombre
            );
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                String.format("receta-medica-%d.pdf", prescripcion.getId()));
            headers.setContentLength(pdfBytes.length);
            
            log.info("✓ PDF de prescripción {} generado exitosamente. Tamaño: {} bytes", id, pdfBytes.length);
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (Exception e) {
            log.error("✗ Error al generar PDF de prescripción {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

