package com.clinica.veterinaria.exception.domain;

/**
 * Excepción lanzada cuando los datos proporcionados no cumplen con las reglas de negocio.
 * 
 * <p>Esta excepción debe usarse para validaciones de negocio que van más allá de las
 * validaciones básicas de Jakarta Validation (@NotNull, @Size, etc.). Representa datos
 * que son técnicamente válidos pero no cumplen con las reglas del dominio.</p>
 * 
 * <p><strong>Casos de uso:</strong></p>
 * <ul>
 *   <li>Edad de paciente fuera de rango razonable (ej: 500 meses)</li>
 *   <li>Peso negativo o excesivo</li>
 *   <li>Fecha de cita en el pasado</li>
 *   <li>Precio negativo en prescripción</li>
 *   <li>Estado inválido para transición (ej: CANCELADA → COMPLETADA)</li>
 * </ul>
 * 
 * <p>El GlobalExceptionHandler convierte esta excepción en una respuesta HTTP 400 (Bad Request).</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-13
 * @see GlobalExceptionHandler
 */
public class InvalidDataException extends RuntimeException {
    
    private final String fieldName;
    private final Object rejectedValue;
    private final String reason;
    
    /**
     * Constructor con mensaje personalizado simple.
     * 
     * @param message Mensaje descriptivo del error
     */
    public InvalidDataException(String message) {
        super(message);
        this.fieldName = null;
        this.rejectedValue = null;
        this.reason = null;
    }
    
    /**
     * Constructor con detalles estructurados del dato inválido.
     * 
     * <p>Genera automáticamente un mensaje descriptivo en formato:
     * "Valor inválido para {fieldName}: {rejectedValue}. Razón: {reason}"</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * throw new InvalidDataException("edadMeses", 500, "La edad no puede exceder 300 meses (25 años)");
     * // Mensaje: "Valor inválido para edadMeses: 500. Razón: La edad no puede exceder 300 meses (25 años)"
     * </pre>
     * 
     * @param fieldName Nombre del campo inválido
     * @param rejectedValue Valor rechazado
     * @param reason Razón por la cual el valor es inválido
     */
    public InvalidDataException(String fieldName, Object rejectedValue, String reason) {
        super(String.format("Valor inválido para %s: %s. Razón: %s", fieldName, rejectedValue, reason));
        this.fieldName = fieldName;
        this.rejectedValue = rejectedValue;
        this.reason = reason;
    }
    
    /**
     * Obtiene el nombre del campo inválido.
     * 
     * @return Nombre del campo (puede ser null si se usó constructor simple)
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Obtiene el valor rechazado.
     * 
     * @return Valor rechazado (puede ser null si se usó constructor simple)
     */
    public Object getRejectedValue() {
        return rejectedValue;
    }
    
    /**
     * Obtiene la razón del rechazo.
     * 
     * @return Razón del rechazo (puede ser null si se usó constructor simple)
     */
    public String getReason() {
        return reason;
    }
}

