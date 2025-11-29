import { Navigate } from 'react-router-dom';
import { useAuth } from '@core/auth/AuthContext';
import { Skeleton } from '@shared/components/ui/skeleton';

/**
 * Propiedades del componente ClienteProtectedRoute.
 *
 * @interface ClienteProtectedRouteProps
 */
interface ClienteProtectedRouteProps {
  /** Contenido a renderizar si el cliente está autenticado */
  children: React.ReactNode;
}

/**
 * Componente de ruta protegida exclusiva para clientes (propietarios).
 *
 * Verifica que el usuario autenticado sea un cliente (propietario de mascotas)
 * antes de renderizar el contenido. Diferente de ProtectedRoute que es para
 * usuarios del sistema (veterinarios, recepcionistas, administradores).
 *
 * @component
 *
 * @param {ClienteProtectedRouteProps} props - Propiedades del componente
 * @param {React.ReactNode} props.children - Contenido protegido para clientes
 *
 * @returns {JSX.Element} El contenido protegido, skeleton de carga o redirección a login
 *
 * @example
 * ```tsx
 * // Ruta protegida para portal de clientes
 * <ClienteProtectedRoute>
 *   <MisMascotas />
 * </ClienteProtectedRoute>
 * ```
 *
 * @see {@link useAuth}
 * @see {@link ProtectedRoute}
 */
export function ClienteProtectedRoute({ children }: ClienteProtectedRouteProps) {
  const { cliente, userType, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="space-y-4 w-full max-w-md">
          <Skeleton className="h-12 w-full" />
          <Skeleton className="h-64 w-full" />
        </div>
      </div>
    );
  }

  if (!cliente || userType !== 'CLIENTE') {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

