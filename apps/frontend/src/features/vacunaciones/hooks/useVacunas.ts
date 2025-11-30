import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { vacunaService, VacunaDTO, VacunaSearchParams } from '../services/vacunaService';
import { Vacuna } from '@core/types';
import { useApiError } from '@shared/hooks/useApiError';

export function useVacunas() {
  return useQuery({
    queryKey: ['vacunas'],
    queryFn: () => vacunaService.getAll(),
  });
}

export function useVacunasActivas() {
  return useQuery({
    queryKey: ['vacunas', 'activas'],
    queryFn: () => vacunaService.getActivas(),
  });
}

export function useVacunasByEspecie(especie: string) {
  return useQuery({
    queryKey: ['vacunas', 'especie', especie],
    queryFn: () => vacunaService.getByEspecie(especie),
    enabled: !!especie,
  });
}

export function useVacuna(id: string | undefined) {
  return useQuery({
    queryKey: ['vacunas', id],
    queryFn: () => vacunaService.getById(id!),
    enabled: !!id,
  });
}

export function useVacunasSearch(params: VacunaSearchParams) {
  return useQuery({
    queryKey: ['vacunas', 'search', params],
    queryFn: () => vacunaService.searchWithFilters(params),
  });
}

export function useCreateVacuna() {
  const queryClient = useQueryClient();
  const { handleError, showSuccess } = useApiError();

  return useMutation({
    mutationFn: (dto: VacunaDTO) => vacunaService.create(dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vacunas'] });
      showSuccess('Vacuna creada exitosamente');
    },
    onError: handleError,
  });
}

export function useUpdateVacuna() {
  const queryClient = useQueryClient();
  const { handleError, showSuccess } = useApiError();

  return useMutation({
    mutationFn: ({ id, dto }: { id: string; dto: VacunaDTO }) =>
      vacunaService.update(id, dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vacunas'] });
      showSuccess('Vacuna actualizada exitosamente');
    },
    onError: handleError,
  });
}

export function useDeleteVacuna() {
  const queryClient = useQueryClient();
  const { handleError, showSuccess } = useApiError();

  return useMutation({
    mutationFn: (id: string) => vacunaService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vacunas'] });
      showSuccess('Vacuna eliminada exitosamente');
    },
    onError: handleError,
  });
}

