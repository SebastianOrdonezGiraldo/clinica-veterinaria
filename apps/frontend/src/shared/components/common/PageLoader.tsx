import { Loader2 } from 'lucide-react';

/**
 * Componente de carga para páginas lazy-loaded
 * 
 * Muestra un spinner centrado mientras se carga el componente.
 * Usado con React.lazy() y Suspense para mejorar el tiempo de carga inicial.
 */
export function PageLoader() {
  return (
    <div className="flex items-center justify-center min-h-screen bg-background">
      <div className="flex flex-col items-center gap-4">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <p className="text-sm text-muted-foreground">Cargando...</p>
      </div>
    </div>
  );
}

