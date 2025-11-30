import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { vacunacionService, VacunacionDTO, VacunacionSearchParams } from '../services/vacunacionService';
import { useApiError } from '@shared/hooks/useApiError';

export function useVacunaciones(params: VacunacionSearchParams = {}) {
  return useQuery({
    queryKey: ['vacunaciones', params],
    queryFn: () => vacunacionService.searchWithFilters(params),
  });
}

export function useVacunacion(id: string | undefined) {
  return useQuery({
    queryKey: ['vacunaciones', id],
    queryFn: () => vacunacionService.getById(id!),
    enabled: !!id,
  });
}

export function useVacunacionesByPaciente(pacienteId: string | undefined) {
  return useQuery({
    queryKey: ['vacunaciones', 'paciente', pacienteId],
    queryFn: () => vacunacionService.getByPaciente(pacienteId!),
    enabled: !!pacienteId,
  });
}

export function useVacunacionesProximas(dias: number = 30) {
  return useQuery({
    queryKey: ['vacunaciones', 'proximas', dias],
    queryFn: () => vacunacionService.getProximasAVencer(dias),
  });
}

export function useVacunacionesVencidas() {
  return useQuery({
    queryKey: ['vacunaciones', 'vencidas'],
    queryFn: () => vacunacionService.getVencidas(),
  });
}

export function useUltimaVacunacion(pacienteId: string | undefined, vacunaId: string | undefined) {
  return useQuery({
    queryKey: ['vacunaciones', 'ultima', pacienteId, vacunaId],
    queryFn: () => vacunacionService.getUltimaVacunacion(pacienteId!, vacunaId!),
    enabled: !!pacienteId && !!vacunaId,
  });
}

export function useCreateVacunacion() {
  const queryClient = useQueryClient();
  const { handleError, showSuccess } = useApiError();

  return useMutation({
    mutationFn: (dto: VacunacionDTO) => vacunacionService.create(dto),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['vacunaciones'] });
      queryClient.invalidateQueries({ queryKey: ['vacunaciones', 'paciente', variables.pacienteId] });
      queryClient.invalidateQueries({ queryKey: ['pacientes'] });
      showSuccess('Vacunación registrada exitosamente');
    },
    onError: handleError,
  });
}

export function useUpdateVacunacion() {
  const queryClient = useQueryClient();
  const { handleError, showSuccess } = useApiError();

  return useMutation({
    mutationFn: ({ id, dto }: { id: string; dto: VacunacionDTO }) =>
      vacunacionService.update(id, dto),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['vacunaciones'] });
      queryClient.invalidateQueries({ queryKey: ['vacunaciones', 'paciente', variables.dto.pacienteId] });
      showSuccess('Vacunación actualizada exitosamente');
    },
    onError: handleError,
  });
}

export function useDeleteVacunacion() {
  const queryClient = useQueryClient();
  const { handleError, showSuccess } = useApiError();

  return useMutation({
    mutationFn: (id: string) => vacunacionService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['vacunaciones'] });
      showSuccess('Vacunación eliminada exitosamente');
    },
    onError: handleError,
  });
}

