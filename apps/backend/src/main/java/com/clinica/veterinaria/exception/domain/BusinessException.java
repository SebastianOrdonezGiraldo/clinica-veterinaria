package com.clinica.veterinaria.exception.domain;

/**
 * Excepción base para errores relacionados con reglas de negocio.
 * 
 * <p>Esta es una excepción genérica para errores de lógica de negocio que no
 * encajan en las categorías más específicas (ResourceNotFound, DuplicateResource, InvalidData).
 * Puede usarse directamente o como clase base para excepciones de negocio más específicas.</p>
 * 
 * <p><strong>Casos de uso:</strong></p>
 * <ul>
 *   <li>Operaciones no permitidas por el estado actual (ej: cancelar cita completada)</li>
 *   <li>Violaciones de reglas complejas del dominio</li>
 *   <li>Operaciones que requieren condiciones previas no cumplidas</li>
 *   <li>Conflictos de negocio (ej: cita en horario ocupado)</li>
 * </ul>
 * 
 * <p>El GlobalExceptionHandler convierte esta excepción en una respuesta HTTP 422 (Unprocessable Entity).</p>
 * 
 * <p><strong>Ejemplo de uso directo:</strong></p>
 * <pre>
 * if (cita.getEstado() == EstadoCita.COMPLETADA) {
 *     throw new BusinessException("No se puede cancelar una cita ya completada");
 * }
 * </pre>
 * 
 * <p><strong>Ejemplo de extensión:</strong></p>
 * <pre>
 * public class CitaConflictException extends BusinessException {
 *     public CitaConflictException(LocalDateTime fecha) {
 *         super("Ya existe una cita programada para: " + fecha);
 *     }
 * }
 * </pre>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-13
 * @see GlobalExceptionHandler
 */
public class BusinessException extends RuntimeException {
    
    /**
     * Constructor con mensaje de error.
     * 
     * @param message Mensaje descriptivo del error de negocio
     */
    public BusinessException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa.
     * 
     * @param message Mensaje descriptivo del error de negocio
     * @param cause Causa raíz del error
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

