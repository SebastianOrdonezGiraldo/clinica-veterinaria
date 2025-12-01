package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.dto.HistorialMedicoDTO;
import com.clinica.veterinaria.service.HistorialMedicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para el historial médico completo de pacientes
 */
@RestController
@RequestMapping("/api/historial-medico")
@RequiredArgsConstructor
public class HistorialMedicoController {

    private final HistorialMedicoService historialMedicoService;

    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE')")
    public ResponseEntity<HistorialMedicoDTO> getHistorialCompleto(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(historialMedicoService.getHistorialCompleto(pacienteId));
    }
}

