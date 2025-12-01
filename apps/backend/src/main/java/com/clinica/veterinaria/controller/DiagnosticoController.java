package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para diagnóstico del sistema.
 * Útil para verificar configuración de servicios.
 */
@RestController
@RequestMapping("/api/diagnostico")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DiagnosticoController {

    private final EmailService emailService;

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${cors.allowed-origins}")
    private String corsOrigins;

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

    /**
     * Verifica la configuración de email
     * Endpoint público para diagnóstico
     */
    @GetMapping("/email")
    public ResponseEntity<Map<String, Object>> diagnosticarEmail() {
        log.info("GET /api/diagnostico/email - Verificando configuración de email");
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Verificar configuración básica
            boolean configOk = mailHost != null && !mailHost.trim().isEmpty() &&
                              mailUsername != null && !mailUsername.trim().isEmpty() &&
                              mailFrom != null && !mailFrom.trim().isEmpty();
            
            resultado.put("configuracionOk", configOk);
            resultado.put("mailHost", mailHost != null && !mailHost.isEmpty() ? "configurado" : "no configurado");
            resultado.put("mailUsername", mailUsername != null && !mailUsername.isEmpty() 
                ? mailUsername.substring(0, Math.min(3, mailUsername.length())) + "***" 
                : "no configurado");
            resultado.put("mailFrom", mailFrom != null && !mailFrom.isEmpty() ? mailFrom : "no configurado");
            resultado.put("mensaje", configOk 
                ? "Configuración de email parece estar correcta" 
                : "Revisa las variables de entorno MAIL_HOST, MAIL_USERNAME y MAIL_FROM");
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al diagnosticar email: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("configuracionOk", false);
            error.put("mensaje", "Error al verificar configuración: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Verifica la configuración de CORS
     */
    @GetMapping("/cors")
    public ResponseEntity<Map<String, Object>> diagnosticarCors() {
        log.info("GET /api/diagnostico/cors - Verificando configuración de CORS");
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Leer y parsear los orígenes permitidos
            String[] origenes = corsOrigins != null ? corsOrigins.split(",") : new String[0];
            
            resultado.put("origenesConfigurados", Arrays.asList(origenes));
            resultado.put("cantidadOrigenes", origenes.length);
            resultado.put("configuracionOk", origenes.length > 0);
            resultado.put("mensaje", origenes.length > 0 
                ? "CORS configurado con " + origenes.length + " origen(es)" 
                : "No hay orígenes CORS configurados. Revisa la variable CORS_ALLOWED_ORIGINS");
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al diagnosticar CORS: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("configuracionOk", false);
            error.put("mensaje", "Error al verificar configuración: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Envía un email de prueba
     * Útil para verificar que el envío de correos funciona correctamente
     */
    @PostMapping("/email/test")
    public ResponseEntity<Map<String, Object>> enviarEmailPrueba(@RequestBody Map<String, String> request) {
        log.info("POST /api/diagnostico/email/test - Enviando email de prueba");
        
        String destinatario = request != null ? request.get("email") : null;
        if (destinatario == null || destinatario.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("exito", false);
            error.put("mensaje", "El campo 'email' es requerido en el body");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Enviar email de prueba usando el servicio
            boolean enviado = emailService.enviarEmailConfirmacionCita(
                destinatario,
                "Usuario de Prueba",
                "Mascota de Prueba",
                java.time.LocalDateTime.now().plusDays(1),
                "Prueba de sistema de correos",
                "Dr. Prueba"
            );
            
            resultado.put("exito", enviado);
            resultado.put("mensaje", enviado 
                ? "Email de prueba enviado exitosamente a " + destinatario 
                : "Error al enviar email de prueba. Revisa los logs para más detalles.");
            
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al enviar email de prueba: {}", e.getMessage(), e);
            resultado.put("exito", false);
            resultado.put("mensaje", "Error: " + e.getMessage());
            return ResponseEntity.status(500).body(resultado);
        }
    }
}

