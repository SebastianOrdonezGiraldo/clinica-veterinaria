/**
 * Paquete de Excepciones de Dominio
 * 
 * <p>Este paquete contiene excepciones personalizadas para representar errores
 * específicos del dominio de la clínica veterinaria. Reemplaza el uso de excepciones
 * genéricas como RuntimeException con excepciones más específicas y descriptivas.</p>
 * 
 * <h3>Jerarquía de Excepciones</h3>
 * 
 * <pre>
 * RuntimeException
 *   ├── ResourceNotFoundException      (HTTP 404)
 *   ├── DuplicateResourceException     (HTTP 409)
 *   ├── InvalidDataException           (HTTP 400)
 *   └── BusinessException              (HTTP 422)
 * </pre>
 * 
 * <h3>Excepciones Disponibles</h3>
 * 
 * <table border="1">
 *   <tr>
 *     <th>Excepción</th>
 *     <th>Uso</th>
 *     <th>HTTP Status</th>
 *   </tr>
 *   <tr>
 *     <td><b>ResourceNotFoundException</b></td>
 *     <td>Recurso no encontrado por ID u otro identificador</td>
 *     <td>404 Not Found</td>
 *   </tr>
 *   <tr>
 *     <td><b>DuplicateResourceException</b></td>
 *     <td>Violación de restricción de unicidad</td>
 *     <td>409 Conflict</td>
 *   </tr>
 *   <tr>
 *     <td><b>InvalidDataException</b></td>
 *     <td>Datos que violan reglas de negocio</td>
 *     <td>400 Bad Request</td>
 *   </tr>
 *   <tr>
 *     <td><b>BusinessException</b></td>
 *     <td>Errores de lógica de negocio genéricos</td>
 *     <td>422 Unprocessable Entity</td>
 *   </tr>
 * </table>
 * 
 * <h3>Ejemplos de Uso</h3>
 * 
 * <pre>{@code
 * // Recurso no encontrado
 * throw new ResourceNotFoundException("Paciente", "id", 123);
 * // Resultado: "Paciente no encontrado con id: 123"
 * 
 * // Recurso duplicado
 * throw new DuplicateResourceException("Usuario", "email", "admin@clinica.com");
 * // Resultado: "Usuario ya existe con email: admin@clinica.com"
 * 
 * // Dato inválido
 * throw new InvalidDataException("edadMeses", 500, "Edad no puede exceder 300 meses");
 * // Resultado: "Valor inválido para edadMeses: 500. Razón: Edad no puede exceder 300 meses"
 * 
 * // Error de negocio
 * throw new BusinessException("No se puede cancelar una cita ya completada");
 * }</pre>
 * 
 * <h3>Ventajas sobre RuntimeException genérico</h3>
 * 
 * <ul>
 *   <li><b>Mayor claridad:</b> El tipo de excepción comunica el tipo de error</li>
 *   <li><b>Manejo específico:</b> GlobalExceptionHandler puede responder de forma apropiada</li>
 *   <li><b>Testing más fácil:</b> Se pueden capturar y verificar excepciones específicas</li>
 *   <li><b>Mensajes consistentes:</b> Formato estandarizado de mensajes de error</li>
 *   <li><b>Metadata estructurada:</b> Campos adicionales (resourceName, fieldName, etc.)</li>
 *   <li><b>Códigos HTTP correctos:</b> Mapeo automático a códigos de estado apropiados</li>
 * </ul>
 * 
 * <h3>Integración con GlobalExceptionHandler</h3>
 * 
 * <p>El {@link com.clinica.veterinaria.exception.GlobalExceptionHandler} captura
 * estas excepciones y las convierte en respuestas HTTP apropiadas con formato JSON:</p>
 * 
 * <pre>
 * {
 *   "mensaje": "Paciente no encontrado con id: 123",
 *   "status": 404,
 *   "timestamp": "2025-11-13T10:30:00",
 *   "path": "/api/pacientes/123"
 * }
 * </pre>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-13
 * @see com.clinica.veterinaria.exception.GlobalExceptionHandler
 */
package com.clinica.veterinaria.exception.domain;

