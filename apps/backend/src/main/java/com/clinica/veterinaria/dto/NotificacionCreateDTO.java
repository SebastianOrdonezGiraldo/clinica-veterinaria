package com.clinica.veterinaria.dto;

import com.clinica.veterinaria.entity.Notificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) para crear una nueva notificación.
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
public class NotificacionCreateDTO {

    @NotNull(message = "El ID del usuario es requerido")
    private Long usuarioId;

    @NotBlank(message = "El título es requerido")
    private String titulo;

    @NotBlank(message = "El mensaje es requerido")
    private String mensaje;

    @NotNull(message = "El tipo es requerido")
    private Notificacion.Tipo tipo;

    private String entidadTipo;
    private Long entidadId;
}

