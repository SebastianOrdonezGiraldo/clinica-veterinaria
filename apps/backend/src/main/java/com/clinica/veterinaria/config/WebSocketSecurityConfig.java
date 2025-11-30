package com.clinica.veterinaria.config;

import com.clinica.veterinaria.security.JwtUtil;
import com.clinica.veterinaria.security.CustomUserDetailsService;
import com.clinica.veterinaria.security.ClienteUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de seguridad para WebSocket.
 * 
 * <p>Este interceptor valida el token JWT en las conexiones WebSocket
 * y establece la autenticación del usuario.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-30
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final ClienteUserDetailsService clienteUserDetailsService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authToken = accessor.getFirstNativeHeader("Authorization");
                    
                    if (authToken != null && authToken.startsWith("Bearer ")) {
                        try {
                            String token = authToken.substring(7);
                            String username = jwtUtil.extractUsername(token);
                            
                            if (username != null) {
                                UserDetails userDetails = null;
                                
                                // Intentar determinar el tipo de usuario
                                try {
                                    String rol = jwtUtil.extractRole(token);
                                    if ("CLIENTE".equals(rol)) {
                                        userDetails = clienteUserDetailsService.loadUserByUsername(username);
                                    } else {
                                        userDetails = userDetailsService.loadUserByUsername(username);
                                    }
                                } catch (Exception e) {
                                    // Intentar como usuario del sistema primero
                                    try {
                                        userDetails = userDetailsService.loadUserByUsername(username);
                                    } catch (Exception ex) {
                                        userDetails = clienteUserDetailsService.loadUserByUsername(username);
                                    }
                                }
                                
                                if (userDetails != null && Boolean.TRUE.equals(jwtUtil.validateToken(token, userDetails))) {
                                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                    );
                                    
                                    accessor.setUser(authentication);
                                    log.info("Usuario autenticado en WebSocket: {}", username);
                                } else {
                                    log.warn("Token JWT inválido en conexión WebSocket");
                                }
                            }
                        } catch (Exception e) {
                            log.error("Error al autenticar conexión WebSocket: {}", e.getMessage(), e);
                        }
                    } else {
                        log.warn("Conexión WebSocket sin token de autorización");
                    }
                }
                
                return message;
            }
        });
    }
}

