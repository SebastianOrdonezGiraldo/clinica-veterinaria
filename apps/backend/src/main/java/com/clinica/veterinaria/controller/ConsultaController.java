package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.ConsultaDTO;
import com.clinica.veterinaria.service.ConsultaService;
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
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<ConsultaDTO>> getAll() {
        log.info("GET /api/consultas");
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
     * Obtener consultas por paciente (historia clínica)
     */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ConsultaDTO>> getByPaciente(@PathVariable Long pacienteId) {
        log.info("GET /api/consultas/paciente/{}", pacienteId);
        return ResponseEntity.ok(consultaService.findByPaciente(pacienteId));
    }

    /**
     * Obtener consultas por profesional
     * ADMIN, VET
     */
    @GetMapping("/profesional/{profesionalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<ConsultaDTO>> getByProfesional(@PathVariable Long profesionalId) {
        log.info("GET /api/consultas/profesional/{}", profesionalId);
        return ResponseEntity.ok(consultaService.findByProfesional(profesionalId));
    }

    /**
     * Obtener consultas por rango de fechas
     * ADMIN, VET
     */
    @GetMapping("/rango")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<ConsultaDTO>> getByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("GET /api/consultas/rango?inicio={}&fin={}", inicio, fin);
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

