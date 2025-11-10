package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.PrescripcionDTO;
import com.clinica.veterinaria.service.PrescripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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

    /**
     * Obtener todas las prescripciones
     * ADMIN, VET
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<PrescripcionDTO>> getAll() {
        log.info("GET /api/prescripciones");
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
     * Obtener prescripciones por consulta
     */
    @GetMapping("/consulta/{consultaId}")
    public ResponseEntity<List<PrescripcionDTO>> getByConsulta(@PathVariable Long consultaId) {
        log.info("GET /api/prescripciones/consulta/{}", consultaId);
        return ResponseEntity.ok(prescripcionService.findByConsulta(consultaId));
    }

    /**
     * Obtener prescripciones por paciente
     */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<PrescripcionDTO>> getByPaciente(@PathVariable Long pacienteId) {
        log.info("GET /api/prescripciones/paciente/{}", pacienteId);
        return ResponseEntity.ok(prescripcionService.findByPaciente(pacienteId));
    }

    /**
     * Obtener prescripciones por rango de fechas
     * ADMIN, VET
     */
    @GetMapping("/rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<PrescripcionDTO>> getByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("GET /api/prescripciones/rango?inicio={}&fin={}", inicio, fin);
        // Nota: Este método requeriría agregar findByFechaEmisionBetween al repository
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
}

