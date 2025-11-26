import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { propietarioService, PropietarioDTO, PropietarioSearchParams } from '../services/propietarioService';
import { Propietario, PageResponse } from '@core/types';
import { useApiError } from '@shared/hooks/useApiError';

/**
 * Hook personalizado para gestionar propietarios con React Query
 */
export function usePropietarios(params: PropietarioSearchParams = {}) {
  const { handleError, showSuccess } = useApiError();
  const queryClient = useQueryClient();

  const {
    data: propietariosPage,
    isLoading,
    error,
    refetch,
  } = useQuery<PageResponse<Propietario>>({
    queryKey: ['propietarios', params],
    queryFn: () => propietarioService.searchWithFilters(params),
    staleTime: 30000,
    gcTime: 5 * 60 * 1000,
  });

  const createMutation = useMutation({
    mutationFn: (data: PropietarioDTO) => propietarioService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['propietarios'] });
      showSuccess('Propietario creado exitosamente');
    },
    onError: handleError,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: PropietarioDTO }) =>
      propietarioService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['propietarios'] });
      queryClient.invalidateQueries({ queryKey: ['propietario'] });
      showSuccess('Propietario actualizado exitosamente');
    },
    onError: handleError,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => propietarioService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['propietarios'] });
      showSuccess('Propietario eliminado exitosamente');
    },
    onError: handleError,
  });

  return {
    propietariosPage,
    propietarios: propietariosPage?.content || [],
    isLoading,
    error,
    refetch,
    createPropietario: createMutation.mutate,
    updatePropietario: updateMutation.mutate,
    deletePropietario: deleteMutation.mutate,
    isCreating: createMutation.isPending,
    isUpdating: updateMutation.isPending,
    isDeleting: deleteMutation.isPending,
  };
}

/**
 * Hook para obtener todos los propietarios (sin paginación)
 */
export function useAllPropietarios() {
  const {
    data: propietarios = [],
    isLoading,
    error,
  } = useQuery<Propietario[]>({
    queryKey: ['propietarios', 'all'],
    queryFn: () => propietarioService.getAll(),
    staleTime: 60000,
  });

  return {
    propietarios,
    isLoading,
    error,
  };
}

