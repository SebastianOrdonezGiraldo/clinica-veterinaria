package com.clinica.veterinaria.exception;

import com.clinica.veterinaria.exception.domain.BusinessException;
import com.clinica.veterinaria.exception.domain.DuplicateResourceException;
import com.clinica.veterinaria.exception.domain.InvalidDataException;
import com.clinica.veterinaria.exception.domain.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación.
 * 
 * <p>Captura y convierte excepciones en respuestas HTTP apropiadas con formato JSON consistente.
 * Todas las respuestas de error incluyen: mensaje, status, timestamp y path.</p>
 * 
 * <h3>Mapeo de Excepciones a Códigos HTTP</h3>
 * <ul>
 *   <li><b>ResourceNotFoundException</b> → 404 Not Found</li>
 *   <li><b>DuplicateResourceException</b> → 409 Conflict</li>
 *   <li><b>InvalidDataException</b> → 400 Bad Request</li>
 *   <li><b>BusinessException</b> → 422 Unprocessable Entity</li>
 *   <li><b>MethodArgumentNotValidException</b> → 400 Bad Request</li>
 *   <li><b>AuthenticationException</b> → 401 Unauthorized</li>
 *   <li><b>AccessDeniedException</b> → 403 Forbidden</li>
 * </ul>
 * 
 * @author Sebastian Ordoñez
 * @version 2.0.0
 * @since 2025-11-13
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja ResourceNotFoundException (recurso no encontrado).
     * 
     * @param ex Excepción lanzada
     * @param request Información del request
     * @return ResponseEntity con status 404 y detalles del error
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("ResourceNotFoundException: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = buildErrorResponse(
            ex.getMessage(),
            HttpStatus.NOT_FOUND,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Agregar detalles adicionales si están disponibles
        if (ex.getResourceName() != null) {
            errorResponse.put("recurso", ex.getResourceName());
            errorResponse.put("campo", ex.getFieldName());
            errorResponse.put("valor", ex.getFieldValue());
        }
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    /**
     * Maneja DuplicateResourceException (recurso duplicado).
     * 
     * @param ex Excepción lanzada
     * @param request Información del request
     * @return ResponseEntity con status 409 y detalles del error
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        log.warn("DuplicateResourceException: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = buildErrorResponse(
            ex.getMessage(),
            HttpStatus.CONFLICT,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Agregar detalles adicionales si están disponibles
        if (ex.getResourceName() != null) {
            errorResponse.put("recurso", ex.getResourceName());
            errorResponse.put("campo", ex.getFieldName());
            errorResponse.put("valor", ex.getFieldValue());
        }
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
    
    /**
     * Maneja InvalidDataException (datos inválidos).
     * 
     * @param ex Excepción lanzada
     * @param request Información del request
     * @return ResponseEntity con status 400 y detalles del error
     */
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDataException(
            InvalidDataException ex, WebRequest request) {
        log.warn("InvalidDataException: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = buildErrorResponse(
            ex.getMessage(),
            HttpStatus.BAD_REQUEST,
            request.getDescription(false).replace("uri=", "")
        );
        
        // Agregar detalles adicionales si están disponibles
        if (ex.getFieldName() != null) {
            errorResponse.put("campo", ex.getFieldName());
            errorResponse.put("valorRechazado", ex.getRejectedValue());
            errorResponse.put("razon", ex.getReason());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    
    /**
     * Maneja BusinessException (errores de lógica de negocio).
     * 
     * @param ex Excepción lanzada
     * @param request Información del request
     * @return ResponseEntity con status 422 y detalles del error
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.warn("BusinessException: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = buildErrorResponse(
            ex.getMessage(),
            HttpStatus.UNPROCESSABLE_ENTITY,
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }
    
    /**
     * Maneja RuntimeException genérico (fallback para excepciones no específicas).
     * 
     * <p>NOTA: Este handler es un fallback. Se recomienda usar excepciones personalizadas
     * (ResourceNotFoundException, DuplicateResourceException, etc.) en lugar de RuntimeException.</p>
     * 
     * @param ex Excepción lanzada
     * @param request Información del request
     * @return ResponseEntity con status 400 y detalles del error
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        log.error("RuntimeException: {} - Considere usar una excepción personalizada", ex.getMessage());
        
        Map<String, Object> errorResponse = buildErrorResponse(
            ex.getMessage(),
            HttpStatus.BAD_REQUEST,
            request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja errores de validación de Jakarta Validation.
     * 
     * <p>Se activa cuando las validaciones @NotNull, @NotBlank, @Size, etc. fallan.</p>
     * 
     * @param ex Excepción de validación
     * @param request Información del request
     * @return ResponseEntity con status 400 y lista de errores de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation errors: {} errors found", ex.getBindingResult().getErrorCount());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> errorResponse = buildErrorResponse(
            "Errores de validación",
            HttpStatus.BAD_REQUEST,
            request.getDescription(false).replace("uri=", "")
        );
        errorResponse.put("errores", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones de autenticación (credenciales incorrectas)
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(Exception ex) {
        log.error("Authentication error: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("mensaje", "Credenciales inválidas");
        errorResponse.put("status", 401);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja excepciones de acceso denegado (permisos insuficientes)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("mensaje", "Acceso denegado");
        errorResponse.put("status", 403);
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja errores de conversión de tipos (ej: enum inválido en RequestParam)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch error: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = new HashMap<>();
        String mensaje = "Valor inválido para el parámetro '" + ex.getName() + "': " + ex.getValue();
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            mensaje += ". Valores válidos: " + java.util.Arrays.toString(ex.getRequiredType().getEnumConstants());
        }
        errorResponse.put("mensaje", mensaje);
        errorResponse.put("status", 400);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja todas las demás excepciones no capturadas.
     * 
     * <p>Este es el handler de último recurso para excepciones inesperadas.</p>
     * 
     * @param ex Excepción lanzada
     * @param request Información del request
     * @return ResponseEntity con status 500 y detalles del error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = buildErrorResponse(
            "Error interno del servidor",
            HttpStatus.INTERNAL_SERVER_ERROR,
            request.getDescription(false).replace("uri=", "")
        );
        errorResponse.put("detalle", ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * Construye una respuesta de error consistente.
     * 
     * <p>Todas las respuestas de error incluyen:</p>
     * <ul>
     *   <li><b>mensaje:</b> Descripción del error</li>
     *   <li><b>status:</b> Código HTTP</li>
     *   <li><b>timestamp:</b> Momento del error</li>
     *   <li><b>path:</b> Ruta del endpoint que falló</li>
     * </ul>
     * 
     * @param message Mensaje de error
     * @param status Status HTTP
     * @param path Ruta del request
     * @return Map con la estructura de respuesta de error
     */
    private Map<String, Object> buildErrorResponse(String message, HttpStatus status, String path) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("mensaje", message);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("path", path);
        return errorResponse;
    }
}

