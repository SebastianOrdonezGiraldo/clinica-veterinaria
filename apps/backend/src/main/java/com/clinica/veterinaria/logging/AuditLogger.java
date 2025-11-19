package com.clinica.veterinaria.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio de auditoría para registrar eventos importantes del negocio.
 * 
 * <p>Este servicio proporciona métodos especializados para registrar eventos de auditoría
 * que son críticos para la trazabilidad, seguridad y cumplimiento normativo. Utiliza un
 * logger dedicado separado del logging general de la aplicación.</p>
 * 
 * <p><strong>Eventos auditados:</strong></p>
 * <ul>
 *   <li>Operaciones CRUD en entidades críticas (CREATE, UPDATE, DELETE)</li>
 *   <li>Accesos a información sensible</li>
 *   <li>Autenticación (login exitoso/fallido, logout)</li>
 *   <li>Cambios de permisos y roles</li>
 *   <li>Cambios de estado importantes</li>
 *   <li>Exportaciones de datos</li>
 *   <li>Eventos de seguridad</li>
 * </ul>
 * 
 * <p><strong>Características:</strong></p>
 * <ul>
 *   <li>Uso de MDC (Mapped Diagnostic Context) para contexto adicional</li>
 *   <li>Sanitización automática de datos sensibles (passwords, tokens)</li>
 *   <li>Inclusión automática de usuario actual y correlation ID</li>
 *   <li>Logger separado para facilitar filtrado y análisis</li>
 * </ul>
 * 
 * <p><strong>Logger:</strong> com.clinica.veterinaria.audit</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see CorrelationIdFilter
 */
@Service
public class AuditLogger implements IAuditLogger {
    
    private static final Logger logger = LoggerFactory.getLogger("com.clinica.veterinaria.audit");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Constantes para claves MDC
    private static final String MDC_ACTION = "action";
    private static final String MDC_ENTITY = "entity";
    private static final String MDC_ENTITY_ID = "entityId";
    private static final String MDC_USERNAME = "username";
    private static final String MDC_CLIENT_IP = "clientIp";
    private static final String MDC_TARGET_USER = "targetUser";
    private static final String MDC_DATA_TYPE = "dataType";
    private static final String MDC_CORRELATION_ID = "correlationId";
    
