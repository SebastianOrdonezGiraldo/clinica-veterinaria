import { useEffect, useRef, useState, useCallback } from 'react';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from '@core/auth/AuthContext';
import { useLogger } from './useLogger';

interface UseWebSocketOptions {
  onNotification?: (notification: any) => void;
  onNotificationCount?: (count: number) => void;
  autoConnect?: boolean;
}

/**
 * Hook para manejar conexión WebSocket con el servidor.
 * 
 * Proporciona:
 * - Conexión automática cuando el usuario está autenticado
 * - Suscripción a notificaciones en tiempo real
 * - Actualización automática del contador de notificaciones
 * - Reconexión automática en caso de desconexión
 */
export function useWebSocket(options: UseWebSocketOptions = {}) {
  const { user, token } = useAuth();
  const logger = useLogger('useWebSocket');
  const clientRef = useRef<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [reconnectAttempts, setReconnectAttempts] = useState(0);
  const maxReconnectAttempts = 5;

  const {
    onNotification,
    onNotificationCount,
    autoConnect = true,
  } = options;

  const connect = useCallback(() => {
    if (!user || !token || !autoConnect) {
      return;
    }

    if (clientRef.current?.active) {
      logger.debug('WebSocket ya está conectado');
      return;
    }

    logger.info('Conectando WebSocket...');

    // Obtener URL base del servidor
    const getWebSocketUrl = () => {
      const apiUrl = import.meta.env.VITE_API_URL;
      if (apiUrl) {
        // Si hay VITE_API_URL, usar esa base y quitar /api si existe
        return apiUrl.replace('/api', '') + '/ws';
      }
      // Fallback: usar la misma URL del navegador pero cambiar el puerto
      const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:';
      const hostname = window.location.hostname;
      return `${protocol}//${hostname}:8080/ws`;
    };

    // Crear cliente STOMP sobre SockJS
    const client = new Client({
      webSocketFactory: () => {
        const socket = new SockJS(getWebSocketUrl());
        return socket as any;
      },
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: (frame) => {
        logger.info('WebSocket conectado', { frame });
        setIsConnected(true);
        setReconnectAttempts(0);

        // Suscribirse a notificaciones del usuario
        if (user?.id) {
          const notificationDestination = `/user/${user.id}/queue/notificaciones`;
          const countDestination = `/user/${user.id}/queue/notificaciones-count`;

          // Suscripción a notificaciones
          client.subscribe(notificationDestination, (message: IMessage) => {
            try {
              const notification = JSON.parse(message.body);
              logger.debug('Notificación recibida', { notification });
              onNotification?.(notification);
            } catch (error) {
              logger.error('Error al parsear notificación', error as Error);
            }
          });

          // Suscripción a contador de notificaciones
          client.subscribe(countDestination, (message: IMessage) => {
            try {
              const count = parseInt(message.body, 10);
              logger.debug('Contador de notificaciones recibido', { count });
              onNotificationCount?.(count);
            } catch (error) {
              logger.error('Error al parsear contador', error as Error);
            }
          });
        }
      },
      onDisconnect: () => {
        logger.warn('WebSocket desconectado');
        setIsConnected(false);
      },
      onStompError: (frame) => {
        logger.error('Error STOMP', new Error(frame.headers['message'] || 'Unknown error'), {
          frame,
        });
        setIsConnected(false);
      },
      onWebSocketError: (event) => {
        logger.error('Error WebSocket', event as any);
        setIsConnected(false);
        
        // Intentar reconectar
        if (reconnectAttempts < maxReconnectAttempts) {
          setReconnectAttempts((prev) => prev + 1);
          setTimeout(() => {
            logger.info(`Intentando reconectar (${reconnectAttempts + 1}/${maxReconnectAttempts})...`);
            connect();
          }, 5000 * (reconnectAttempts + 1));
        } else {
          logger.error('Máximo de intentos de reconexión alcanzado');
        }
      },
    });

    client.activate();
    clientRef.current = client;
  }, [user, token, autoConnect, onNotification, onNotificationCount, reconnectAttempts, logger]);

  const disconnect = useCallback(() => {
    if (clientRef.current) {
      logger.info('Desconectando WebSocket...');
      clientRef.current.deactivate();
      clientRef.current = null;
      setIsConnected(false);
    }
  }, [logger]);

  useEffect(() => {
    if (autoConnect && user && token) {
      connect();
    }

    return () => {
      disconnect();
    };
  }, [autoConnect, user, token, connect, disconnect]);

  return {
    isConnected,
    connect,
    disconnect,
  };
}

