import { useCallback } from 'react';
import { loggerService, LogLevel } from '@core/logging/loggerService';

/**
 * Hook personalizado para logging en componentes React
 * 
 * Proporciona métodos de logging tipados y contextualizados para componentes.
 * Todos los logs incluyen automáticamente información del componente y contexto.
 * 
 * @example
 * ```tsx
 * function MyComponent() {
 *   const logger = useLogger('MyComponent');
 *   
 *   const handleAction = async () => {
 *     try {
 *       logger.debug('Iniciando acción');
 *       await someAsyncOperation();
 *       logger.info('Acción completada exitosamente');
 *     } catch (error) {
 *       logger.error('Error al ejecutar acción', error, { action: 'handleAction' });
 *     }
 *   };
 * }
 * ```
 * 
 * @param componentName Nombre del componente para contexto en logs
 * @returns Objeto con métodos de logging (debug, info, warn, error)
 */
export function useLogger(componentName: string) {
  /**
   * Log de nivel DEBUG - Solo visible en desarrollo
   * Útil para información detallada durante desarrollo
   */
  const debug = useCallback(
    (message: string, context?: Record<string, any>) => {
      loggerService.debug(`[${componentName}] ${message}`, {
        component: componentName,
        ...context,
      });
    },
    [componentName]
  );

  /**
   * Log de nivel INFO
   * Para eventos normales y operaciones exitosas
   */
  const info = useCallback(
    (message: string, context?: Record<string, any>) => {
      loggerService.info(`[${componentName}] ${message}`, {
        component: componentName,
        ...context,
      });
    },
    [componentName]
  );

  /**
   * Log de nivel WARN
   * Para situaciones inusuales que no son errores críticos
   */
  const warn = useCallback(
    (message: string, context?: Record<string, any>) => {
      loggerService.warn(`[${componentName}] ${message}`, {
        component: componentName,
        ...context,
      });
    },
    [componentName]
  );

  /**
   * Log de nivel ERROR
   * Para errores que requieren atención
   * 
   * @param message Mensaje descriptivo del error
   * @param error Objeto de error (opcional)
   * @param context Contexto adicional (opcional)
   */
  const error = useCallback(
    (message: string, error?: any, context?: Record<string, any>) => {
      loggerService.error(
        `[${componentName}] ${message}`,
        error,
        {
          component: componentName,
          ...context,
        }
      );
    },
    [componentName]
  );

  /**
   * Log de evento de usuario
   * Para tracking de interacciones del usuario
   */
  const logUserEvent = useCallback(
    (eventName: string, details?: Record<string, any>) => {
      loggerService.logUserEvent(`${componentName}:${eventName}`, {
        component: componentName,
        ...details,
      });
    },
    [componentName]
  );

  /**
   * Log de performance
   * Para medir tiempos de operaciones
   */
  const logPerformance = useCallback(
    (metric: string, value: number, unit: string = 'ms') => {
      loggerService.logPerformance(`${componentName}:${metric}`, value, unit);
    },
    [componentName]
  );

  return {
    debug,
    info,
    warn,
    error,
    logUserEvent,
    logPerformance,
  };
}

/**
 * Tipo del objeto retornado por useLogger
 */
export type UseLoggerReturn = ReturnType<typeof useLogger>;

