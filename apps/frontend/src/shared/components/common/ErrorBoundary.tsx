import React, { Component, ErrorInfo, ReactNode } from 'react';
import { loggerService } from '../../../core/logging/loggerService';
import { Button } from '../ui/button';
import { AlertTriangle, Home, RefreshCw } from 'lucide-react';

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
}

interface State {
  hasError: boolean;
  error?: Error;
  errorInfo?: ErrorInfo;
  errorCount: number;
}

/**
 * Error Boundary para capturar errores de React y mostrar UI de fallback
 * 
 * Captura:
 * - Errores en el render de componentes
 * - Errores en lifecycle methods
 * - Errores en constructores
 * 
 * NO captura:
 * - Errores en event handlers (usar try-catch)
 * - Errores asíncronos (usar try-catch o .catch())
 * - Errores en el server-side rendering
 */
export class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false,
      errorCount: 0
    };
  }

  static getDerivedStateFromError(error: Error): State {
    // Actualizar estado para mostrar UI de fallback
    return {
      hasError: true,
      error,
      errorCount: 0
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    // Log del error
    loggerService.error(
      'React Error Boundary caught an error',
      error,
      {
        componentStack: errorInfo.componentStack,
        errorBoundary: true,
        errorCount: this.state.errorCount + 1
      }
    );

    // Actualizar contador de errores
    this.setState(prevState => ({
      errorInfo,
      errorCount: prevState.errorCount + 1
    }));

    // Callback personalizado si se proporciona
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    // Si hay demasiados errores consecutivos, algo está muy mal
    if (this.state.errorCount >= 3) {
      loggerService.error('Too many consecutive errors detected', error, {
        errorCount: this.state.errorCount,
        critical: true
      });
    }
  }

  handleReset = (): void => {
    this.setState({
      hasError: false,
      error: undefined,
      errorInfo: undefined
    });
  };

  handleGoHome = (): void => {
    window.location.href = '/';
  };

  handleReload = (): void => {
    window.location.reload();
  };

  render(): ReactNode {
    if (this.state.hasError) {
      // Si se proporciona un fallback personalizado, usarlo
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // UI de fallback por defecto
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
          <div className="max-w-2xl w-full bg-white rounded-lg shadow-lg p-8">
            <div className="flex items-center justify-center mb-6">
              <div className="bg-red-100 p-4 rounded-full">
                <AlertTriangle className="h-12 w-12 text-red-600" />
              </div>
            </div>

            <h1 className="text-3xl font-bold text-gray-900 text-center mb-4">
              ¡Oops! Algo salió mal
            </h1>

            <p className="text-gray-600 text-center mb-6">
              Ha ocurrido un error inesperado en la aplicación. 
              Nuestro equipo ha sido notificado automáticamente.
            </p>

            {/* Mostrar detalles del error solo en desarrollo */}
            {!import.meta.env.PROD && this.state.error && (
              <div className="mb-6 p-4 bg-gray-100 rounded-md overflow-auto max-h-64">
                <h3 className="font-semibold text-gray-900 mb-2">
                  Detalles del error (solo en desarrollo):
                </h3>
                <p className="text-sm text-red-600 mb-2">
                  <strong>{this.state.error.name}:</strong> {this.state.error.message}
                </p>
                {this.state.error.stack && (
                  <pre className="text-xs text-gray-700 whitespace-pre-wrap">
                    {this.state.error.stack}
                  </pre>
                )}
                {this.state.errorInfo?.componentStack && (
                  <details className="mt-4">
                    <summary className="cursor-pointer text-sm font-semibold text-gray-900">
                      Component Stack
                    </summary>
                    <pre className="text-xs text-gray-700 whitespace-pre-wrap mt-2">
                      {this.state.errorInfo.componentStack}
                    </pre>
                  </details>
                )}
              </div>
            )}

            {/* Botones de acción */}
            <div className="flex flex-col sm:flex-row gap-3 justify-center">
              <Button
                onClick={this.handleReset}
                variant="default"
                className="flex items-center gap-2"
              >
                <RefreshCw className="h-4 w-4" />
                Intentar de nuevo
              </Button>

              <Button
                onClick={this.handleGoHome}
                variant="outline"
                className="flex items-center gap-2"
              >
                <Home className="h-4 w-4" />
                Ir al inicio
              </Button>

              <Button
                onClick={this.handleReload}
                variant="outline"
                className="flex items-center gap-2"
              >
                <RefreshCw className="h-4 w-4" />
                Recargar página
              </Button>
            </div>

            {/* Información adicional */}
            <div className="mt-8 pt-6 border-t border-gray-200">
              <p className="text-sm text-gray-500 text-center">
                Si el problema persiste, por favor contacta al soporte técnico.
              </p>
              {this.state.errorCount >= 3 && (
                <p className="text-sm text-red-600 text-center mt-2 font-semibold">
                  ⚠️ Se han detectado múltiples errores consecutivos. 
                  Considera recargar la página completamente.
                </p>
              )}
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

// Hook para usar Error Boundary de forma programática
export const useErrorHandler = () => {
  const [error, setError] = React.useState<Error | null>(null);

  React.useEffect(() => {
    if (error) {
      throw error;
    }
  }, [error]);

  return setError;
};

