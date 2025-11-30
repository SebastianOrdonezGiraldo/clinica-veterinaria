package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Notificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para notificaciones enviadas a través de WebSocket.
 * 
 * <p>Este DTO contiene la información necesaria para enviar
 * notificaciones en tiempo real a los clientes conectados.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionWebSocketDTO {
    
    private Long id;
    private String titulo;
    private String mensaje;
    private Notificacion.Tipo tipo;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private String entidadTipo;
    private Long entidadId;
    private Long usuarioId;

    /**
     * Convierte una entidad Notificacion a DTO para WebSocket.
     */
    public static NotificacionWebSocketDTO fromEntity(Notificacion notificacion) {
        return NotificacionWebSocketDTO.builder()
                .id(notificacion.getId())
                .titulo(notificacion.getTitulo())
                .mensaje(notificacion.getMensaje())
                .tipo(notificacion.getTipo())
                .leida(notificacion.getLeida())
                .fechaCreacion(notificacion.getFechaCreacion())
                .entidadTipo(notificacion.getEntidadTipo())
                .entidadId(notificacion.getEntidadId())
                .usuarioId(notificacion.getUsuario().getId())
                .build();
    }
}

