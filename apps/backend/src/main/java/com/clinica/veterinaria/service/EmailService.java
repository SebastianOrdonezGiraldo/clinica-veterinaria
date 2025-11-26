package com.clinica.veterinaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

/**
 * Servicio para el envío de correos electrónicos.
 * 
 * <p>Este servicio proporciona funcionalidad para enviar emails tanto en formato
 * texto plano como HTML usando plantillas Thymeleaf.</p>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Envío de emails simples (texto plano)</li>
 *   <li>Envío de emails HTML usando plantillas Thymeleaf</li>
 *   <li>Manejo de errores sin interrumpir el flujo principal</li>
 *   <li>Logging detallado de operaciones</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
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

    /**
     * Envía un email simple en texto plano.
     * 
     * @param to Dirección de correo del destinatario
     * @param subject Asunto del correo
     * @param text Contenido del correo
     * @return true si el email se envió exitosamente, false en caso contrario
     */
    public boolean sendSimpleEmail(String to, String subject, String text) {
        try {
            if (to == null || to.trim().isEmpty()) {
                log.warn("No se puede enviar email: dirección de correo vacía");
                return false;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("✓ Email enviado exitosamente a: {}", to);
            return true;
        } catch (MailException e) {
            log.error("✗ Error al enviar email a {}: {}", to, e.getMessage());
            return false;
        }
    }

    /**
     * Envía un email HTML usando una plantilla Thymeleaf.
     * 
     * @param to Dirección de correo del destinatario
     * @param subject Asunto del correo
     * @param templateName Nombre de la plantilla Thymeleaf (sin extensión .html)
     * @param context Contexto con las variables para la plantilla
     * @return true si el email se envió exitosamente, false en caso contrario
     */
    public boolean sendHtmlEmail(String to, String subject, String templateName, Context context) {
        try {
            if (to == null || to.trim().isEmpty()) {
                log.warn("No se puede enviar email: dirección de correo vacía");
                return false;
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            try {
                helper.setFrom(fromEmail, fromName);
            } catch (UnsupportedEncodingException e) {
                log.warn("Error al establecer nombre del remitente, usando solo email: {}", e.getMessage());
                helper.setFrom(fromEmail);
            }
            helper.setTo(to);
            helper.setSubject(subject);

            // Procesar plantilla Thymeleaf
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("✓ Email HTML enviado exitosamente a: {}", to);
            return true;
        } catch (MessagingException e) {
            log.error("✗ Error al crear mensaje HTML para {}: {}", to, e.getMessage());
            return false;
        } catch (MailException e) {
            log.error("✗ Error al enviar email HTML a {}: {}", to, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("✗ Error inesperado al enviar email HTML a {}: {}", to, e.getMessage());
            return false;
        }
    }

    /**
     * Envía un email de confirmación de cita al propietario.
     * 
     * @param propietarioEmail Email del propietario
     * @param propietarioNombre Nombre del propietario
     * @param pacienteNombre Nombre de la mascota
     * @param fecha Fecha y hora de la cita
     * @param motivo Motivo de la cita
     * @param profesionalNombre Nombre del veterinario
     * @return true si el email se envió exitosamente
     */
    public boolean sendCitaConfirmacionEmail(String propietarioEmail, String propietarioNombre,
                                             String pacienteNombre, java.time.LocalDateTime fecha,
                                             String motivo, String profesionalNombre) {
        try {
            Context context = new Context();
            context.setVariable("propietarioNombre", propietarioNombre);
            context.setVariable("pacienteNombre", pacienteNombre);
            context.setVariable("fecha", fecha.toLocalDate());
            context.setVariable("hora", fecha.toLocalTime());
            context.setVariable("motivo", motivo);
            context.setVariable("profesionalNombre", profesionalNombre);
            context.setVariable("clinicaNombre", "Clínica Veterinaria");

            String subject = String.format("Confirmación de cita - %s", pacienteNombre);
            
            return sendHtmlEmail(propietarioEmail, subject, "email/cita-confirmacion", context);
        } catch (Exception e) {
            log.error("✗ Error al enviar email de confirmación de cita: {}", e.getMessage());
            return false;
        }
    }
}

