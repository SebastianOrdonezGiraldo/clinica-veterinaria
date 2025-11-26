package com.clinica.veterinaria.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para diagnóstico del sistema.
 */
@RestController
@RequestMapping("/api/diagnostico")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DiagnosticoController {

    /**
     * Endpoint de salud del sistema
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("status", "ok");
        resultado.put("mensaje", "Sistema funcionando correctamente");
        return ResponseEntity.ok(resultado);
    }
}

