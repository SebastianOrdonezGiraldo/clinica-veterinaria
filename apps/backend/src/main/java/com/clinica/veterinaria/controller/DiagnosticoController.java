package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para diagnóstico del sistema.
 * Útil para verificar configuración de email y otros servicios.
 */
@RestController
@RequestMapping("/api/diagnostico")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DiagnosticoController {

    private final EmailService emailService;

    /**
     * Verifica la configuración de email
     * Endpoint público para diagnóstico
     */
    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> diagnosticarEmail() {
        log.info("GET /api/diagnostico/email - Verificando configuración de email");
        
        Map<String, Object> resultado = new HashMap<>();
        boolean configOk = emailService.verificarConfiguracion();
        
        resultado.put("configuracionOk", configOk);
        resultado.put("mensaje", configOk 
            ? "Configuración de email correcta" 
            : "Revisa los logs para más detalles sobre qué falta configurar");
        
        return ResponseEntity.ok(resultado);
    }
}

