/**
 * Servicio centralizado de logging para el frontend
 * 
 * Características:
 * - Niveles de log: debug, info, warn, error
 * - Logging estructurado con metadata
 * - Envío de logs al backend (opcional)
 * - Almacenamiento local de logs recientes
 * - Sanitización de datos sensibles
 */

export enum LogLevel {
  DEBUG = 'DEBUG',
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR'
}

interface LogEntry {
  timestamp: string;
  level: LogLevel;
  message: string;
  context?: Record<string, any>;
  error?: any;
  userAgent?: string;
  url?: string;
  userId?: string;
  correlationId?: string;
}

class LoggerService {
  private logs: LogEntry[] = [];
  private maxLogsInMemory = 100;
  private isProduction = import.meta.env.PROD;
  private enableRemoteLogging = true; // Cambiar a true para enviar logs al backend
  
  /**
   * Log nivel DEBUG - Solo en desarrollo
   */
  debug(message: string, context?: Record<string, any>): void {
    if (!this.isProduction) {
      this.log(LogLevel.DEBUG, message, context);
    }
  }
  
  /**
   * Log nivel INFO
   */
  info(message: string, context?: Record<string, any>): void {
    this.log(LogLevel.INFO, message, context);
  }
  
  /**
   * Log nivel WARN
   */
  warn(message: string, context?: Record<string, any>): void {
    this.log(LogLevel.WARN, message, context);
  }
  
  /**
   * Log nivel ERROR
   */
  error(message: string, error?: any, context?: Record<string, any>): void {
    this.log(LogLevel.ERROR, message, context, error);
  }
  
  /**
   * Log de petición API
   */
  logApiRequest(method: string, url: string, correlationId: string, params?: any): void {
    this.debug(`→ API Request: ${method} ${url}`, {
      method,
      url,
      correlationId,
      params: this.sanitize(params),
      type: 'api-request'
    });
  }
  
  /**
   * Log de respuesta API exitosa
   */
  logApiResponse(method: string, url: string, status: number, duration: number, correlationId: string): void {
    const level = status >= 400 ? LogLevel.WARN : LogLevel.INFO;
    this.log(level, `← API Response: ${method} ${url} [${status}] ${duration}ms`, {
      method,
      url,
      status,
      duration,
      correlationId,
      type: 'api-response'
    });
  }
  
  /**
   * Log de error de API
   */
  logApiError(
    method: string, 
    url: string, 
    status: number, 
    errorData: any, 
    duration: number,
    correlationId: string
  ): void {
    this.error(
      `✗ API Error: ${method} ${url} [${status}] ${duration}ms`,
      errorData,
      {
        method,
        url,
        status,
        duration,
        correlationId,
        type: 'api-error'
      }
    );
  }
  
  /**
   * Log de evento de usuario
   */
  logUserEvent(eventName: string, details?: Record<string, any>): void {
    this.info(`User Event: ${eventName}`, {
      eventName,
      ...details,
      type: 'user-event'
    });
  }
  
  /**
   * Log de navegación
   */
  logNavigation(from: string, to: string): void {
    this.debug(`Navigation: ${from} → ${to}`, {
      from,
      to,
      type: 'navigation'
    });
  }
  
  /**
   * Log de autenticación
   */
  logAuth(action: 'login' | 'logout' | 'token-refresh', userId?: string): void {
    this.info(`Auth: ${action}`, {
      action,
      userId,
      type: 'auth'
    });
  }
  
  /**
   * Log de performance
   */
  logPerformance(metric: string, value: number, unit: string = 'ms'): void {
    this.info(`Performance: ${metric} = ${value}${unit}`, {
      metric,
      value,
      unit,
      type: 'performance'
    });
  }
  
  /**
   * Método principal de logging
   */
  private log(level: LogLevel, message: string, context?: Record<string, any>, error?: any): void {
    const logEntry: LogEntry = {
      timestamp: new Date().toISOString(),
      level,
      message,
      context: this.sanitize(context),
      error: error ? this.serializeError(error) : undefined,
      userAgent: navigator.userAgent,
      url: window.location.href,
      userId: this.getUserId(),
      correlationId: context?.correlationId
    };
    
    // Guardar en memoria (buffer circular)
    this.logs.push(logEntry);
    if (this.logs.length > this.maxLogsInMemory) {
      this.logs.shift();
    }
    
    // Log en consola con formato
    this.logToConsole(logEntry);
    
    // Enviar logs críticos al backend
    if (this.enableRemoteLogging && (level === LogLevel.ERROR || level === LogLevel.WARN)) {
      this.sendToBackend(logEntry);
    }
    
    // Guardar en localStorage para análisis posterior
    this.saveToLocalStorage(logEntry);
  }
  
  /**
   * Log formateado en consola del navegador
   */
  private logToConsole(entry: LogEntry): void {
    const emoji = this.getEmojiForLevel(entry.level);
    const color = this.getColorForLevel(entry.level);
    const timestamp = new Date(entry.timestamp).toLocaleTimeString();
    
    const style = `color: ${color}; font-weight: bold;`;
    
    const consoleMethod = this.getConsoleMethod(entry.level);
    
    if (entry.error) {
      consoleMethod(
        `%c[${timestamp}] ${emoji} ${entry.level}`,
        style,
        entry.message,
        '\nContext:',
        entry.context,
        '\nError:',
        entry.error
      );
    } else if (entry.context && Object.keys(entry.context).length > 0) {
      consoleMethod(
        `%c[${timestamp}] ${emoji} ${entry.level}`,
        style,
        entry.message,
        entry.context
      );
    } else {
      consoleMethod(
        `%c[${timestamp}] ${emoji} ${entry.level}`,
        style,
        entry.message
      );
    }
  }
  
