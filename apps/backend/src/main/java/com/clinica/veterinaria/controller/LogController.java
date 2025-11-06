package com.clinica.veterinaria.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Controlador para recibir logs del frontend
 * 
 * Permite al frontend enviar logs importantes (errores, warnings)
 * al backend para centralizar el logging y facilitar el debugging.
 */
@RestController
@RequestMapping("/api/logs")
public class LogController {
    
    private static final Logger frontendLogger = LoggerFactory.getLogger("com.clinica.veterinaria.frontend");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Endpoint para recibir logs del frontend
     * 
     * POST /api/logs/frontend
     * Body: {
     *   "level": "ERROR|WARN|INFO|DEBUG",
     *   "message": "Mensaje del log",
     *   "timestamp": "2024-01-01T12:00:00.000Z",
     *   "context": { ... },
     *   "error": { ... },
     *   "url": "http://...",
     *   "userId": "123",
     *   "correlationId": "abc-123"
     * }
     */
    @PostMapping("/frontend")
    public ResponseEntity<Map<String, String>> receiveFrontendLog(@RequestBody Map<String, Object> logData) {
        try {
            // Extraer datos del log
            String level = (String) logData.get("level");
            String message = (String) logData.get("message");
            String timestamp = (String) logData.get("timestamp");
            String url = (String) logData.get("url");
            String userId = (String) logData.get("userId");
            String correlationId = (String) logData.get("correlationId");
            Map<String, Object> context = (Map<String, Object>) logData.get("context");
            Map<String, Object> error = (Map<String, Object>) logData.get("error");
            
            // Añadir metadata al MDC
            if (correlationId != null) {
                MDC.put("correlationId", correlationId);
            }
            if (userId != null) {
                MDC.put("userId", userId);
            }
            MDC.put("source", "frontend");
            MDC.put("frontendUrl", url != null ? url : "unknown");
            
            // Construir mensaje completo
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("[FRONTEND] ").append(message);
            
            if (url != null) {
                logMessage.append(" | URL: ").append(url);
            }
            
            if (context != null && !context.isEmpty()) {
                logMessage.append(" | Context: ").append(context);
            }
            
            if (error != null && !error.isEmpty()) {
                logMessage.append(" | Error: ").append(error);
            }
            
            // Registrar según el nivel
            switch (level != null ? level.toUpperCase() : "INFO") {
                case "ERROR":
                    frontendLogger.error(logMessage.toString());
                    break;
                case "WARN":
                    frontendLogger.warn(logMessage.toString());
                    break;
                case "DEBUG":
                    frontendLogger.debug(logMessage.toString());
                    break;
                case "INFO":
                default:
                    frontendLogger.info(logMessage.toString());
                    break;
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Log received",
                "timestamp", LocalDateTime.now().format(formatter)
            ));
            
        } catch (Exception e) {
            // No queremos que un error en el logging rompa la aplicación
            frontendLogger.error("Error processing frontend log", e);
            return ResponseEntity.ok(Map.of(
                "status", "error",
                "message", "Failed to process log"
            ));
        } finally {
            // Limpiar MDC
            MDC.remove("source");
            MDC.remove("frontendUrl");
        }
    }
    
    /**
     * Endpoint para health check del servicio de logging
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "logging",
            "timestamp", LocalDateTime.now().format(formatter)
        ));
    }
}

