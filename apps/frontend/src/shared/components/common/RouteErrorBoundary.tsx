import React, { Component, ErrorInfo, ReactNode } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { loggerService } from '@core/logging/loggerService';
import { Button } from '../ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { AlertTriangle, Home, RefreshCw, ArrowLeft } from 'lucide-react';

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
 * Error Boundary específico para rutas/páginas.
 * 
 * Captura errores de renderizado en componentes de página y muestra
 * una UI de fallback apropiada para el contexto de la aplicación.
 */
export class RouteErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false,
      errorCount: 0
    };
  }

  static getDerivedStateFromError(error: Error): Partial<State> {
    return {
      hasError: true,
      error
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    const errorCount = this.state.errorCount + 1;

    // Log del error
    loggerService.error(
      'Route Error Boundary caught an error',
      error,
      {
        componentStack: errorInfo.componentStack,
        errorBoundary: true,
        errorCount,
        route: window.location.pathname,
      }
    );

    this.setState({
      errorInfo,
      errorCount
    });

    // Callback personalizado si se proporciona
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    // Si hay demasiados errores consecutivos, algo está muy mal
    if (errorCount >= 3) {
      loggerService.error('Too many consecutive errors detected in route', error, {
        errorCount,
        critical: true,
        route: window.location.pathname,
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

  handleGoBack = (): void => {
    window.history.back();
  };

  handleGoHome = (): void => {
    window.location.href = '/';
  };

  render(): ReactNode {
    if (this.state.hasError) {
      // Si se proporciona un fallback personalizado, usarlo
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // UI de fallback por defecto
      return (
        <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4">
          <Card className="max-w-2xl w-full">
            <CardHeader>
              <div className="flex items-center justify-center mb-4">
                <div className="bg-destructive/10 p-4 rounded-full">
                  <AlertTriangle className="h-12 w-12 text-destructive" />
                </div>
              </div>
              <CardTitle className="text-2xl text-center">
                Error al cargar la página
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <p className="text-muted-foreground text-center">
                Ha ocurrido un error inesperado al cargar esta página. 
                Nuestro equipo ha sido notificado automáticamente.
              </p>

              {/* Mostrar detalles del error solo en desarrollo */}
              {!import.meta.env.PROD && this.state.error && (
                <div className="p-4 bg-muted rounded-md overflow-auto max-h-64">
                  <h3 className="font-semibold text-foreground mb-2 text-sm">
                    Detalles del error (solo en desarrollo):
                  </h3>
                  <p className="text-sm text-destructive mb-2">
                    <strong>{this.state.error.name}:</strong> {this.state.error.message}
                  </p>
                  {this.state.error.stack && (
                    <pre className="text-xs text-muted-foreground whitespace-pre-wrap">
                      {this.state.error.stack}
                    </pre>
                  )}
                  {this.state.errorInfo?.componentStack && (
                    <details className="mt-4">
                      <summary className="cursor-pointer text-sm font-semibold text-foreground">
                        Component Stack
                      </summary>
                      <pre className="text-xs text-muted-foreground whitespace-pre-wrap mt-2">
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
                  className="gap-2"
                >
                  <RefreshCw className="h-4 w-4" />
                  Intentar de nuevo
                </Button>

                <Button
                  onClick={this.handleGoBack}
                  variant="outline"
                  className="gap-2"
                >
                  <ArrowLeft className="h-4 w-4" />
                  Volver
                </Button>

                <Button
                  onClick={this.handleGoHome}
                  variant="outline"
                  className="gap-2"
                >
                  <Home className="h-4 w-4" />
                  Ir al inicio
                </Button>
              </div>

              {/* Información adicional */}
              {this.state.errorCount >= 3 && (
                <div className="mt-4 pt-4 border-t">
                  <p className="text-sm text-destructive text-center font-semibold">
                    ⚠️ Se han detectado múltiples errores consecutivos. 
                    Considera recargar la página completamente.
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      );
    }

    return this.props.children;
  }
}

/**
 * Hook para usar Error Boundary de forma programática en componentes funcionales
 */
export const useErrorHandler = () => {
  const [error, setError] = React.useState<Error | null>(null);

  React.useEffect(() => {
    if (error) {
      throw error;
    }
  }, [error]);

  return setError;
};