  /**
   * Enviar log al backend
   */
  private async sendToBackend(entry: LogEntry): Promise<void> {
    try {
      // Evitar ciclos infinitos: no logear errores de logging
      const sanitizedEntry = {
        ...entry,
        userAgent: undefined, // El backend ya lo tiene
        url: entry.url
      };
      
      // Usar fetch nativo en lugar de axios para evitar interceptores
      await fetch('/api/logs/frontend', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Correlation-ID': entry.correlationId || 'unknown'
        },
        body: JSON.stringify(sanitizedEntry)
      });
    } catch (error) {
      // Silenciosamente fallar si no se puede enviar al backend
      // console.error('Failed to send log to backend:', error);
    }
  }
  
  /**
   * Guardar en localStorage para debugging
   */
  private saveToLocalStorage(entry: LogEntry): void {
    try {
      const key = `log_${entry.timestamp}`;
      const logsInStorage = this.getLogsFromLocalStorage();
      
      // Mantener solo los últimos 50 logs
      if (logsInStorage.length >= 50) {
        const oldestKey = logsInStorage[0];
        localStorage.removeItem(oldestKey);
      }
      
      localStorage.setItem(key, JSON.stringify(entry));
    } catch (error) {
      // Ignorar errores de almacenamiento (cuota excedida, etc)
    }
  }
  
  /**
   * Obtener logs del localStorage
   */
  private getLogsFromLocalStorage(): string[] {
    const keys: string[] = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (key && key.startsWith('log_')) {
        keys.push(key);
      }
    }
    return keys.sort();
  }
  
  /**
   * Obtener logs recientes de memoria
   */
  getRecentLogs(count: number = 20): LogEntry[] {
    return this.logs.slice(-count);
  }
  
  /**
   * Limpiar logs de localStorage
   */
  clearLogs(): void {
    const keys = this.getLogsFromLocalStorage();
    keys.forEach(key => localStorage.removeItem(key));
    this.logs = [];
  }
  
  /**
   * Exportar logs para debugging
   */
  exportLogs(): string {
    return JSON.stringify(this.logs, null, 2);
  }
  
  /**
   * Sanitizar datos sensibles
   */
  private sanitize(data: any): any {
    if (!data) return data;
    
    const sensitiveKeys = ['password', 'token', 'secret', 'authorization', 'apiKey'];
    const sanitized = JSON.parse(JSON.stringify(data));
    
    const recursiveSanitize = (obj: any): any => {
      if (typeof obj !== 'object' || obj === null) return obj;
      
      for (const key in obj) {
        if (sensitiveKeys.some(sk => key.toLowerCase().includes(sk.toLowerCase()))) {
          obj[key] = '***REDACTED***';
        } else if (typeof obj[key] === 'object') {
          recursiveSanitize(obj[key]);
        }
      }
      
      return obj;
    };
    
    return recursiveSanitize(sanitized);
  }
  
  /**
   * Serializar error para logging
   */
  private serializeError(error: any): any {
    if (error instanceof Error) {
      return {
        name: error.name,
        message: error.message,
        stack: error.stack,
        ...error
      };
    }
    return error;
  }
  
  /**
   * Obtener ID del usuario actual
   */
  private getUserId(): string | undefined {
    try {
      const userStr = localStorage.getItem('user');
      if (userStr) {
        const user = JSON.parse(userStr);
        return user.id || user.email || user.username;
      }
    } catch (error) {
      // Ignorar errores
    }
    return undefined;
  }
  
  /**
   * Obtener emoji según nivel de log
   */
  private getEmojiForLevel(level: LogLevel): string {
    switch (level) {
      case LogLevel.DEBUG: return '🔍';
      case LogLevel.INFO: return 'ℹ️';
      case LogLevel.WARN: return '⚠️';
      case LogLevel.ERROR: return '❌';
      default: return '📝';
    }
  }
  
  /**
   * Obtener color según nivel de log
   */
  private getColorForLevel(level: LogLevel): string {
    switch (level) {
      case LogLevel.DEBUG: return '#6c757d';
      case LogLevel.INFO: return '#0d6efd';
      case LogLevel.WARN: return '#ffc107';
      case LogLevel.ERROR: return '#dc3545';
      default: return '#000000';
    }
  }
  
  /**
   * Obtener método de consola según nivel
   */
  private getConsoleMethod(level: LogLevel): (...args: any[]) => void {
    switch (level) {
      case LogLevel.DEBUG: return console.debug;
      case LogLevel.INFO: return console.info;
      case LogLevel.WARN: return console.warn;
      case LogLevel.ERROR: return console.error;
      default: return console.log;
    }
  }
}

// Exportar instancia singleton
export const loggerService = new LoggerService();

// Exponer en window para debugging en consola
if (typeof window !== 'undefined') {
  (window as any).logger = loggerService;
}

