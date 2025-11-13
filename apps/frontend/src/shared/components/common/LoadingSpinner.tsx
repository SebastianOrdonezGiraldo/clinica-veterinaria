import { Loader2 } from 'lucide-react';
import { cn } from '@/shared/utils/utils';

interface LoadingSpinnerProps {
  /** Tamaño del spinner */
  size?: 'sm' | 'md' | 'lg' | 'xl';
  /** Mensaje de carga (opcional) */
  message?: string;
  /** Si debe centrarse en pantalla completa */
  fullScreen?: boolean;
  /** Clases adicionales */
  className?: string;
}

const sizeClasses = {
  sm: 'h-4 w-4',
  md: 'h-8 w-8',
  lg: 'h-12 w-12',
  xl: 'h-16 w-16',
};

/**
 * Componente de spinner de carga reutilizable
 * 
 * Características:
 * - Múltiples tamaños
 * - Mensaje opcional
 * - Modo pantalla completa
 * - Animación suave
 * 
 * @example
 * ```tsx
 * // Spinner simple
 * <LoadingSpinner />
 * 
 * // Con mensaje
 * <LoadingSpinner message="Cargando pacientes..." />
 * 
 * // Pantalla completa
 * <LoadingSpinner fullScreen message="Procesando..." />
 * ```
 */
export const LoadingSpinner = ({ 
  size = 'md', 
  message, 
  fullScreen = false,
  className 
}: LoadingSpinnerProps) => {
  const content = (
    <div className={cn(
      'flex flex-col items-center justify-center gap-3',
      fullScreen && 'min-h-screen',
      className
    )}>
      <Loader2 
        className={cn(
          'animate-spin text-primary',
          sizeClasses[size]
        )} 
      />
      {message && (
        <p className="text-sm text-muted-foreground animate-pulse">
          {message}
        </p>
      )}
    </div>
  );

  if (fullScreen) {
    return (
      <div className="fixed inset-0 bg-background/80 backdrop-blur-sm z-50 flex items-center justify-center">
        {content}
      </div>
    );
  }

  return content;
};

/**
 * Componente de skeleton loader para tablas
 */
export const TableSkeleton = ({ rows = 5, columns = 4 }: { rows?: number; columns?: number }) => {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, rowIndex) => (
        <div key={rowIndex} className="flex gap-4">
          {Array.from({ length: columns }).map((_, colIndex) => (
            <div
              key={colIndex}
              className="h-10 bg-muted animate-pulse rounded flex-1"
            />
          ))}
        </div>
      ))}
    </div>
  );
};

/**
 * Componente de skeleton loader para cards
 */
export const CardSkeleton = ({ count = 1 }: { count?: number }) => {
  return (
    <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="rounded-lg border p-6 space-y-3">
          <div className="h-4 bg-muted animate-pulse rounded w-3/4" />
          <div className="h-3 bg-muted animate-pulse rounded w-1/2" />
          <div className="h-20 bg-muted animate-pulse rounded" />
        </div>
      ))}
    </div>
  );
};

