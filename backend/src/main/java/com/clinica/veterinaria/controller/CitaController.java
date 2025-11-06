package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.CitaDTO;
import com.clinica.veterinaria.entity.Cita;
import com.clinica.veterinaria.service.CitaService;
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
 * Controlador REST para gestión de citas médicas
 */
@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CitaController {

    private final CitaService citaService;

    /**
     * Obtener todas las citas
     */
    @GetMapping
    public ResponseEntity<List<CitaDTO>> getAll() {
        log.info("GET /api/citas");
        return ResponseEntity.ok(citaService.findAll());
    }

    /**
     * Obtener una cita por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CitaDTO> getById(@PathVariable Long id) {
        log.info("GET /api/citas/{}", id);
        return ResponseEntity.ok(citaService.findById(id));
    }

    /**
     * Obtener citas por paciente
     */
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<CitaDTO>> getByPaciente(@PathVariable Long pacienteId) {
        log.info("GET /api/citas/paciente/{}", pacienteId);
        return ResponseEntity.ok(citaService.findByPaciente(pacienteId));
    }

    /**
     * Obtener citas por profesional
     */
    @GetMapping("/profesional/{profesionalId}")
    public ResponseEntity<List<CitaDTO>> getByProfesional(@PathVariable Long profesionalId) {
        log.info("GET /api/citas/profesional/{}", profesionalId);
        return ResponseEntity.ok(citaService.findByProfesional(profesionalId));
    }

    /**
     * Obtener citas por rango de fechas
     */
    @GetMapping("/rango")
    public ResponseEntity<List<CitaDTO>> getByFechaRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.info("GET /api/citas/rango?inicio={}&fin={}", inicio, fin);
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
    public ResponseEntity<CitaDTO> update(@PathVariable Long id, @Valid @RequestBody CitaDTO dto) {
        log.info("PUT /api/citas/{}", id);
        return ResponseEntity.ok(citaService.update(id, dto));
    }

    /**
     * Cambiar estado de una cita
     * ADMIN, RECEPCION, VET
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'VET')")
    public ResponseEntity<CitaDTO> cambiarEstado(@PathVariable Long id, @RequestParam Cita.EstadoCita estado) {
        log.info("PATCH /api/citas/{}/estado?estado={}", id, estado);
        return ResponseEntity.ok(citaService.cambiarEstado(id, estado));
    }

    /**
     * Eliminar una cita
     * ADMIN, RECEPCION
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/citas/{}", id);
        citaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

