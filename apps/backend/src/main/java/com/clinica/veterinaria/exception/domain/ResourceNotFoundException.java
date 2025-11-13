package com.clinica.veterinaria.exception.domain;

/**
 * Excepción lanzada cuando un recurso solicitado no se encuentra en el sistema.
 * 
 * <p>Esta excepción debe usarse en lugar de RuntimeException genérico cuando
 * una entidad buscada por ID u otro identificador único no existe en la base de datos.</p>
 * 
 * <p><strong>Casos de uso:</strong></p>
 * <ul>
 *   <li>Búsqueda de paciente por ID que no existe</li>
 *   <li>Búsqueda de propietario por ID que no existe</li>
 *   <li>Búsqueda de cita por ID que no existe</li>
 * </ul>
 * 
 * <p>El GlobalExceptionHandler convierte esta excepción en una respuesta HTTP 404.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-13
 * @see GlobalExceptionHandler
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;
    
    /**
     * Constructor con mensaje personalizado.
     * 
     * @param message Mensaje descriptivo del error
     */
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceName = null;
        this.fieldName = null;
        this.fieldValue = null;
    }
    
    /**
     * Constructor con detalles estructurados del recurso no encontrado.
     * 
     * <p>Genera automáticamente un mensaje descriptivo en formato:
     * "{resourceName} no encontrado con {fieldName}: {fieldValue}"</p>
     * 
     * <p><strong>Ejemplo:</strong></p>
     * <pre>
     * throw new ResourceNotFoundException("Paciente", "id", 123);
     * // Mensaje: "Paciente no encontrado con id: 123"
     * </pre>
     * 
     * @param resourceName Nombre del recurso (Paciente, Propietario, etc.)
     * @param fieldName Nombre del campo de búsqueda (id, email, etc.)
     * @param fieldValue Valor buscado
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s no encontrado con %s: %s", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
    
    /**
     * Obtiene el nombre del recurso no encontrado.
     * 
     * @return Nombre del recurso (puede ser null si se usó constructor simple)
     */
    public String getResourceName() {
        return resourceName;
    }
    
    /**
     * Obtiene el nombre del campo de búsqueda.
     * 
     * @return Nombre del campo (puede ser null si se usó constructor simple)
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Obtiene el valor buscado.
     * 
     * @return Valor del campo (puede ser null si se usó constructor simple)
     */
    public Object getFieldValue() {
        return fieldValue;
    }
}

