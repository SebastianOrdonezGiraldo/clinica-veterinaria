import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { prescripcionService, PrescripcionDTO, PrescripcionSearchParams } from '../services/prescripcionService';
import { Prescripcion, PageResponse } from '@core/types';
import { useApiError } from '@shared/hooks/useApiError';

/**
 * Hook personalizado para gestionar prescripciones con React Query
 */
export function usePrescripciones(params: PrescripcionSearchParams = {}) {
  const { handleError, showSuccess } = useApiError();
  const queryClient = useQueryClient();

  const {
    data: prescripcionesPage,
    isLoading,
    error,
    refetch,
  } = useQuery<PageResponse<Prescripcion>>({
    queryKey: ['prescripciones', params],
    queryFn: () => prescripcionService.searchWithFilters(params),
    staleTime: 30000, // 30 segundos
    gcTime: 5 * 60 * 1000, // 5 minutos
  });

  const createMutation = useMutation({
    mutationFn: (data: PrescripcionDTO) => prescripcionService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['prescripciones'] });
      showSuccess('Prescripción creada exitosamente');
    },
    onError: handleError,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: PrescripcionDTO }) =>
      prescripcionService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['prescripciones'] });
      queryClient.invalidateQueries({ queryKey: ['prescripcion'] });
      showSuccess('Prescripción actualizada exitosamente');
    },
    onError: handleError,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => prescripcionService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['prescripciones'] });
      showSuccess('Prescripción eliminada exitosamente');
    },
    onError: handleError,
  });

  return {
    prescripcionesPage,
    prescripciones: prescripcionesPage?.content || [],
    isLoading,
    error,
    refetch,
    createPrescripcion: createMutation.mutate,
    updatePrescripcion: updateMutation.mutate,
    deletePrescripcion: deleteMutation.mutate,
    isCreating: createMutation.isPending,
    isUpdating: updateMutation.isPending,
    isDeleting: deleteMutation.isPending,
  };
}

/**
 * Hook para obtener una prescripción por ID
 */
export function usePrescripcion(id: string | undefined) {
  const { handleError } = useApiError();

  const {
    data: prescripcion,
    isLoading,
    error,
    refetch,
  } = useQuery<Prescripcion>({
    queryKey: ['prescripcion', id],
    queryFn: () => {
      if (!id) throw new Error('ID es requerido');
      return prescripcionService.getById(id);
    },
    enabled: !!id,
    staleTime: 60000, // 1 minuto
  });

  return {
    prescripcion,
    isLoading,
    error,
    refetch,
  };
}

