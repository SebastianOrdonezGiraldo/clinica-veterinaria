package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.NotificacionWebSocketDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Servicio para enviar notificaciones a través de WebSocket.
 * 
 * <p>Este servicio permite enviar notificaciones en tiempo real
 * a usuarios específicos o a todos los usuarios conectados.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-30
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Envía una notificación a un usuario específico.
     * 
     * @param userId ID del usuario destinatario
     * @param notificacion DTO de la notificación
     */
    public void enviarNotificacionAUsuario(Long userId, NotificacionWebSocketDTO notificacion) {
        log.debug("Enviando notificación WebSocket a usuario ID: {}", userId);
        String destination = "/user/" + userId + "/queue/notificaciones";
        messagingTemplate.convertAndSend(destination, notificacion);
        log.info("Notificación enviada exitosamente a usuario ID: {}", userId);
    }

    /**
     * Envía una notificación a todos los usuarios conectados.
     * 
     * @param notificacion DTO de la notificación
     */
    public void enviarNotificacionGlobal(NotificacionWebSocketDTO notificacion) {
        log.debug("Enviando notificación WebSocket global");
        messagingTemplate.convertAndSend("/topic/notificaciones", notificacion);
        log.info("Notificación global enviada exitosamente");
    }

    /**
     * Envía un contador de notificaciones no leídas a un usuario.
     * 
     * @param userId ID del usuario
     * @param count Cantidad de notificaciones no leídas
     */
    public void enviarContadorNotificaciones(Long userId, Long count) {
        log.debug("Enviando contador de notificaciones a usuario ID: {} - Count: {}", userId, count);
        String destination = "/user/" + userId + "/queue/notificaciones-count";
        messagingTemplate.convertAndSend(destination, count);
    }
}

