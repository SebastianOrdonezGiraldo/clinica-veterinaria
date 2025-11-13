import { AlertCircle, RefreshCw, Home } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Alert, AlertDescription, AlertTitle } from '@shared/components/ui/alert';
import { cn } from '@/shared/utils/utils';

interface ErrorStateProps {
  /** Título del error */
  title?: string;
  /** Mensaje de error */
  message?: string;
  /** Función para reintentar */
  onRetry?: () => void;
  /** Función para volver al inicio */
  onGoHome?: () => void;
  /** Si debe mostrarse en pantalla completa */
  fullScreen?: boolean;
  /** Clases adicionales */
  className?: string;
}

/**
 * Componente para mostrar estados de error de manera consistente
 * 
 * Características:
 * - Mensaje de error claro
 * - Botón para reintentar
 * - Botón para volver al inicio
 * - Modo pantalla completa
 * 
 * @example
 * ```tsx
 * // Error simple
 * <ErrorState message="No se pudieron cargar los datos" onRetry={refetch} />
 * 
 * // Error completo
 * <ErrorState 
 *   title="Error al cargar pacientes"
 *   message="No se pudo conectar con el servidor"
 *   onRetry={() => refetch()}
 *   onGoHome={() => navigate('/')}
 * />
 * ```
 */
export const ErrorState = ({
  title = 'Ocurrió un error',
  message = 'No se pudieron cargar los datos. Por favor, inténtelo de nuevo.',
  onRetry,
  onGoHome,
  fullScreen = false,
  className,
}: ErrorStateProps) => {
  const content = (
    <div className={cn(
      'flex flex-col items-center justify-center p-6',
      fullScreen && 'min-h-screen',
      className
    )}>
      <Alert variant="destructive" className="max-w-lg">
        <AlertCircle className="h-4 w-4" />
        <AlertTitle>{title}</AlertTitle>
        <AlertDescription className="mt-2">
          {message}
        </AlertDescription>
      </Alert>
      
      <div className="flex gap-3 mt-6">
        {onRetry && (
          <Button onClick={onRetry} variant="default">
            <RefreshCw className="mr-2 h-4 w-4" />
            Reintentar
          </Button>
        )}
        {onGoHome && (
          <Button onClick={onGoHome} variant="outline">
            <Home className="mr-2 h-4 w-4" />
            Ir al inicio
          </Button>
        )}
      </div>
    </div>
  );

  return content;
};

/**
 * Componente para mostrar mensaje cuando no hay datos
 */
interface EmptyStateProps {
  /** Título */
  title?: string;
  /** Mensaje */
  message?: string;
  /** Icono personalizado */
  icon?: React.ReactNode;
  /** Acción principal */
  action?: {
    label: string;
    onClick: () => void;
  };
  /** Clases adicionales */
  className?: string;
}

export const EmptyState = ({
  title = 'No hay datos',
  message = 'No se encontraron resultados',
  icon,
  action,
  className,
}: EmptyStateProps) => {
  return (
    <div className={cn(
      'flex flex-col items-center justify-center p-12 text-center',
      className
    )}>
      <div className="rounded-full bg-muted p-6 mb-4">
        {icon || <AlertCircle className="h-10 w-10 text-muted-foreground" />}
      </div>
      <h3 className="text-lg font-semibold mb-2">{title}</h3>
      <p className="text-sm text-muted-foreground max-w-sm mb-6">
        {message}
      </p>
      {action && (
        <Button onClick={action.onClick}>
          {action.label}
        </Button>
      )}
    </div>
  );
};

/**
 * Componente inline para errores pequeños
 */
interface InlineErrorProps {
  message: string;
  onRetry?: () => void;
}

export const InlineError = ({ message, onRetry }: InlineErrorProps) => {
  return (
    <div className="flex items-center justify-between p-3 bg-destructive/10 border border-destructive/20 rounded-md">
      <div className="flex items-center gap-2">
        <AlertCircle className="h-4 w-4 text-destructive" />
        <span className="text-sm text-destructive">{message}</span>
      </div>
      {onRetry && (
        <Button 
          size="sm" 
          variant="ghost" 
          onClick={onRetry}
          className="text-destructive hover:text-destructive"
        >
          <RefreshCw className="h-3 w-3" />
        </Button>
      )}
    </div>
  );
};

