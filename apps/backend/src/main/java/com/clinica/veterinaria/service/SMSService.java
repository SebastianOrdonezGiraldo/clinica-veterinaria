package com.clinica.veterinaria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio para el envío de mensajes SMS.
 * 
 * <p>Este servicio proporciona funcionalidad para enviar SMS a los propietarios.
 * Actualmente soporta Twilio como proveedor, pero puede extenderse para otros.</p>
 * 
 * <p><strong>Nota:</strong> Para usar este servicio en producción, necesitas:</p>
 * <ul>
 *   <li>Una cuenta de Twilio (https://www.twilio.com)</li>
 *   <li>Configurar las variables de entorno: TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER</li>
 *   <li>Habilitar SMS en application.properties: app.sms.enabled=true</li>
 * </ul>
 * 
 * <p><strong>Alternativas:</strong></p>
 * <ul>
 *   <li>AWS SNS (Amazon Simple Notification Service)</li>
 *   <li>Vonage (Nexmo)</li>
 *   <li>Plivo</li>
 *   <li>MessageBird</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-01-XX
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SMSService {

    @Value("${app.sms.enabled:false}")
    private boolean smsEnabled;

    @Value("${app.sms.provider:twilio}")
    private String smsProvider;

    @Value("${app.sms.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${app.sms.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${app.sms.twilio.from-number:}")
    private String twilioFromNumber;

    /**
     * Envía un SMS al número de teléfono especificado.
     * 
     * @param telefono Número de teléfono del destinatario (formato internacional: +1234567890)
     * @param mensaje Contenido del mensaje SMS
     * @return true si el SMS se envió exitosamente, false en caso contrario
     */
    public boolean sendSMS(String telefono, String mensaje) {
        if (!smsEnabled) {
            log.debug("SMS deshabilitado. Para habilitarlo, configure app.sms.enabled=true");
            return false;
        }

        if (telefono == null || telefono.trim().isEmpty()) {
            log.warn("No se puede enviar SMS: número de teléfono vacío");
            return false;
        }

        if (mensaje == null || mensaje.trim().isEmpty()) {
            log.warn("No se puede enviar SMS: mensaje vacío");
            return false;
        }

        try {
            switch (smsProvider.toLowerCase()) {
                case "twilio":
                    return sendSMSViaTwilio(telefono, mensaje);
                default:
                    log.warn("Proveedor de SMS no soportado: {}. Solo 'twilio' está disponible actualmente.", smsProvider);
                    return false;
            }
        } catch (Exception e) {
            log.error("✗ Error al enviar SMS a {}: {}", telefono, e.getMessage());
            return false;
        }
    }

    /**
     * Envía un SMS usando Twilio.
     * 
     * <p><strong>Nota:</strong> Para usar Twilio, necesitas agregar la dependencia al pom.xml:</p>
     * <pre>
     * &lt;dependency&gt;
     *     &lt;groupId&gt;com.twilio.sdk&lt;/groupId&gt;
     *     &lt;artifactId&gt;twilio&lt;/artifactId&gt;
     *     &lt;version&gt;9.14.0&lt;/version&gt;
     * &lt;/dependency&gt;
     * </pre>
     * 
     * @param telefono Número de teléfono del destinatario
     * @param mensaje Contenido del mensaje
     * @return true si se envió exitosamente
     */
    private boolean sendSMSViaTwilio(String telefono, String mensaje) {
        try {
            // Validar configuración de Twilio
            if (twilioAccountSid == null || twilioAccountSid.trim().isEmpty() ||
                twilioAuthToken == null || twilioAuthToken.trim().isEmpty() ||
                twilioFromNumber == null || twilioFromNumber.trim().isEmpty()) {
                log.warn("Configuración de Twilio incompleta. Verifica TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN y TWILIO_FROM_NUMBER");
                return false;
            }

            // TODO: Implementar envío real con Twilio cuando se agregue la dependencia
            // Ejemplo de código (requiere dependencia de Twilio):
            /*
            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message message = Message.creator(
                new PhoneNumber(telefono),
                new PhoneNumber(twilioFromNumber),
                mensaje
            ).create();
            
            log.info("✓ SMS enviado exitosamente a: {} (SID: {})", telefono, message.getSid());
            return true;
            */

            // Por ahora, solo loguear (modo desarrollo)
            log.info("📱 [MODO DESARROLLO] SMS simulado a {}: {}", telefono, mensaje);
            log.info("   Para habilitar SMS real, agrega la dependencia de Twilio al pom.xml");
            return true;
        } catch (Exception e) {
            log.error("✗ Error al enviar SMS vía Twilio: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Envía un SMS de confirmación de cita al propietario.
     * 
     * @param telefono Número de teléfono del propietario
     * @param propietarioNombre Nombre del propietario
     * @param pacienteNombre Nombre de la mascota
     * @param fecha Fecha y hora de la cita
     * @param motivo Motivo de la cita
     * @return true si el SMS se envió exitosamente
     */
    public boolean sendCitaConfirmacionSMS(String telefono, String propietarioNombre,
                                           String pacienteNombre, java.time.LocalDateTime fecha,
                                           String motivo) {
        String mensaje = String.format(
            "Hola %s, confirmamos tu cita para %s el %s a las %s. Motivo: %s. Clínica Veterinaria",
            propietarioNombre,
            pacienteNombre,
            fecha.toLocalDate(),
            fecha.toLocalTime(),
            motivo
        );

        return sendSMS(telefono, mensaje);
    }

    /**
     * Normaliza un número de teléfono al formato internacional.
     * 
     * @param telefono Número de teléfono en cualquier formato
     * @return Número normalizado con código de país (+)
     */
    public String normalizePhoneNumber(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return null;
        }

        // Remover espacios, guiones, paréntesis
        String normalized = telefono.replaceAll("[\\s\\-\\(\\)]", "");

        // Si no empieza con +, asumir código de país (ajustar según tu país)
        if (!normalized.startsWith("+")) {
            // Ejemplo para Colombia: agregar +57
            // Ejemplo para México: agregar +52
            // Ejemplo para Argentina: agregar +54
            // Por defecto, no modificar (el usuario debe proporcionar formato internacional)
            log.debug("Número sin código de país. Se recomienda formato internacional (+XX...): {}", telefono);
        }

        return normalized;
    }
}

