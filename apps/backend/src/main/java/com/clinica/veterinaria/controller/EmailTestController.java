package com.clinica.veterinaria.controller;

import com.clinica.veterinaria.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador temporal para probar el envío de correos electrónicos.
 * 
 * <p><strong>NOTA:</strong> Este controlador es solo para desarrollo y pruebas.
 * Debe ser removido o deshabilitado en producción.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-12-XX
 */
@RestController
@RequestMapping("/api/test/email")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')") // Solo admins pueden probar correos
@Profile("dev") // Solo disponible en desarrollo
public class EmailTestController {

    private final EmailService emailService;

    /**
     * Prueba el envío de email de bienvenida para usuario del sistema.
     * 
     * @param email Email de destino
     * @param nombre Nombre del usuario
     * @param rol Rol del usuario (ADMIN, VET, RECEPCION, ESTUDIANTE)
     * @return Resultado del envío
     */
    @PostMapping("/bienvenida-usuario")
    public ResponseEntity<Map<String, Object>> testBienvenidaUsuario(
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "Usuario de Prueba") String nombre,
            @RequestParam(required = false, defaultValue = "Veterinario") String rol) {
        
        log.info("🧪 TEST: Enviando email de bienvenida usuario a: {}", email);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean enviado = emailService.enviarEmailBienvenidaUsuario(email, nombre, rol);
            
            response.put("success", enviado);
            response.put("message", enviado 
                ? "Email de bienvenida enviado exitosamente" 
                : "Error al enviar email de bienvenida");
            response.put("email", email);
            response.put("tipo", "bienvenida-usuario");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("✗ Error al probar envío de email de bienvenida usuario: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Prueba el envío de email de bienvenida para cliente/propietario.
     * 
     * @param email Email de destino
     * @param nombre Nombre del propietario
     * @return Resultado del envío
     */
    @PostMapping("/bienvenida-cliente")
    public ResponseEntity<Map<String, Object>> testBienvenidaCliente(
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "Cliente de Prueba") String nombre) {
        
        log.info("🧪 TEST: Enviando email de bienvenida cliente a: {}", email);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean enviado = emailService.enviarEmailBienvenidaCliente(email, nombre);
            
            response.put("success", enviado);
            response.put("message", enviado 
                ? "Email de bienvenida enviado exitosamente" 
                : "Error al enviar email de bienvenida");
            response.put("email", email);
            response.put("tipo", "bienvenida-cliente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("✗ Error al probar envío de email de bienvenida cliente: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Prueba el envío de email de cambio de contraseña para usuario del sistema.
     * 
     * @param email Email de destino
     * @param nombre Nombre del usuario
     * @param esResetAdmin true si es reset por admin, false si es cambio por el usuario
     * @return Resultado del envío
     */
    @PostMapping("/cambio-password-usuario")
    public ResponseEntity<Map<String, Object>> testCambioPasswordUsuario(
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "Usuario de Prueba") String nombre,
            @RequestParam(required = false, defaultValue = "false") boolean esResetAdmin) {
        
        log.info("🧪 TEST: Enviando email de cambio de contraseña usuario a: {}", email);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean enviado = emailService.enviarEmailCambioPasswordUsuario(email, nombre, esResetAdmin);
            
            response.put("success", enviado);
            response.put("message", enviado 
                ? "Email de cambio de contraseña enviado exitosamente" 
                : "Error al enviar email de cambio de contraseña");
            response.put("email", email);
            response.put("tipo", "cambio-password-usuario");
            response.put("esResetAdmin", esResetAdmin);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("✗ Error al probar envío de email de cambio de contraseña usuario: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Prueba el envío de email de cambio de contraseña para cliente/propietario.
     * 
     * @param email Email de destino
     * @param nombre Nombre del propietario
     * @return Resultado del envío
     */
    @PostMapping("/cambio-password-cliente")
    public ResponseEntity<Map<String, Object>> testCambioPasswordCliente(
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "Cliente de Prueba") String nombre) {
        
        log.info("🧪 TEST: Enviando email de cambio de contraseña cliente a: {}", email);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean enviado = emailService.enviarEmailCambioPasswordCliente(email, nombre);
            
            response.put("success", enviado);
            response.put("message", enviado 
                ? "Email de cambio de contraseña enviado exitosamente" 
                : "Error al enviar email de cambio de contraseña");
            response.put("email", email);
            response.put("tipo", "cambio-password-cliente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("✗ Error al probar envío de email de cambio de contraseña cliente: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Prueba el envío de email de confirmación de cita.
     * 
     * @param email Email de destino
     * @param nombrePropietario Nombre del propietario
     * @param nombrePaciente Nombre de la mascota
     * @return Resultado del envío
     */
    @PostMapping("/confirmacion-cita")
    public ResponseEntity<Map<String, Object>> testConfirmacionCita(
            @RequestParam String email,
            @RequestParam(required = false, defaultValue = "Propietario de Prueba") String nombrePropietario,
            @RequestParam(required = false, defaultValue = "Mascota de Prueba") String nombrePaciente,
            @RequestParam(required = false, defaultValue = "Dra. María García") String profesionalNombre) {
        
        log.info("🧪 TEST: Enviando email de confirmación de cita a: {}", email);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDateTime fecha = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            boolean enviado = emailService.enviarEmailConfirmacionCita(
                email,
                nombrePropietario,
                nombrePaciente,
                fecha,
                "Consulta general",
                profesionalNombre
            );
            
            response.put("success", enviado);
            response.put("message", enviado 
                ? "Email de confirmación de cita enviado exitosamente" 
                : "Error al enviar email de confirmación de cita");
            response.put("email", email);
            response.put("tipo", "confirmacion-cita");
            response.put("fecha", fecha.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("✗ Error al probar envío de email de confirmación de cita: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Prueba todos los tipos de correos en secuencia.
     * 
     * @param email Email de destino
     * @return Resultado de todos los envíos
     */
    @PostMapping("/todos")
    public ResponseEntity<Map<String, Object>> testTodosLosCorreos(@RequestParam String email) {
        
        log.info("🧪 TEST: Enviando todos los tipos de correos a: {}", email);
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Boolean> resultados = new HashMap<>();
        
        try {
            // Bienvenida usuario
            resultados.put("bienvenida-usuario", 
                emailService.enviarEmailBienvenidaUsuario(email, "Usuario de Prueba", "Veterinario"));
            
            Thread.sleep(1000); // Esperar 1 segundo entre envíos
            
            // Bienvenida cliente
            resultados.put("bienvenida-cliente", 
                emailService.enviarEmailBienvenidaCliente(email, "Cliente de Prueba"));
            
            Thread.sleep(1000);
            
            // Cambio password usuario
            resultados.put("cambio-password-usuario", 
                emailService.enviarEmailCambioPasswordUsuario(email, "Usuario de Prueba", false));
            
            Thread.sleep(1000);
            
            // Cambio password cliente
            resultados.put("cambio-password-cliente", 
                emailService.enviarEmailCambioPasswordCliente(email, "Cliente de Prueba"));
            
            Thread.sleep(1000);
            
            // Confirmación cita
            LocalDateTime fecha = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            resultados.put("confirmacion-cita", 
                emailService.enviarEmailConfirmacionCita(
                    email, "Propietario de Prueba", "Mascota de Prueba", 
                    fecha, "Consulta general", "Dra. María García"));
            
            long exitosos = resultados.values().stream().mapToLong(b -> b ? 1 : 0).sum();
            
            response.put("success", exitosos == resultados.size());
            response.put("message", String.format("Enviados %d de %d correos exitosamente", exitosos, resultados.size()));
            response.put("email", email);
            response.put("resultados", resultados);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("✗ Error al probar envío de todos los correos: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("resultados", resultados);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}

