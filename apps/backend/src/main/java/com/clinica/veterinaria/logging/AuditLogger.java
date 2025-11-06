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
 * Eventos auditados:
 * - Operaciones CRUD en entidades críticas
 * - Accesos y cambios de permisos
 * - Login/Logout de usuarios
 * - Cambios de estado importantes
 * - Eliminaciones de datos
 * - Exportaciones de información sensible
 */
@Service
public class AuditLogger {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("com.clinica.veterinaria.audit");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Registra un evento de creación
     */
    public void logCreate(String entity, Object entityId, Object data) {
        try {
            MDC.put("action", "CREATE");
            MDC.put("entity", entity);
            MDC.put("entityId", String.valueOf(entityId));
            
            auditLogger.info("✓ CREATED {} with ID {} | User: {} | Data: {}", 
                    entity, 
                    entityId, 
                    getCurrentUsername(),
                    sanitizeData(data));
        } finally {
            MDC.remove("action");
            MDC.remove("entity");
            MDC.remove("entityId");
        }
    }
    
    /**
     * Registra un evento de actualización
     */
    public void logUpdate(String entity, Object entityId, Object oldData, Object newData) {
        try {
            MDC.put("action", "UPDATE");
            MDC.put("entity", entity);
            MDC.put("entityId", String.valueOf(entityId));
            
            auditLogger.info("✎ UPDATED {} with ID {} | User: {} | Old: {} | New: {}", 
                    entity, 
                    entityId, 
                    getCurrentUsername(),
                    sanitizeData(oldData),
                    sanitizeData(newData));
        } finally {
            MDC.remove("action");
            MDC.remove("entity");
            MDC.remove("entityId");
        }
    }
    
    /**
     * Registra un evento de eliminación
     */
    public void logDelete(String entity, Object entityId) {
        try {
            MDC.put("action", "DELETE");
            MDC.put("entity", entity);
            MDC.put("entityId", String.valueOf(entityId));
            
            auditLogger.warn("⚠ DELETED {} with ID {} | User: {} | Timestamp: {}", 
                    entity, 
                    entityId, 
                    getCurrentUsername(),
                    LocalDateTime.now().format(formatter));
        } finally {
            MDC.remove("action");
            MDC.remove("entity");
            MDC.remove("entityId");
        }
    }
    
    /**
     * Registra un evento de acceso/lectura a información sensible
     */
    public void logAccess(String entity, Object entityId, String reason) {
        try {
            MDC.put("action", "ACCESS");
            MDC.put("entity", entity);
            MDC.put("entityId", String.valueOf(entityId));
            
            auditLogger.info("👁 ACCESSED {} with ID {} | User: {} | Reason: {}", 
                    entity, 
                    entityId, 
                    getCurrentUsername(),
                    reason);
        } finally {
            MDC.remove("action");
            MDC.remove("entity");
            MDC.remove("entityId");
        }
    }
    
    /**
     * Registra un evento de autenticación exitosa
     */
    public void logLoginSuccess(String username, String ipAddress) {
        try {
            MDC.put("action", "LOGIN_SUCCESS");
            MDC.put("username", username);
            MDC.put("clientIp", ipAddress);
            
            auditLogger.info("🔓 LOGIN SUCCESS | User: {} | IP: {} | Timestamp: {}", 
                    username, 
                    ipAddress,
                    LocalDateTime.now().format(formatter));
        } finally {
            MDC.remove("action");
            MDC.remove("username");
            MDC.remove("clientIp");
        }
    }
    
    /**
     * Registra un intento de autenticación fallido
     */
    public void logLoginFailure(String username, String ipAddress, String reason) {
        try {
            MDC.put("action", "LOGIN_FAILURE");
            MDC.put("username", username);
            MDC.put("clientIp", ipAddress);
            
            auditLogger.warn("🔒 LOGIN FAILURE | User: {} | IP: {} | Reason: {} | Timestamp: {}", 
                    username, 
                    ipAddress,
                    reason,
                    LocalDateTime.now().format(formatter));
        } finally {
            MDC.remove("action");
            MDC.remove("username");
            MDC.remove("clientIp");
        }
    }
    
    /**
     * Registra un evento de logout
     */
    public void logLogout(String username) {
        try {
            MDC.put("action", "LOGOUT");
            MDC.put("username", username);
            
            auditLogger.info("🚪 LOGOUT | User: {} | Timestamp: {}", 
                    username,
                    LocalDateTime.now().format(formatter));
        } finally {
            MDC.remove("action");
            MDC.remove("username");
        }
    }
    
    /**
     * Registra un cambio de permisos o roles
     */
    public void logPermissionChange(String targetUser, String action, String details) {
        try {
            MDC.put("action", "PERMISSION_CHANGE");
            MDC.put("targetUser", targetUser);
            
            auditLogger.warn("⚡ PERMISSION CHANGE | Target: {} | Action: {} | Details: {} | By: {}", 
                    targetUser,
                    action,
                    details,
                    getCurrentUsername());
        } finally {
            MDC.remove("action");
            MDC.remove("targetUser");
        }
    }
    
    /**
     * Registra una exportación de datos
     */
    public void logDataExport(String dataType, int recordCount, String format) {
        try {
            MDC.put("action", "DATA_EXPORT");
            MDC.put("dataType", dataType);
            
            auditLogger.info("📊 DATA EXPORT | Type: {} | Records: {} | Format: {} | User: {}", 
                    dataType,
                    recordCount,
                    format,
                    getCurrentUsername());
        } finally {
            MDC.remove("action");
            MDC.remove("dataType");
        }
    }
    
    /**
     * Registra un cambio de estado importante
     */
    public void logStatusChange(String entity, Object entityId, String oldStatus, String newStatus) {
        try {
            MDC.put("action", "STATUS_CHANGE");
            MDC.put("entity", entity);
            MDC.put("entityId", String.valueOf(entityId));
            
            auditLogger.info("🔄 STATUS CHANGE | {} ID: {} | From: {} → To: {} | User: {}", 
                    entity,
                    entityId,
                    oldStatus,
                    newStatus,
                    getCurrentUsername());
        } finally {
            MDC.remove("action");
            MDC.remove("entity");
            MDC.remove("entityId");
        }
    }
    
    /**
     * Registra un evento personalizado
     */
    public void logCustomEvent(String eventType, String message, Object... params) {
        try {
            MDC.put("action", eventType);
            
            auditLogger.info("📝 {} | {} | User: {}", 
                    eventType,
                    String.format(message, params),
                    getCurrentUsername());
        } finally {
            MDC.remove("action");
        }
    }
    
    /**
     * Registra un error de seguridad
     */
    public void logSecurityEvent(String eventType, String details) {
        try {
            MDC.put("action", "SECURITY_EVENT");
            
            auditLogger.error("🚨 SECURITY EVENT | Type: {} | Details: {} | User: {} | Correlation-ID: {}", 
                    eventType,
                    details,
                    getCurrentUsername(),
                    MDC.get("correlationId"));
        } finally {
            MDC.remove("action");
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

