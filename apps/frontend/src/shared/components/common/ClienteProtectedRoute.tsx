import { Navigate } from 'react-router-dom';
import { useAuth } from '@core/auth/AuthContext';
import { Skeleton } from '@shared/components/ui/skeleton';

interface ClienteProtectedRouteProps {
  children: React.ReactNode;
}

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

