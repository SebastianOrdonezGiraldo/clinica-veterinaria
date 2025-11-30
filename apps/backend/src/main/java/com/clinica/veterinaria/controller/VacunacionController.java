package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.VacunacionDTO;
import com.clinica.veterinaria.service.VacunacionService;
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
import java.util.Optional;

/**
 * Controlador REST para gestión de registros de vacunaciones.
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@RestController
@RequestMapping("/api/vacunaciones")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VacunacionController {

    private final VacunacionService vacunacionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<Page<VacunacionDTO>> getAll(Pageable pageable) {
        log.info("GET /api/vacunaciones");
        return ResponseEntity.ok(vacunacionService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<VacunacionDTO> getById(@PathVariable Long id) {
        log.info("GET /api/vacunaciones/{}", id);
        return ResponseEntity.ok(vacunacionService.findById(id));
    }

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<VacunacionDTO>> getByPaciente(@PathVariable Long pacienteId) {
        log.info("GET /api/vacunaciones/paciente/{}", pacienteId);
        return ResponseEntity.ok(vacunacionService.findByPaciente(pacienteId));
    }

    @GetMapping("/paciente/{pacienteId}/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<Page<VacunacionDTO>> getByPacientePage(
        @PathVariable Long pacienteId,
        Pageable pageable
    ) {
        log.info("GET /api/vacunaciones/paciente/{}/page", pacienteId);
        return ResponseEntity.ok(vacunacionService.findByPaciente(pacienteId, pageable));
    }

    @GetMapping("/proximas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<List<VacunacionDTO>> getProximasAVencer(
        @RequestParam(defaultValue = "30") int dias
    ) {
        log.info("GET /api/vacunaciones/proximas?dias={}", dias);
        return ResponseEntity.ok(vacunacionService.findProximasAVencer(dias));
    }

    @GetMapping("/vencidas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION')")
    public ResponseEntity<List<VacunacionDTO>> getVencidas() {
        log.info("GET /api/vacunaciones/vencidas");
        return ResponseEntity.ok(vacunacionService.findVencidas());
    }

    @GetMapping("/paciente/{pacienteId}/vacuna/{vacunaId}/ultima")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<VacunacionDTO> getUltimaVacunacion(
        @PathVariable Long pacienteId,
        @PathVariable Long vacunaId
    ) {
        log.info("GET /api/vacunaciones/paciente/{}/vacuna/{}/ultima", pacienteId, vacunaId);
        Optional<VacunacionDTO> ultima = vacunacionService.findUltimaVacunacion(pacienteId, vacunaId);
        return ultima.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<VacunacionDTO> create(@Valid @RequestBody VacunacionDTO dto) {
        log.info("POST /api/vacunaciones");
        return ResponseEntity.status(HttpStatus.CREATED).body(vacunacionService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<VacunacionDTO> update(@PathVariable Long id, @Valid @RequestBody VacunacionDTO dto) {
        log.info("PUT /api/vacunaciones/{}", id);
        return ResponseEntity.ok(vacunacionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/vacunaciones/{}", id);
        vacunacionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

