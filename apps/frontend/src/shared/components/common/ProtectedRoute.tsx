import { Navigate } from 'react-router-dom';
import { useAuth } from '@core/auth/AuthContext';
import { Rol } from '@core/types';

/**
 * Propiedades del componente ProtectedRoute.
 *
 * @interface ProtectedRouteProps
 */
interface ProtectedRouteProps {
  /** Contenido a renderizar si está autenticado y autorizado */
  children: React.ReactNode;
  /** Roles permitidos para acceder a la ruta (opcional) */
  allowedRoles?: Rol[];
}

/**
 * Componente de ruta protegida para usuarios del sistema.
 *
 * Verifica autenticación y autorización basada en roles antes de renderizar
 * el contenido. Redirige a login si no está autenticado o muestra mensaje
 * de acceso denegado si el rol no está permitido.
 *
 * @component
 *
 * @param {ProtectedRouteProps} props - Propiedades del componente
 * @param {React.ReactNode} props.children - Contenido protegido
 * @param {Rol[]} [props.allowedRoles] - Roles permitidos (si no se especifica, cualquier usuario autenticado puede acceder)
 *
 * @returns {JSX.Element} El contenido protegido, spinner de carga, redirección o mensaje de acceso denegado
 *
 * @example
 * ```tsx
 * // Ruta protegida para cualquier usuario autenticado
 * <ProtectedRoute>
 *   <Dashboard />
 * </ProtectedRoute>
 *
 * // Ruta solo para administradores
 * <ProtectedRoute allowedRoles={['ADMIN']}>
 *   <AdminPanel />
 * </ProtectedRoute>
 *
 * // Ruta para veterinarios y administradores
 * <ProtectedRoute allowedRoles={['VETERINARIO', 'ADMIN']}>
 *   <VetModule />
 * </ProtectedRoute>
 * ```
 *
 * @see {@link useAuth}
 * @see {@link ClienteProtectedRoute}
 */
export function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.rol)) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-background">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-foreground mb-2">Acceso Denegado</h1>
          <p className="text-muted-foreground">No tienes permisos para acceder a esta página</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
