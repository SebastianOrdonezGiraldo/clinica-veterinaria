package com.clinica.veterinaria.service;

import com.clinica.veterinaria.dto.NotificacionWebSocketDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para WebSocketNotificationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios de WebSocketNotificationService")
class WebSocketNotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketNotificationService webSocketNotificationService;

    private NotificacionWebSocketDTO notificacion;

    @BeforeEach
    void setUp() {
        notificacion = NotificacionWebSocketDTO.builder()
            .id(1L)
            .titulo("Test Notification")
            .mensaje("Test message")
            .usuarioId(1L)
            .build();
    }

    @Test
    @DisplayName("Debe enviar notificación a usuario específico")
    void testEnviarNotificacionAUsuario() {
        // Act
        webSocketNotificationService.enviarNotificacionAUsuario(1L, notificacion);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/user/1/queue/notificaciones"),
            eq(notificacion)
        );
    }

    @Test
    @DisplayName("Debe enviar notificación global")
    void testEnviarNotificacionGlobal() {
        // Act
        webSocketNotificationService.enviarNotificacionGlobal(notificacion);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/topic/notificaciones"),
            eq(notificacion)
        );
    }

    @Test
    @DisplayName("Debe enviar contador de notificaciones")
    void testEnviarContadorNotificaciones() {
        // Act
        webSocketNotificationService.enviarContadorNotificaciones(1L, 5L);

        // Assert
        verify(messagingTemplate, times(1)).convertAndSend(
            eq("/user/1/queue/notificaciones-count"),
            eq(5L)
        );
    }
}

