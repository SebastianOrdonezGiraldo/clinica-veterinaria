import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { consultaService, ConsultaDTO, ConsultaSearchParams } from '../services/consultaService';
import { Consulta, PageResponse } from '@core/types';
import { useApiError } from '@shared/hooks/useApiError';

/**
 * Hook personalizado para gestionar consultas médicas con React Query.
 *
 * Proporciona funcionalidades CRUD para consultas veterinarias con:
 * - Cache automático de datos
 * - Invalidación de cache de pacientes relacionados
 * - Manejo de estados de carga y error
 *
 * @hook
 *
 * @param {ConsultaSearchParams} params - Parámetros de búsqueda y paginación
 * @param {number} [params.page=0] - Número de página
 * @param {number} [params.size=10] - Elementos por página
 * @param {string} [params.pacienteId] - Filtrar por paciente
 * @param {string} [params.veterinarioId] - Filtrar por veterinario
 *
 * @returns {Object} Estado y funciones de consultas
 * @returns {PageResponse<Consulta>} returns.consultasPage - Respuesta paginada
 * @returns {Consulta[]} returns.consultas - Array de consultas
 * @returns {boolean} returns.isLoading - Si está cargando
 * @returns {Function} returns.createConsulta - Crear consulta
 * @returns {Function} returns.updateConsulta - Actualizar consulta
 * @returns {Function} returns.deleteConsulta - Eliminar consulta
 *
 * @example
 * ```tsx
 * function ConsultasList() {
 *   const { consultas, isLoading, createConsulta } = useConsultas();
 *
 *   const handleCreate = (data: ConsultaDTO) => {
 *     createConsulta(data);
 *   };
 *
 *   return <ConsultaForm onSubmit={handleCreate} />;
 * }
 * ```
 *
 * @see {@link useConsulta}
 * @see {@link useConsultasByPaciente}
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
 * Hook para obtener una consulta por ID.
 *
 * @hook
 *
 * @param {string | undefined} id - ID de la consulta
 *
 * @returns {Object} Consulta y estado de carga
 * @returns {Consulta} returns.consulta - Datos de la consulta
 * @returns {boolean} returns.isLoading - Si está cargando
 * @returns {Function} returns.refetch - Recargar datos
 *
 * @example
 * ```tsx
 * function ConsultaDetalle({ id }: { id: string }) {
 *   const { consulta, isLoading } = useConsulta(id);
 *
 *   if (isLoading) return <Skeleton />;
 *   if (!consulta) return <NotFound />;
 *
 *   return <ConsultaView consulta={consulta} />;
 * }
 * ```
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
 * Hook para obtener consultas de un paciente (historia clínica).
 *
 * Carga todas las consultas de un paciente ordenadas por fecha descendente.
 * Útil para mostrar la historia clínica completa.
 *
 * @hook
 *
 * @param {string | undefined} pacienteId - ID del paciente
 *
 * @returns {Object} Consultas del paciente y estado
 * @returns {Consulta[]} returns.consultas - Array de consultas del paciente
 * @returns {boolean} returns.isLoading - Si está cargando
 * @returns {Function} returns.refetch - Recargar datos
 *
 * @example
 * ```tsx
 * function HistoriaClinica({ pacienteId }: { pacienteId: string }) {
 *   const { consultas, isLoading } = useConsultasByPaciente(pacienteId);
 *
 *   return (
 *     <Timeline>
 *       {consultas.map(c => <ConsultaItem key={c.id} consulta={c} />)}
 *     </Timeline>
 *   );
 * }
 * ```
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

