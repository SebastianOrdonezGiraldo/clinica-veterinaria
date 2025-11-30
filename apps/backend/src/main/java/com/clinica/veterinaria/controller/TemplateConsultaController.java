package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.TemplateConsultaDTO;
import com.clinica.veterinaria.repository.UsuarioRepository;
import com.clinica.veterinaria.service.TemplateConsultaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates/consultas")
@RequiredArgsConstructor
public class TemplateConsultaController {

    private final TemplateConsultaService service;
    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<TemplateConsultaDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<TemplateConsultaDTO>> getByCategoria(@PathVariable String categoria) {
        return ResponseEntity.ok(service.findByCategoria(categoria));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<List<TemplateConsultaDTO>> search(@RequestParam String nombre) {
        return ResponseEntity.ok(service.search(nombre));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<TemplateConsultaDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<TemplateConsultaDTO> create(
            @Valid @RequestBody TemplateConsultaDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long usuarioId = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
            .getId();
        TemplateConsultaDTO created = service.create(dto, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET')")
    public ResponseEntity<TemplateConsultaDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TemplateConsultaDTO dto) {
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

