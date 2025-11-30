package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.VacunaDTO;
import com.clinica.veterinaria.service.VacunaService;
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
 * Controlador REST para gestión de tipos de vacunas.
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@RestController
@RequestMapping("/api/vacunas")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class VacunaController {

    private final VacunaService vacunaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<VacunaDTO>> getAll() {
        log.info("GET /api/vacunas");
        return ResponseEntity.ok(vacunaService.findAll());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<Page<VacunaDTO>> getPage(Pageable pageable) {
        log.info("GET /api/vacunas/page");
        return ResponseEntity.ok(vacunaService.findAll(pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<Page<VacunaDTO>> searchWithFilters(
        @RequestParam(required = false) String nombre,
        @RequestParam(required = false) String especie,
        Pageable pageable
    ) {
        log.info("GET /api/vacunas/search - nombre: {}, especie: {}", nombre, especie);
        return ResponseEntity.ok(vacunaService.searchWithFilters(nombre, especie, pageable));
    }

    @GetMapping("/activas")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<VacunaDTO>> getActivas() {
        log.info("GET /api/vacunas/activas");
        return ResponseEntity.ok(vacunaService.findActivas());
    }

    @GetMapping("/especie/{especie}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<List<VacunaDTO>> getByEspecie(@PathVariable String especie) {
        log.info("GET /api/vacunas/especie/{}", especie);
        return ResponseEntity.ok(vacunaService.findByEspecie(especie));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<VacunaDTO> getById(@PathVariable Long id) {
        log.info("GET /api/vacunas/{}", id);
        return ResponseEntity.ok(vacunaService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<VacunaDTO> create(@Valid @RequestBody VacunaDTO dto) {
        log.info("POST /api/vacunas");
        return ResponseEntity.status(HttpStatus.CREATED).body(vacunaService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<VacunaDTO> update(@PathVariable Long id, @Valid @RequestBody VacunaDTO dto) {
        log.info("PUT /api/vacunas/{}", id);
        return ResponseEntity.ok(vacunaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("DELETE /api/vacunas/{}", id);
        vacunaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

