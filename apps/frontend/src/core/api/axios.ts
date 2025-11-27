import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { loggerService } from '../logging/loggerService';

// Función para generar Correlation ID único
const generateCorrelationId = (): string => {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
};

// Configuración base de axios
const axiosInstance = axios.create({
  baseURL: '/api', // El proxy de Vite redirigirá a http://localhost:8080/api
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 segundos
});

// Interceptor para agregar metadata y logging a las peticiones
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // Generar o recuperar Correlation ID
    const correlationId = config.headers['X-Correlation-ID'] as string || generateCorrelationId();
    config.headers['X-Correlation-ID'] = correlationId;
    
    // Agregar token JWT según el tipo de usuario
    const userType = localStorage.getItem('userType');
    let token: string | null = null;
    
    if (userType === 'CLIENTE') {
      token = localStorage.getItem('clienteToken');
    } else if (userType === 'SISTEMA') {
      token = localStorage.getItem('token');
    } else {
      // Fallback: intentar ambos tokens si no hay userType
      token = localStorage.getItem('token') || localStorage.getItem('clienteToken');
    }
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Agregar timestamp de inicio para medir duración
    (config as any).metadata = { 
      startTime: Date.now(),
      correlationId 
    };
    
    // Log de request saliente
    loggerService.logApiRequest(
      config.method?.toUpperCase() || 'GET',
      config.url || '',
      correlationId,
      config.params
    );
    
    return config;
  },
  (error: AxiosError) => {
    loggerService.error('Request interceptor error', error, {
      context: 'axios-request-interceptor'
    });
    return Promise.reject(error);
  }
);

// Interceptor para logging de respuestas
axiosInstance.interceptors.response.use(
  (response) => {
    // Calcular duración de la petición
    const config = response.config as any;
    const duration = config.metadata ? Date.now() - config.metadata.startTime : 0;
    const correlationId = config.metadata?.correlationId || 'unknown';
    
    // Log de response exitoso
    loggerService.logApiResponse(
      config.method?.toUpperCase() || 'GET',
      config.url || '',
      response.status,
      duration,
      correlationId
    );
    
    // Alertar si la petición fue lenta
    if (duration > 3000) {
      loggerService.warn(`Slow API call detected: ${config.method} ${config.url} took ${duration}ms`, {
        duration,
        correlationId,
        url: config.url,
        method: config.method
      });
    }
    
    return response;
  },
  (error: AxiosError) => {
    // Calcular duración de la petición fallida
    const config = error.config as any;
    const duration = config?.metadata ? Date.now() - config.metadata.startTime : 0;
    const correlationId = config?.metadata?.correlationId || 'unknown';
    
    // Log de error de respuesta
    if (error.response) {
      // El servidor respondió con un código de error
      loggerService.logApiError(
        config?.method?.toUpperCase() || 'UNKNOWN',
        config?.url || 'unknown',
        error.response.status,
        error.response.data || error.message,
        duration,
        correlationId
      );
      
      // Manejar errores específicos
      if (error.response.status === 401) {
        // Token expirado o inválido
        loggerService.warn('Unauthorized access - redirecting to login', {
          correlationId,
          url: config?.url
        });
        // Limpiar todos los tokens
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('clienteToken');
        localStorage.removeItem('cliente');
        localStorage.removeItem('userType');
        // Redirigir al login unificado
        window.location.href = '/login';
      } else if (error.response.status === 403) {
        loggerService.warn('Access forbidden', {
          correlationId,
          url: config?.url
        });
      } else if (error.response.status >= 500) {
        loggerService.error('Server error', error, {
          correlationId,
          url: config?.url,
          status: error.response.status
        });
      }
    } else if (error.request) {
      // La petición se hizo pero no hubo respuesta (timeout, red caída, etc)
      loggerService.error('No response from server', error, {
        correlationId,
        url: config?.url,
        timeout: config?.timeout,
        message: error.message
      });
    } else {
      // Error al configurar la petición
      loggerService.error('Request configuration error', error, {
        correlationId,
        message: error.message
      });
    }
    
    return Promise.reject(error);
  }
);

export default axiosInstance;

