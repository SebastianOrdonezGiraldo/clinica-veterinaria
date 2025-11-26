import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { consultaService, ConsultaDTO, ConsultaSearchParams } from '../services/consultaService';
import { Consulta, PageResponse } from '@core/types';
import { useApiError } from '@shared/hooks/useApiError';

/**
 * Hook personalizado para gestionar consultas con React Query
 */
export function useConsultas(params: ConsultaSearchParams = {}) {
  const { handleError, showSuccess } = useApiError();
  const queryClient = useQueryClient();

  const {
    data: consultasPage,
    isLoading,
    error,
    refetch,
  } = useQuery<PageResponse<Consulta>>({
    queryKey: ['consultas', params],
    queryFn: () => consultaService.searchWithFilters(params),
    staleTime: 30000, // 30 segundos
    gcTime: 5 * 60 * 1000, // 5 minutos
  });

  const createMutation = useMutation({
    mutationFn: (data: ConsultaDTO) => consultaService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['consultas'] });
      queryClient.invalidateQueries({ queryKey: ['paciente'] }); // Invalidar cache del paciente también
      showSuccess('Consulta creada exitosamente');
    },
    onError: handleError,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: ConsultaDTO }) =>
      consultaService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['consultas'] });
      queryClient.invalidateQueries({ queryKey: ['consulta'] });
      queryClient.invalidateQueries({ queryKey: ['paciente'] });
      showSuccess('Consulta actualizada exitosamente');
    },
    onError: handleError,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => consultaService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['consultas'] });
      queryClient.invalidateQueries({ queryKey: ['paciente'] });
      showSuccess('Consulta eliminada exitosamente');
    },
    onError: handleError,
  });

  return {
    consultasPage,
    consultas: consultasPage?.content || [],
    isLoading,
    error,
    refetch,
    createConsulta: createMutation.mutate,
    updateConsulta: updateMutation.mutate,
    deleteConsulta: deleteMutation.mutate,
    isCreating: createMutation.isPending,
    isUpdating: updateMutation.isPending,
    isDeleting: deleteMutation.isPending,
  };
}

/**
 * Hook para obtener una consulta por ID
 */
export function useConsulta(id: string | undefined) {
  const { handleError } = useApiError();

  const {
    data: consulta,
    isLoading,
    error,
    refetch,
  } = useQuery<Consulta>({
    queryKey: ['consulta', id],
    queryFn: () => {
      if (!id) throw new Error('ID es requerido');
      return consultaService.getById(id);
    },
    enabled: !!id,
    staleTime: 60000, // 1 minuto
  });

  return {
    consulta,
    isLoading,
    error,
    refetch,
  };
}

/**
 * Hook para obtener consultas de un paciente (historia clínica)
 */
export function useConsultasByPaciente(pacienteId: string | undefined) {
  const { handleError } = useApiError();

  const {
    data: consultasPage,
    isLoading,
    error,
    refetch,
  } = useQuery<PageResponse<Consulta>>({
    queryKey: ['consultas', 'paciente', pacienteId],
    queryFn: () => consultaService.searchWithFilters({
      pacienteId,
      page: 0,
      size: 100, // Cargar todas las consultas del paciente
      sort: 'fecha,desc',
    }),
    enabled: !!pacienteId,
    staleTime: 60000, // 1 minuto
  });

  return {
    consultasPage,
    consultas: consultasPage?.content || [],
    isLoading,
    error,
    refetch,
  };
}

