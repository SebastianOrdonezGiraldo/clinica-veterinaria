package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.TemplatePrescripcionDTO;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.service.TemplatePrescripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates/prescripciones")
@RequiredArgsConstructor
public class TemplatePrescripcionController {

    private final TemplatePrescripcionService service;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<TemplatePrescripcionDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/categorias")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<String>> getCategorias() {
        return ResponseEntity.ok(service.getCategorias());
    }

    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<TemplatePrescripcionDTO>> getByCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(service.findByCategoria(categoria));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<TemplatePrescripcionDTO>> search(@RequestParam String nombre) {
        return ResponseEntity.ok(service.search(nombre));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<TemplatePrescripcionDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<TemplatePrescripcionDTO> create(
            @Valid @RequestBody TemplatePrescripcionDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long usuarioId = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
            .getId();
        TemplatePrescripcionDTO created = service.create(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<TemplatePrescripcionDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TemplatePrescripcionDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/usar")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<Void> incrementarUso(@PathVariable Long id) {
        service.incrementarUso(id);
        return ResponseEntity.ok().build();
    }
}

