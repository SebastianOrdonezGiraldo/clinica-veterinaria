import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { pacienteService, PacienteDTO, PacienteSearchParams } from '../services/pacienteService';
import { Paciente, PageResponse } from '@core/types';
import { useApiError } from '@shared/hooks/useApiError';

/**
 * Hook personalizado para gestionar pacientes con React Query
 * 
 * Beneficios:
 * - Cache automático de datos
 * - Refetch automático en background
 * - Loading y error states manejados automáticamente
 * - Invalidación de cache después de mutaciones
 */
export function usePacientes(params: PacienteSearchParams = {}) {
  const { handleError, showSuccess } = useApiError();
  const queryClient = useQueryClient();

  // Query para obtener pacientes paginados
  const {
    data: pacientesPage,
    isLoading,
    error,
    refetch,
  } = useQuery<PageResponse<Paciente>>({
    queryKey: ['pacientes', params],
    queryFn: () => pacienteService.searchWithFilters(params),
    staleTime: 30000, // 30 segundos - los datos se consideran frescos
    gcTime: 5 * 60 * 1000, // 5 minutos - tiempo en cache
  });

  // Mutation para crear paciente
  const createMutation = useMutation({
    mutationFn: (data: PacienteDTO) => pacienteService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pacientes'] });
      showSuccess('Paciente creado exitosamente');
    },
    onError: handleError,
  });

  // Mutation para actualizar paciente
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: PacienteDTO }) =>
      pacienteService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pacientes'] });
      queryClient.invalidateQueries({ queryKey: ['paciente'] });
      showSuccess('Paciente actualizado exitosamente');
    },
    onError: handleError,
  });

  // Mutation para eliminar paciente
  const deleteMutation = useMutation({
    mutationFn: (id: string) => pacienteService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['pacientes'] });
      showSuccess('Paciente eliminado exitosamente');
    },
    onError: handleError,
  });

  return {
    pacientesPage,
    pacientes: pacientesPage?.content || [],
    isLoading,
    error,
    refetch,
    createPaciente: createMutation.mutate,
    updatePaciente: updateMutation.mutate,
    deletePaciente: deleteMutation.mutate,
    isCreating: createMutation.isPending,
    isUpdating: updateMutation.isPending,
    isDeleting: deleteMutation.isPending,
  };
}

/**
 * Hook para obtener un paciente por ID
 */
export function usePaciente(id: string | undefined) {
  const { handleError } = useApiError();

  const {
    data: paciente,
    isLoading,
    error,
    refetch,
  } = useQuery<Paciente>({
    queryKey: ['paciente', id],
    queryFn: () => {
      if (!id) throw new Error('ID es requerido');
      return pacienteService.getById(id);
    },
    enabled: !!id, // Solo ejecutar si hay ID
    staleTime: 60000, // 1 minuto
  });

  return {
    paciente,
    isLoading,
    error,
    refetch,
  };
}

/**
 * Hook para obtener todos los pacientes (sin paginación)
 * Útil para dropdowns y selects
 */
export function useAllPacientes() {
  const {
    data: pacientes = [],
    isLoading,
    error,
  } = useQuery<Paciente[]>({
    queryKey: ['pacientes', 'all'],
    queryFn: () => pacienteService.getAll(),
    staleTime: 60000, // 1 minuto
  });

  return {
    pacientes,
    isLoading,
    error,
  };
}

