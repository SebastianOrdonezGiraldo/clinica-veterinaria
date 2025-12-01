import { AxiosError } from 'axios';
import { toast } from 'sonner';
import { loggerService } from '@core/logging/loggerService';

/**
 * Interface para errores estructurados del backend
 */
interface BackendErrorResponse {
  mensaje: string;
  status: number;
  timestamp: string;
  path?: string;
  errores?: Record<string, string>;
  detalle?: string;
  campo?: string;
  valor?: any;
}

/**
 * Hook personalizado para manejar errores de API de manera consistente
 * 
 * Características:
 * - Extrae mensajes de error estructurados del backend
 * - Muestra notificaciones toast automáticas
 * - Maneja diferentes tipos de errores (validación, autenticación, servidor, red)
 * - Proporciona mensajes amigables para el usuario
 * 
 * @returns Funciones para manejar diferentes tipos de errores
 */
export const useApiError = () => {
  /**
   * Extrae el mensaje de error del response del backend
   */
  const extractErrorMessage = (error: AxiosError<BackendErrorResponse>): string => {
    if (!error.response) {
      // Error de red o timeout
      if (error.code === 'ECONNABORTED') {
        return 'La petición tardó demasiado tiempo. Por favor, inténtelo de nuevo.';
      }
      if (error.message === 'Network Error') {
        return 'No se pudo conectar con el servidor. Verifique su conexión a internet.';
      }
      return 'Error de conexión. Por favor, inténtelo más tarde.';
    }

    const { data, status } = error.response;

    // Errores de validación (400) con múltiples campos
    if (status === 400 && data?.errores) {
      const errores = Object.entries(data.errores)
        .map(([campo, mensaje]) => `${campo}: ${mensaje}`)
        .join('\n');
      return `Errores de validación:\n${errores}`;
    }

    // Mensaje estructurado del backend
    if (data?.mensaje) {
      return data.mensaje;
    }

    // Mensajes por defecto según código HTTP
    switch (status) {
      case 400:
        return 'Los datos enviados no son válidos.';
      case 401:
        return 'Su sesión ha expirado. Por favor, inicie sesión nuevamente.';
      case 403:
        return 'No tiene permisos para realizar esta acción.';
      case 404:
        return 'El recurso solicitado no fue encontrado.';
      case 409:
        return 'Ya existe un registro con esos datos.';
      case 422:
        return 'No se pudo procesar la solicitud. Verifique los datos.';
      case 500:
        return 'Error interno del servidor. Por favor, inténtelo más tarde.';
      case 503:
        return 'El servicio no está disponible temporalmente.';
      default:
        return `Error del servidor (${status})`;
    }
  };

  /**
   * Maneja errores de API mostrando notificación toast
   * 
   * Utiliza el logger centralizado en lugar de console.error para:
   * - Envío automático de logs críticos al backend
   * - Mejor trazabilidad y debugging
   * - Logging estructurado con contexto
   * 
   * @param error Error de axios
   * @param customMessage Mensaje personalizado (opcional)
   */
  const handleError = (error: unknown, customMessage?: string) => {
    if (error instanceof AxiosError) {
      const message = customMessage || extractErrorMessage(error);
      const status = error.response?.status;
      const url = error.config?.url || 'unknown';
      const method = error.config?.method?.toUpperCase() || 'UNKNOWN';
      const data = error.response?.data;

      // Asegurar que la descripción del toast sea siempre un string (React no acepta objetos como children)
      const description =
        data && typeof (data as any).detalle === 'string'
          ? (data as any).detalle
          : undefined;
      
      // Log estructurado del error
      loggerService.logApiError(
        method,
        url,
        status || 0,
        data,
        0, // duration no disponible aquí
        error.config?.headers?.['X-Correlation-ID'] as string || 'unknown'
      );
      
      // No mostrar toast para 401 porque se redirige automáticamente
      if (status !== 401) {
        toast.error(message, {
          duration: 5000,
          description,
        });
      }
    } else if (error instanceof Error) {
      // Log de errores no relacionados con API
      loggerService.error('Error no relacionado con API', error, {
        type: 'non-api-error',
      });
      toast.error(customMessage || error.message);
    } else {
      // Log de errores desconocidos
      loggerService.error('Error desconocido', undefined, {
        type: 'unknown-error',
        error: String(error),
      });
      toast.error(customMessage || 'Ocurrió un error inesperado');
    }
  };

  /**
   * Maneja errores de validación mostrando cada campo
   * 
   * @param error Error de axios con errores de validación
   */
  const handleValidationError = (error: AxiosError<BackendErrorResponse>) => {
    if (error.response?.data?.errores) {
      Object.entries(error.response.data.errores).forEach(([campo, mensaje]) => {
        toast.error(`${campo}: ${mensaje}`, {
          duration: 4000,
        });
      });
    } else {
      handleError(error);
    }
  };

  /**
   * Muestra mensaje de éxito
   * 
   * @param message Mensaje de éxito
   */
  const showSuccess = (message: string, description?: string) => {
    toast.success(message, {
      duration: 3000,
      description,
    });
  };

  /**
   * Muestra mensaje de advertencia
   * 
   * @param message Mensaje de advertencia
   */
  const showWarning = (message: string, description?: string) => {
    toast.warning(message, {
      duration: 4000,
      description,
    });
  };

  /**
   * Muestra mensaje informativo
   * 
   * @param message Mensaje informativo
   */
  const showInfo = (message: string, description?: string) => {
    toast.info(message, {
      duration: 3000,
      description,
    });
  };

  return {
    handleError,
    handleValidationError,
    showSuccess,
    showWarning,
    showInfo,
    extractErrorMessage,
  };
};

export type UseApiError = ReturnType<typeof useApiError>;

