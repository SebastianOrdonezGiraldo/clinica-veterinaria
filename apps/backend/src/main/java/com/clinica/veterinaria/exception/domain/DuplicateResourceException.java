package com.clinica.veterinaria.exception.domain;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe.
 * 
 * <p>Esta excepción debe usarse cuando se violan restricciones de unicidad,
 * como intentar crear un usuario con un email que ya está registrado, o
 * un paciente con un microchip duplicado.</p>
 * 
 * <p><strong>Casos de uso:</strong></p>
 * <ul>
 *   <li>Email de usuario duplicado</li>
 *   <li>Microchip de paciente duplicado</li>
 *   <li>Documento de propietario duplicado</li>
 *   <li>Cualquier violación de constraint UNIQUE</li>
 * </ul>
 * 
 * <p>El GlobalExceptionHandler convierte esta excepción en una respuesta HTTP 409 (Conflict).</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-13
 * @see GlobalExceptionHandler
 */
public class DuplicateResourceException extends RuntimeException {
    
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje descriptivo del error
     */
    public DuplicateResourceException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }
    
    /**
     * Constructor con detalles estructurados del recurso duplicado.
     * 
     * <p>Genera automáticamente un mensaje descriptivo en formato:
     * "{resourceName} ya existe con {fieldName}: {fieldValue}"</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * throw new DuplicateResourceException("Usuario", "email", "admin@clinica.com");
     * // Mensaje: "Usuario ya existe con email: admin@clinica.com"
     * </pre>
     * 
     * @param resourceName Nombre del recurso (Usuario, Paciente, etc.)
     * @param fieldName Nombre del campo duplicado (email, microchip, etc.)
     * @param fieldValue Valor duplicado
     */
    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s ya existe con %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    /**
     * Obtiene el nombre del recurso duplicado.
     * 
     * @return Nombre del recurso (puede ser null si se usó constructor simple)
     */
    public String getResourceName() {
        return resourceName;
    }
    
    /**
     * Obtiene el nombre del campo duplicado.
     * 
     * @return Nombre del campo (puede ser null si se usó constructor simple)
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Obtiene el valor duplicado.
     * 
     * @return Valor del campo (puede ser null si se usó constructor simple)
     */
    public Object getFieldValue() {
        return fieldValue;
    }
}

