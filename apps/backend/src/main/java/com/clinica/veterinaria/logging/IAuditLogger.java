package com.clinica.veterinaria.logging;

/**
 * Interfaz para el servicio de auditoría.
 * 
 * <p>Esta interfaz permite que AuditLogger sea fácilmente mockeable en tests unitarios,
 * especialmente importante cuando se usa Java 23+ donde Mockito tiene limitaciones
 * para mockear clases concretas.</p>
 * 
 * @author Sebastian Ordoñez
 * @version 1.0.0
 * @since 2025-11-06
 * @see AuditLogger
 */
public interface IAuditLogger {
    
    /**
     * Registra un evento de creación
     */
    void logCreate(String entity, Object entityId, Object data);
    
    /**
     * Registra un evento de actualización
     */
    void logUpdate(String entity, Object entityId, Object oldData, Object newData);
    
    /**
     * Registra un evento de eliminación
     */
    void logDelete(String entity, Object entityId);
    
    /**
     * Registra un evento de acceso/lectura a información sensible
     */
    void logAccess(String entity, Object entityId, String reason);
    
    /**
     * Registra un evento de autenticación exitosa
     */
    void logLoginSuccess(String username, String ipAddress);
    
    /**
     * Registra un intento de autenticación fallido
     */
    void logLoginFailure(String username, String ipAddress, String reason);
    
    /**
     * Registra un evento de logout
     */
    void logLogout(String username);
    
    /**
     * Registra un cambio de permisos o roles
     */
    void logPermissionChange(String targetUser, String action, String details);
    
    /**
     * Registra una exportación de datos
     */
    void logDataExport(String dataType, int recordCount, String format);
    
    /**
     * Registra un cambio de estado importante
     */
    void logStatusChange(String entity, Object entityId, String oldStatus, String newStatus);
    
    /**
     * Registra un evento personalizado
     */
    void logCustomEvent(String eventType, String message, Object... params);
    
    /**
     * Registra un error de seguridad
     */
    void logSecurityEvent(String eventType, String details);
}

