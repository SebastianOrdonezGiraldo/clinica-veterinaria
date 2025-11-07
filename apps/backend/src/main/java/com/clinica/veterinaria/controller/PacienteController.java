package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.PacienteDTO;
import com.clinica.veterinaria.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión de pacientes veterinarios (mascotas).
 * 
 * <p>Expone endpoints HTTP para la gestión completa de pacientes, incluyendo
 * operaciones CRUD, búsquedas por nombre/especie/propietario, y soporte de paginación.</p>
 * 
 * <p><strong>Endpoints principales:</strong></p>
 * <ul>
 *   <li><b>GET /api/pacientes:</b> Lista todos los pacientes (con paginación opcional)</li>
 *   <li><b>GET /api/pacientes/{id}:</b> Obtiene un paciente específico</li>
 *   <li><b>GET /api/pacientes/propietario/{id}:</b> Mascotas de un propietario</li>
 *   <li><b>GET /api/pacientes/buscar:</b> Búsqueda por nombre</li>
 *   <li><b>GET /api/pacientes/especie/{especie}:</b> Filtra por especie</li>
 *   <li><b>POST /api/pacientes:</b> Registra un nuevo paciente</li>
 *   <li><b>PUT /api/pacientes/{id}:</b> Actualiza un paciente</li>
 *   <li><b>DELETE /api/pacientes/{id}:</b> Desactiva un paciente (soft delete)</li>
 * </ul>
 * 
 * <p><strong>Control de acceso:</strong> Todos los endpoints requieren autenticación.
 * Creación/Actualización/Eliminación requieren roles ADMIN, RECEPCION o VET.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see PacienteService
 * @see PacienteDTO
 */
@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PacienteController {

    private final PacienteService pacienteService;

    /**
     * Obtener todos los pacientes
     */
    @GetMapping
    public ResponseEntity<List<PacienteDTO>> getAll() {
        log.info("GET /api/pacientes");
        return ResponseEntity.ok(pacienteService.findAll());
    }

    /**
     * Obtener pacientes con paginación
     */
    @GetMapping("/page")
    public ResponseEntity<Page<PacienteDTO>> getPage(Pageable pageable) {
        log.info("GET /api/pacientes/page");
        return ResponseEntity.ok(pacienteService.findAll(pageable));
    }

    /**
     * Obtener un paciente por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PacienteDTO> getById(@PathVariable Long id) {
        log.info("GET /api/pacientes/{}", id);
        return ResponseEntity.ok(pacienteService.findById(id));
    }

    /**
     * Obtener pacientes por propietario
     */
    @GetMapping("/propietario/{propietarioId}")
    public ResponseEntity<List<PacienteDTO>> getByPropietario(@PathVariable Long propietarioId) {
        log.info("GET /api/pacientes/propietario/{}", propietarioId);
        return ResponseEntity.ok(pacienteService.findByPropietario(propietarioId));
    }

    /**
     * Buscar pacientes por nombre
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<PacienteDTO>> searchByNombre(@RequestParam String nombre) {
        log.info("GET /api/pacientes/buscar?nombre={}", nombre);
        return ResponseEntity.ok(pacienteService.findByNombre(nombre));
    }

    /**
     * Buscar pacientes por especie
     */
    @GetMapping("/especie/{especie}")
    public ResponseEntity<List<PacienteDTO>> getByEspecie(@PathVariable String especie) {
        log.info("GET /api/pacientes/especie/{}", especie);
        return ResponseEntity.ok(pacienteService.findByEspecie(especie));
    }

    /**
     * Crear un nuevo paciente
     * ADMIN, RECEPCION, VET
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'VET')")
    public ResponseEntity<PacienteDTO> create(@Valid @RequestBody PacienteDTO dto) {
        log.info("POST /api/pacientes - nombre: {}", dto.getNombre());
        PacienteDTO created = pacienteService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Actualizar un paciente
     * ADMIN, RECEPCION, VET
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'VET')")
    public ResponseEntity<PacienteDTO> update(@PathVariable Long id, @Valid @RequestBody PacienteDTO dto) {
        log.info("PUT /api/pacientes/{}", id);
        return ResponseEntity.ok(pacienteService.update(id, dto));
    }

    /**
     * Eliminar un paciente
     * Solo ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/pacientes/{}", id);
        pacienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

