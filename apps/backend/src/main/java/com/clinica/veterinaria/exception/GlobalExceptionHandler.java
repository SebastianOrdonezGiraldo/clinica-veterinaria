package com.clinica.veterinaria.exception;

import com.clinica.veterinaria.dto.ErrorResponseDTO;
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
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        log.warn("ResourceNotFoundException: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .mensaje(ex.getMessage())
            .status(HttpStatus.NOT_FOUND.value())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .recurso(ex.getResourceName())
            .campo(ex.getFieldName())
            .valor(ex.getFieldValue())
            .build();
        
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
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        log.warn("DuplicateResourceException: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .mensaje(ex.getMessage())
            .status(HttpStatus.CONFLICT.value())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .recurso(ex.getResourceName())
            .campo(ex.getFieldName())
            .valor(ex.getFieldValue())
            .build();
        
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
    public ResponseEntity<ErrorResponseDTO> handleInvalidDataException(
            InvalidDataException ex, WebRequest request) {
        log.warn("InvalidDataException: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .mensaje(ex.getMessage())
            .status(HttpStatus.BAD_REQUEST.value())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .campo(ex.getFieldName())
            .valor(ex.getRejectedValue())
            .detalle(ex.getReason())
            .build();
        
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
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(
            BusinessException ex, WebRequest request) {
        log.warn("BusinessException: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .mensaje(ex.getMessage())
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
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
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        log.error("RuntimeException: {} - Considere usar una excepción personalizada", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .mensaje(ex.getMessage())
            .status(HttpStatus.BAD_REQUEST.value())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
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
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Validation errors: {} errors found", ex.getBindingResult().getErrorCount());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .mensaje("Errores de validación")
            .status(HttpStatus.BAD_REQUEST.value())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .errores(errors)
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones de autenticación (credenciales incorrectas)
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(Exception ex) {
        log.error("Authentication error: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .mensaje("Credenciales inválidas")
            .status(HttpStatus.UNAUTHORIZED.value())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja excepciones de acceso denegado (permisos insuficientes)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage());
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .mensaje("Acceso denegado - No tiene permisos para realizar esta acción")
            .status(HttpStatus.FORBIDDEN.value())
            .timestamp(LocalDateTime.now())
            .detalle(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja errores de conversión de tipos (ej: enum inválido en RequestParam)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("Type mismatch error: {}", ex.getMessage());
        
        String mensaje = "Valor inválido para el parámetro '" + ex.getName() + "': " + ex.getValue();
        String detalle = null;
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            detalle = "Valores válidos: " + java.util.Arrays.toString(ex.getRequiredType().getEnumConstants());
        }
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .mensaje(mensaje)
            .status(HttpStatus.BAD_REQUEST.value())
            .timestamp(LocalDateTime.now())
            .campo(ex.getName())
            .valor(ex.getValue())
            .detalle(detalle)
            .build();
        
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
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
            .mensaje("Error interno del servidor")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .timestamp(LocalDateTime.now())
            .path(request.getDescription(false).replace("uri=", ""))
            .detalle(ex.getMessage())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

