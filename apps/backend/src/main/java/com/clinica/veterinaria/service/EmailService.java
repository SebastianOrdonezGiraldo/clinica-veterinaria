package com.clinica.veterinaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Servicio para el envío de correos electrónicos del sistema.
 * 
 * <p>Este servicio proporciona funcionalidad para enviar emails HTML usando
 * plantillas Thymeleaf para diferentes eventos del sistema:</p>
 * <ul>
 *   <li>Confirmación y actualización de citas</li>
 *   <li>Bienvenida a usuarios del sistema y clientes</li>
 *   <li>Notificaciones de cambio de contraseña</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 2.0.0
 * @since 2025-01-XX
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name:Clínica Veterinaria}")
    private String fromName;

    @Value("${app.mail.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.mail.logo.url:}")
    private String logoUrl;

    /**
     * Envía un email de confirmación cuando se crea una nueva cita.
     * 
     * @param propietarioEmail Email del propietario
     * @param propietarioNombre Nombre del propietario
     * @param pacienteNombre Nombre de la mascota
     * @param fecha Fecha y hora de la cita
     * @param motivo Motivo de la cita
     * @param profesionalNombre Nombre del veterinario
     * @return true si el email se envió exitosamente
     */
    public boolean enviarEmailConfirmacionCita(String propietarioEmail, String propietarioNombre,
                                               String pacienteNombre, LocalDateTime fecha,
                                               String motivo, String profesionalNombre) {
        try {
            log.info("📧 Enviando email de confirmación de cita a: {}", propietarioEmail);
            
            Context context = new Context(new Locale("es", "ES"));
            context.setVariable("propietarioNombre", propietarioNombre);
            context.setVariable("pacienteNombre", pacienteNombre);
            context.setVariable("fecha", fecha.toLocalDate());
            context.setVariable("hora", fecha.toLocalTime());
            context.setVariable("motivo", motivo);
            context.setVariable("profesionalNombre", profesionalNombre);
            context.setVariable("clinicaNombre", "Clínica Veterinaria Universitaria Humboldt");
            
            // Construir URL del logo
            String finalLogoUrl = logoUrl != null && !logoUrl.isEmpty() 
                ? logoUrl 
                : (baseUrl != null && !baseUrl.isEmpty() ? baseUrl + "/images/logo-clinica.webp" : "");
            context.setVariable("logoUrl", finalLogoUrl);

            String subject = String.format("Confirmación de cita - %s", pacienteNombre);
            
            return enviarEmailHtml(propietarioEmail, subject, "email/cita-confirmacion", context);
        } catch (Exception e) {
            log.error("✗ Error al enviar email de confirmación de cita: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envía un email cuando se cancela una cita.
     * 
     * @param propietarioEmail Email del propietario
     * @param propietarioNombre Nombre del propietario
     * @param pacienteNombre Nombre de la mascota
     * @param fecha Fecha y hora de la cita cancelada
     * @param motivo Motivo original de la cita
     * @param profesionalNombre Nombre del veterinario
     * @param razonCancelacion Razón de la cancelación (opcional)
     * @return true si el email se envió exitosamente
     */
    public boolean enviarEmailCancelacionCita(String propietarioEmail, String propietarioNombre,
                                              String pacienteNombre, LocalDateTime fecha,
                                              String motivo, String profesionalNombre, 
                                              String razonCancelacion) {
        try {
            log.info("📧 Enviando email de cancelación de cita a: {}", propietarioEmail);
            
            Context context = new Context(new Locale("es", "ES"));
            context.setVariable("propietarioNombre", propietarioNombre);
            context.setVariable("pacienteNombre", pacienteNombre);
            context.setVariable("fecha", fecha.toLocalDate());
            context.setVariable("hora", fecha.toLocalTime());
            context.setVariable("motivo", motivo);
            context.setVariable("profesionalNombre", profesionalNombre);
            context.setVariable("razonCancelacion", razonCancelacion != null ? razonCancelacion : "No especificada");
            context.setVariable("clinicaNombre", "Clínica Veterinaria Universitaria Humboldt");
            
            // Construir URL del logo
            String finalLogoUrl = logoUrl != null && !logoUrl.isEmpty() 
                ? logoUrl 
                : (baseUrl != null && !baseUrl.isEmpty() ? baseUrl + "/images/logo-clinica.webp" : "");
            context.setVariable("logoUrl", finalLogoUrl);

            String subject = String.format("Cancelación de cita - %s", pacienteNombre);
            
            return enviarEmailHtml(propietarioEmail, subject, "email/cita-cancelacion", context);
        } catch (Exception e) {
            log.error("✗ Error al enviar email de cancelación de cita: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envía un email cuando cambia el estado de una cita.
     * 
     * @param propietarioEmail Email del propietario
     * @param propietarioNombre Nombre del propietario
     * @param pacienteNombre Nombre de la mascota
     * @param fecha Fecha y hora de la cita
     * @param motivo Motivo de la cita
     * @param profesionalNombre Nombre del veterinario
     * @param nuevoEstado Nuevo estado de la cita
     * @return true si el email se envió exitosamente
     */
    public boolean enviarEmailCambioEstadoCita(String propietarioEmail, String propietarioNombre,
                                                String pacienteNombre, LocalDateTime fecha,
                                                String motivo, String profesionalNombre, 
                                                String nuevoEstado) {
        try {
            log.info("📧 Enviando email de cambio de estado de cita a: {}", propietarioEmail);
            
            Context context = new Context(new Locale("es", "ES"));
            context.setVariable("propietarioNombre", propietarioNombre);
            context.setVariable("pacienteNombre", pacienteNombre);
            context.setVariable("fecha", fecha.toLocalDate());
            context.setVariable("hora", fecha.toLocalTime());
            context.setVariable("motivo", motivo);
            context.setVariable("profesionalNombre", profesionalNombre);
            context.setVariable("nuevoEstado", nuevoEstado);
            context.setVariable("clinicaNombre", "Clínica Veterinaria Universitaria Humboldt");
            
            // Construir URL del logo
            String finalLogoUrl = logoUrl != null && !logoUrl.isEmpty() 
                ? logoUrl 
                : (baseUrl != null && !baseUrl.isEmpty() ? baseUrl + "/images/logo-clinica.webp" : "");
            context.setVariable("logoUrl", finalLogoUrl);

            String subject;
            if ("CONFIRMADA".equals(nuevoEstado)) {
                subject = String.format("Cita confirmada - %s", pacienteNombre);
            } else {
                subject = String.format("Actualización de cita - %s", pacienteNombre);
            }
            
            return enviarEmailHtml(propietarioEmail, subject, "email/cita-estado-actualizado", context);
        } catch (Exception e) {
            log.error("✗ Error al enviar email de cambio de estado de cita: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envía un email de bienvenida cuando se crea un nuevo usuario del sistema.
     * 
     * @param usuarioEmail Email del usuario
     * @param usuarioNombre Nombre del usuario
     * @param rolNombre Nombre del rol asignado (ej: "Administrador", "Veterinario")
     * @return true si el email se envió exitosamente
     */
    public boolean enviarEmailBienvenidaUsuario(String usuarioEmail, String usuarioNombre, String rolNombre) {
        try {
            log.info("📧 Enviando email de bienvenida a usuario: {}", usuarioEmail);
            
            Context context = new Context(new Locale("es", "ES"));
            context.setVariable("usuarioNombre", usuarioNombre);
            context.setVariable("usuarioEmail", usuarioEmail);
            context.setVariable("rolNombre", rolNombre);
            context.setVariable("clinicaNombre", "Clínica Veterinaria Universitaria Humboldt");
            
            String finalLogoUrl = logoUrl != null && !logoUrl.isEmpty() 
                ? logoUrl 
                : (baseUrl != null && !baseUrl.isEmpty() ? baseUrl + "/images/logo-clinica.webp" : "");
            context.setVariable("logoUrl", finalLogoUrl);
            context.setVariable("baseUrl", baseUrl);

            String subject = String.format("Bienvenido/a a %s", "Clínica Veterinaria Universitaria Humboldt");
            
            return enviarEmailHtml(usuarioEmail, subject, "email/usuario-bienvenida", context);
        } catch (Exception e) {
            log.error("✗ Error al enviar email de bienvenida a usuario: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envía un email de bienvenida cuando se crea un nuevo propietario/cliente.
     * 
     * @param propietarioEmail Email del propietario
     * @param propietarioNombre Nombre del propietario
     * @return true si el email se envió exitosamente
     */
    public boolean enviarEmailBienvenidaCliente(String propietarioEmail, String propietarioNombre) {
        try {
            log.info("📧 Enviando email de bienvenida a cliente: {}", propietarioEmail);
            
            Context context = new Context(new Locale("es", "ES"));
            context.setVariable("propietarioNombre", propietarioNombre);
            context.setVariable("clinicaNombre", "Clínica Veterinaria Universitaria Humboldt");
            
            String finalLogoUrl = logoUrl != null && !logoUrl.isEmpty() 
                ? logoUrl 
                : (baseUrl != null && !baseUrl.isEmpty() ? baseUrl + "/images/logo-clinica.webp" : "");
            context.setVariable("logoUrl", finalLogoUrl);
            context.setVariable("baseUrl", baseUrl);
            
            // Construir URL del portal del cliente
            String portalUrl = baseUrl != null && !baseUrl.isEmpty() 
                ? baseUrl + "/cliente/login" 
                : "http://localhost:5173/cliente/login";
            context.setVariable("portalUrl", portalUrl);

            String subject = "Bienvenido al Portal del Cliente";
            
            return enviarEmailHtml(propietarioEmail, subject, "email/cliente-bienvenida", context);
        } catch (Exception e) {
            log.error("✗ Error al enviar email de bienvenida a cliente: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envía un email cuando se cambia la contraseña de un usuario del sistema.
     * 
     * @param usuarioEmail Email del usuario
     * @param usuarioNombre Nombre del usuario
     * @param esResetAdmin true si el cambio fue realizado por un administrador, false si fue por el usuario
     * @return true si el email se envió exitosamente
     */
    public boolean enviarEmailCambioPasswordUsuario(String usuarioEmail, String usuarioNombre, boolean esResetAdmin) {
        try {
            log.info("📧 Enviando email de cambio de contraseña a usuario: {}", usuarioEmail);
            
            Context context = new Context(new Locale("es", "ES"));
            context.setVariable("usuarioNombre", usuarioNombre);
            context.setVariable("esResetAdmin", esResetAdmin);
            context.setVariable("fechaCambio", LocalDateTime.now());
            context.setVariable("titulo", esResetAdmin ? "Contraseña Reseteada" : "Contraseña Cambiada");
            context.setVariable("clinicaNombre", "Clínica Veterinaria Universitaria Humboldt");
            
            String finalLogoUrl = logoUrl != null && !logoUrl.isEmpty() 
                ? logoUrl 
                : (baseUrl != null && !baseUrl.isEmpty() ? baseUrl + "/images/logo-clinica.webp" : "");
            context.setVariable("logoUrl", finalLogoUrl);
            context.setVariable("baseUrl", baseUrl);

            String subject = esResetAdmin 
                ? "Su contraseña ha sido reseteada" 
                : "Confirmación de cambio de contraseña";
            
            return enviarEmailHtml(usuarioEmail, subject, "email/usuario-cambio-password", context);
        } catch (Exception e) {
            log.error("✗ Error al enviar email de cambio de contraseña a usuario: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Envía un email cuando se establece o cambia la contraseña de un propietario/cliente.
     * 
     * @param propietarioEmail Email del propietario
     * @param propietarioNombre Nombre del propietario
     * @return true si el email se envió exitosamente
     */
    public boolean enviarEmailCambioPasswordCliente(String propietarioEmail, String propietarioNombre) {
        try {
            log.info("📧 Enviando email de confirmación de contraseña a cliente: {}", propietarioEmail);
            
            Context context = new Context(new Locale("es", "ES"));
            context.setVariable("propietarioNombre", propietarioNombre);
            context.setVariable("titulo", "Contraseña Establecida");
            context.setVariable("clinicaNombre", "Clínica Veterinaria Universitaria Humboldt");
            
            String finalLogoUrl = logoUrl != null && !logoUrl.isEmpty() 
                ? logoUrl 
                : (baseUrl != null && !baseUrl.isEmpty() ? baseUrl + "/images/logo-clinica.webp" : "");
            context.setVariable("logoUrl", finalLogoUrl);
            context.setVariable("baseUrl", baseUrl);
            
            // Construir URL del portal del cliente
            String portalUrl = baseUrl != null && !baseUrl.isEmpty() 
                ? baseUrl + "/cliente/login" 
                : "http://localhost:5173/cliente/login";
            context.setVariable("portalUrl", portalUrl);

            String subject = "Contraseña establecida - Portal del Cliente";
            
            return enviarEmailHtml(propietarioEmail, subject, "email/cliente-cambio-password", context);
        } catch (Exception e) {
            log.error("✗ Error al enviar email de cambio de contraseña a cliente: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Método auxiliar para enviar emails HTML usando plantillas Thymeleaf.
     * 
     * @param to Dirección de correo del destinatario
     * @param subject Asunto del correo
     * @param templateName Nombre de la plantilla Thymeleaf (sin extensión .html)
     * @param context Contexto con las variables para la plantilla
     * @return true si el email se envió exitosamente
     */
    private boolean enviarEmailHtml(String to, String subject, String templateName, Context context) {
        try {
            if (to == null || to.trim().isEmpty()) {
                log.warn("⚠ No se puede enviar email: dirección de correo vacía");
                return false;
            }

            // Validar configuración
            if (fromEmail == null || fromEmail.trim().isEmpty()) {
                log.error("✗ No se puede enviar email: app.mail.from no está configurado");
                return false;
            }

            String safeFromName = (fromName != null && !fromName.trim().isEmpty()) ? fromName : "Clínica Veterinaria";

            log.debug("📧 Preparando email HTML - Destinatario: {}, Asunto: {}, Plantilla: {}", 
                to, subject, templateName);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            try {
                helper.setFrom(fromEmail, safeFromName);
            } catch (UnsupportedEncodingException e) {
                log.warn("⚠ Error al establecer nombre del remitente, usando solo email: {}", e.getMessage());
                helper.setFrom(fromEmail);
            }
            
            helper.setTo(to);
            helper.setSubject(subject != null ? subject : "Notificación de Cita");

            // Procesar plantilla Thymeleaf
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent != null ? htmlContent : "", true);

            mailSender.send(mimeMessage);
            log.info("✓ Email HTML enviado exitosamente a: {}", to);
            return true;
        } catch (MessagingException e) {
            log.error("✗ Error al crear mensaje HTML para {}: {}", to, e.getMessage(), e);
            return false;
        } catch (MailException e) {
            log.error("✗ Error al enviar email HTML a {}: {}", to, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("✗ Error inesperado al enviar email HTML a {}: {}", to, e.getMessage(), e);
            return false;
        }
    }
}

