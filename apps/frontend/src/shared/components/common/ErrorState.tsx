import { AlertCircle, RefreshCw } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent } from '@shared/components/ui/card';

/**
 * Props del componente ErrorState
 */
interface ErrorStateProps {
  /** Título del error (default: 'Error al cargar datos') */
  title?: string;
  /** Mensaje descriptivo del error (default: mensaje genérico) */
  message?: string;
  /** Función callback para reintentar la operación que falló */
  onRetry?: () => void;
  /** Clases CSS adicionales para el contenedor */
  className?: string;
}

/**
 * Componente reutilizable para mostrar estados de error.
 * 
 * Muestra un mensaje de error amigable con opción de reintentar la operación.
 * Diseñado para ser usado cuando falla la carga de datos o una operación crítica.
 * 
 * **Características:**
 * - Mensaje de error claro y centrado
 * - Botón opcional para reintentar
 * - Diseño consistente con el sistema de diseño
 * - Accesible con iconos y texto descriptivo
 * 
 * @param props Props del componente
 * @returns Componente de error con opción de reintentar
 * 
 * @example
 * ```tsx
 * function MyComponent() {
 *   const { data, error, refetch } = useQuery(...);
 *   
 *   if (error) {
 *     return (
 *       <ErrorState
 *         title="Error al cargar pacientes"
 *         message="No se pudieron cargar los pacientes. Verifica tu conexión."
 *         onRetry={() => refetch()}
 *       />
 *     );
 *   }
 *   
 *   return <div>...</div>;
 * }
 * ```
 * 
 * @see ErrorBoundary Para errores de renderizado de React
 * @see useApiError Para manejo de errores de API
 */
export function ErrorState({ 
  title = 'Error al cargar datos', 
  message = 'Ocurrió un error inesperado. Por favor, intenta nuevamente.',
  onRetry,
  className = '',
}: ErrorStateProps) {
  return (
    <Card className={className}>
      <CardContent className="flex flex-col items-center justify-center py-12">
        <AlertCircle className="h-12 w-12 text-destructive mb-4" />
        <h3 className="text-lg font-medium text-foreground mb-2">{title}</h3>
        <p className="text-sm text-muted-foreground mb-4 text-center max-w-md">
          {message}
        </p>
        {onRetry && (
          <Button onClick={onRetry} variant="outline" className="gap-2">
            <RefreshCw className="h-4 w-4" />
            Reintentar
          </Button>
        )}
      </CardContent>
    </Card>
  );
}
