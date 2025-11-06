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
 * Controlador REST para gestión de consultas médicas (historia clínica)
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

