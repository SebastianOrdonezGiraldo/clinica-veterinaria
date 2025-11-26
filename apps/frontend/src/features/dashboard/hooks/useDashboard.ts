import { useQuery } from '@tanstack/react-query';
import { dashboardService, DashboardStats } from '../services/dashboardService';
import { useApiError } from '@shared/hooks/useApiError';

/**
 * Hook personalizado para el dashboard con React Query
 * 
 * Características:
 * - Refetch automático cada 30 segundos para datos actualizados
 * - Cache de 15 segundos para evitar llamadas innecesarias
 * - Manejo automático de errores
 */
export function useDashboard() {
  const { handleError } = useApiError();

  const {
    data: stats,
    isLoading,
    error,
    refetch,
  } = useQuery<DashboardStats>({
    queryKey: ['dashboard', 'stats'],
    queryFn: () => dashboardService.getStats(),
    staleTime: 15000, // 15 segundos - datos frescos
    gcTime: 2 * 60 * 1000, // 2 minutos en cache
    refetchInterval: 30000, // Refetch automático cada 30 segundos
    refetchOnWindowFocus: true, // Refetch cuando la ventana recupera el foco
  });

  if (error) {
    handleError(error);
  }

  return {
    stats,
    isLoading,
    error,
    refetch,
  };
}

