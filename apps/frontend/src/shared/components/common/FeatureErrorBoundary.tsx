import React, { Component, ErrorInfo, ReactNode } from 'react';
import { loggerService } from '@core/logging/loggerService';
import { Button } from '../ui/button';
import { Card, CardContent } from '../ui/card';
import { AlertTriangle, RefreshCw } from 'lucide-react';
import { ErrorState } from './ErrorState';

interface Props {
  children: ReactNode;
  featureName?: string;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  onReset?: () => void;
}

interface State {
  hasError: boolean;
  error?: Error;
  errorInfo?: ErrorInfo;
}

/**
 * Error Boundary específico para features/módulos de la aplicación.
 * 
 * Útil para aislar errores en secciones específicas sin afectar
 * el resto de la aplicación.
 * 
 * @example
 * ```tsx
 * <FeatureErrorBoundary featureName="Dashboard">
 *   <Dashboard />
 * </FeatureErrorBoundary>
 * ```
 */
export class FeatureErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false
    };
  }

  static getDerivedStateFromError(error: Error): Partial<State> {
    return {
      hasError: true,
      error
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    // Log del error
    loggerService.error(
      `Feature Error Boundary caught an error${this.props.featureName ? ` in ${this.props.featureName}` : ''}`,
      error,
      {
        componentStack: errorInfo.componentStack,
        errorBoundary: true,
        featureName: this.props.featureName,
      }
    );

    this.setState({
      errorInfo
    });

    // Callback personalizado si se proporciona
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }
  }

  handleReset = (): void => {
    this.setState({
      hasError: false,
      error: undefined,
      errorInfo: undefined
    });

    // Callback personalizado si se proporciona
    if (this.props.onReset) {
      this.props.onReset();
    }
  };

  render(): ReactNode {
    if (this.state.hasError) {
      return (
        <Card className="my-4">
          <CardContent className="py-8">
            <ErrorState
              title={`Error en ${this.props.featureName || 'esta sección'}`}
              message="Ha ocurrido un error inesperado. Puedes intentar recargar esta sección."
              onRetry={this.handleReset}
            />
          </CardContent>
        </Card>
      );
    }

    return this.props.children;
  }
}

