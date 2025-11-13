package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Notificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) para la entidad {@link Notificacion}.
 * 
 * <p>Este DTO se utiliza para transferir datos de notificaciones entre el cliente
 * y el servidor.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-09
 * @see Notificacion
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionDTO {

    private Long id;
    private Long usuarioId;
    private String titulo;
    private String mensaje;
    private Notificacion.Tipo tipo;
    private Boolean leida;
    private LocalDateTime fechaCreacion;
    private String entidadTipo;
    private Long entidadId;

    /**
     * Convierte una entidad Notificacion a su DTO correspondiente.
     */
    public static NotificacionDTO fromEntity(Notificacion notificacion) {
        return NotificacionDTO.builder()
                .id(notificacion.getId())
                .usuarioId(notificacion.getUsuario().getId())
                .titulo(notificacion.getTitulo())
                .mensaje(notificacion.getMensaje())
                .tipo(notificacion.getTipo())
                .leida(notificacion.getLeida())
                .fechaCreacion(notificacion.getFechaCreacion())
                .entidadTipo(notificacion.getEntidadTipo())
                .entidadId(notificacion.getEntidadId())
                .build();
    }
}