    /**
     * Registra un evento de creación
     */
    public void logCreate(String entity, Object entityId, Object data) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "CREATE");
            MDC.put(MDC_ENTITY, entity);
            MDC.put(MDC_ENTITY_ID, String.valueOf(entityId));
            
            logger.info("✓ CREATED {} with ID {} | User: {} | Data: {}", 
                    entity, 
                    entityId, 
                    getCurrentUsername(),
                    sanitizeData(data));
        } finally {
            MDC.remove(MDC_ACTION);
            MDC.remove(MDC_ENTITY);
            MDC.remove(MDC_ENTITY_ID);
        }
    }
    
    /**
     * Registra un evento de actualización
     */
    public void logUpdate(String entity, Object entityId, Object oldData, Object newData) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "UPDATE");
            MDC.put(MDC_ENTITY, entity);
            MDC.put(MDC_ENTITY_ID, String.valueOf(entityId));
            
            logger.info("✎ UPDATED {} with ID {} | User: {} | Old: {} | New: {}", 
                    entity, 
                    entityId, 
                    getCurrentUsername(),
                    sanitizeData(oldData),
                    sanitizeData(newData));
        } finally {
            MDC.remove(MDC_ACTION);
            MDC.remove(MDC_ENTITY);
            MDC.remove(MDC_ENTITY_ID);
        }
    }
    
    /**
     * Registra un evento de eliminación
     */
    public void logDelete(String entity, Object entityId) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "DELETE");
            MDC.put(MDC_ENTITY, entity);
            MDC.put(MDC_ENTITY_ID, String.valueOf(entityId));
            
            logger.warn("⚠ DELETED {} with ID {} | User: {} | Timestamp: {}", 
                    entity, 
                    entityId, 
                    getCurrentUsername(),
                    LocalDateTime.now().format(formatter));
        } finally {
            MDC.remove(MDC_ACTION);
            MDC.remove(MDC_ENTITY);
            MDC.remove(MDC_ENTITY_ID);
        }
    }
    
    /**
     * Registra un evento de acceso/lectura a información sensible
     */
    public void logAccess(String entity, Object entityId, String reason) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "ACCESS");
            MDC.put(MDC_ENTITY, entity);
            MDC.put(MDC_ENTITY_ID, String.valueOf(entityId));
            
            logger.info("👁 ACCESSED {} with ID {} | User: {} | Reason: {}", 
                    entity, 
                    entityId, 
                    getCurrentUsername(),
                    reason);
        } finally {
            MDC.remove(MDC_ACTION);
            MDC.remove(MDC_ENTITY);
            MDC.remove(MDC_ENTITY_ID);
        }
    }
    
    /**
     * Registra un evento de autenticación exitosa
     */
    public void logLoginSuccess(String username, String ipAddress) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "LOGIN_SUCCESS");
            MDC.put(MDC_USERNAME, username);
            MDC.put(MDC_CLIENT_IP, ipAddress);
            
            logger.info("🔓 LOGIN SUCCESS | User: {} | IP: {} | Timestamp: {}", 
                    username, 
                    ipAddress,
                    LocalDateTime.now().format(formatter));
        } finally {
            MDC.remove(MDC_ACTION);
            MDC.remove(MDC_USERNAME);
            MDC.remove(MDC_CLIENT_IP);
        }
    }
    
    /**
     * Registra un intento de autenticación fallido
     */
    public void logLoginFailure(String username, String ipAddress, String reason) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "LOGIN_FAILURE");
            MDC.put(MDC_USERNAME, username);
            MDC.put(MDC_CLIENT_IP, ipAddress);
            
            logger.warn("🔒 LOGIN FAILURE | User: {} | IP: {} | Reason: {} | Timestamp: {}", 
                    username, 
                    ipAddress,
                    reason,
                    LocalDateTime.now().format(formatter));
        } finally {
            MDC.remove(MDC_ACTION);
            MDC.remove(MDC_USERNAME);
            MDC.remove(MDC_CLIENT_IP);
        }
    }
    
    /**
     * Registra un evento de logout
     */
    public void logLogout(String username) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "LOGOUT");
            MDC.put(MDC_USERNAME, username);
            
            logger.info("🚪 LOGOUT | User: {} | Timestamp: {}", 
                    username,
                    LocalDateTime.now().format(formatter));
        } finally {
            MDC.remove(MDC_ACTION);
            MDC.remove(MDC_USERNAME);
        }
    }
    
    /**
     * Registra un cambio de permisos o roles
     */
    public void logPermissionChange(String targetUser, String action, String details) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "PERMISSION_CHANGE");
            MDC.put(MDC_TARGET_USER, targetUser);
            
            logger.warn("⚡ PERMISSION CHANGE | Target: {} | Action: {} | Details: {} | By: {}", 
                    targetUser,
                    action,
                    details,
                    getCurrentUsername());
        } finally {
            MDC.remove(MDC_ACTION);
            MDC.remove(MDC_TARGET_USER);
        }
    }
    
    /**
     * Registra una exportación de datos
     */
    public void logDataExport(String dataType, int recordCount, String format) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "DATA_EXPORT");
            MDC.put(MDC_DATA_TYPE, dataType);
            
            logger.info("📊 DATA EXPORT | Type: {} | Records: {} | Format: {} | User: {}", 
                    dataType,
                    recordCount,
                    format,
                    getCurrentUsername());
        } finally {
            MDC.remove(MDC_ACTION);
            MDC.remove(MDC_DATA_TYPE);
        }
    }
    
    /**
     * Registra un cambio de estado importante
     */
    public void logStatusChange(String entity, Object entityId, String oldStatus, String newStatus) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "STATUS_CHANGE");
            MDC.put(MDC_ENTITY, entity);
            MDC.put(MDC_ENTITY_ID, String.valueOf(entityId));
            
            logger.info("🔄 STATUS CHANGE | {} ID: {} | From: {} → To: {} | User: {}", 
                    entity,
                    entityId,
                    oldStatus,
                    newStatus,
                    getCurrentUsername());
        } finally {
            MDC.remove(MDC_ACTION);
            MDC.remove(MDC_ENTITY);
            MDC.remove(MDC_ENTITY_ID);
        }
    }
    
    /**
     * Registra un evento personalizado
     */
    public void logCustomEvent(String eventType, String message, Object... params) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, eventType);
            
            logger.info("📝 {} | {} | User: {}", 
                    eventType,
                    String.format(message, params),
                    getCurrentUsername());
        } finally {
            MDC.remove(MDC_ACTION);
        }
    }
    
    /**
     * Registra un error de seguridad
     */
    public void logSecurityEvent(String eventType, String details) {
        if (!logger.isErrorEnabled()) {
            return;
        }
        try {
            MDC.put(MDC_ACTION, "SECURITY_EVENT");
            
            logger.error("🚨 SECURITY EVENT | Type: {} | Details: {} | User: {} | Correlation-ID: {}", 
                    eventType,
                    details,
                    getCurrentUsername(),
                    MDC.get(MDC_CORRELATION_ID));
        } finally {
            MDC.remove(MDC_ACTION);
        }
    }
    
    /**
     * Obtiene el nombre del usuario actual
     */
    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignorar errores al obtener el usuario
        }
        return "system";
    }
    
    /**
     * Sanitiza datos sensibles antes de registrarlos
     */
    private String sanitizeData(Object data) {
        if (data == null) {
            return "null";
        }
        
        String dataStr = data.toString();
        
        // Ofuscar información sensible
        dataStr = dataStr.replaceAll("(?i)password[\"']?\\s*[:=]\\s*[\"']?[^,}\"']+", "password=***");
        dataStr = dataStr.replaceAll("(?i)token[\"']?\\s*[:=]\\s*[\"']?[^,}\"']+", "token=***");
        dataStr = dataStr.replaceAll("(?i)secret[\"']?\\s*[:=]\\s*[\"']?[^,}\"']+", "secret=***");
        
        // Limitar longitud para evitar logs enormes
        if (dataStr.length() > 500) {
            dataStr = dataStr.substring(0, 500) + "... (truncated)";
        }
        
        return dataStr;
    }
}

