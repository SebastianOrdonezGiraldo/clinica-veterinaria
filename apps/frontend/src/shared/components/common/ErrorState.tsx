import { AlertCircle, RefreshCw } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent } from '@shared/components/ui/card';

interface ErrorStateProps {
  title?: string;
  message?: string;
  onRetry?: () => void;
  className?: string;
}

/**
 * Componente reutilizable para mostrar estados de error
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
