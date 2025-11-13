package com.clinica.veterinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para respuestas de error estandarizadas.
 * 
 * <p>Proporciona una estructura consistente para todas las respuestas de error de la API,
 * facilitando el manejo de errores en el cliente y mejorando la experiencia del desarrollador.</p>
 * 
 * <p><strong>Estructura de la respuesta:</strong></p>
 * <ul>
 *   <li><b>mensaje:</b> Descripción legible del error</li>
 *   <li><b>status:</b> Código HTTP del error</li>
 *   <li><b>timestamp:</b> Momento exacto en que ocurrió el error</li>
 *   <li><b>path:</b> Ruta del endpoint donde ocurrió el error</li>
 *   <li><b>errores:</b> (Opcional) Mapa de errores de validación por campo</li>
 *   <li><b>detalle:</b> (Opcional) Información adicional sobre el error</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-13
 * @see com.clinica.veterinaria.exception.GlobalExceptionHandler
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    
    /**
     * Mensaje descriptivo del error
     */
    private String mensaje;
    
    /**
     * Código de estado HTTP
     */
    private Integer status;
    
    /**
     * Marca de tiempo cuando ocurrió el error
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Ruta del endpoint donde ocurrió el error
     */
    private String path;
    
    /**
     * Mapa de errores de validación (campo -> mensaje)
     * Usado principalmente para errores de validación de formularios
     */
    private Map<String, String> errores;
    
    /**
     * Información adicional sobre el error
     */
    private String detalle;
    
    /**
     * Información del recurso relacionado con el error (si aplica)
     */
    private String recurso;
    
    /**
     * Campo relacionado con el error (si aplica)
     */
    private String campo;
    
    /**
     * Valor que causó el error (si aplica)
     */
    private Object valor;
}

