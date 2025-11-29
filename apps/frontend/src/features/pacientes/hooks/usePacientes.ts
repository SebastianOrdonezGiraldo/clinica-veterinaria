import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { pacienteService, PacienteDTO, PacienteSearchParams } from '../services/pacienteService';
import { Paciente, PageResponse } from '@core/types';
import { useApiError } from '@shared/hooks/useApiError';

/**
 * Hook personalizado para gestionar pacientes con React Query.
 *
 * Proporciona funcionalidades CRUD completas para pacientes (mascotas) con:
 * - Cache automático de datos (30 segundos staleTime)
 * - Refetch automático en background
 * - Invalidación de cache después de mutaciones
 * - Manejo de estados de carga y error integrado
 *
 * @hook
 *
 * @param {PacienteSearchParams} params - Parámetros de búsqueda y paginación
 * @param {number} [params.page=0] - Número de página (0-indexed)
 * @param {number} [params.size=10] - Elementos por página
 * @param {string} [params.nombre] - Filtrar por nombre
 * @param {string} [params.especie] - Filtrar por especie
 * @param {string} [params.propietarioId] - Filtrar por propietario
 *
 * @returns {Object} Estado y funciones de pacientes
 * @returns {PageResponse<Paciente>} returns.pacientesPage - Respuesta paginada completa
 * @returns {Paciente[]} returns.pacientes - Array de pacientes
 * @returns {boolean} returns.isLoading - Si está cargando datos
 * @returns {Error} returns.error - Error si ocurrió
 * @returns {Function} returns.refetch - Función para recargar datos
 * @returns {Function} returns.createPaciente - Función para crear paciente
 * @returns {Function} returns.updatePaciente - Función para actualizar paciente
 * @returns {Function} returns.deletePaciente - Función para eliminar paciente
 * @returns {boolean} returns.isCreating - Si está creando
 * @returns {boolean} returns.isUpdating - Si está actualizando
 * @returns {boolean} returns.isDeleting - Si está eliminando
 *
 * @example
 * ```tsx
 * function PacientesList() {
 *   const {
 *     pacientes,
 *     isLoading,
 *     deletePaciente,
 *     isDeleting
 *   } = usePacientes({ page: 0, size: 10, especie: 'Canino' });
 *
 *   if (isLoading) return <LoadingCards />;
 *
 *   return (
 *     <div className="grid gap-4">
 *       {pacientes.map(p => (
 *         <PacienteCard
 *           key={p.id}
 *           paciente={p}
 *           onDelete={deletePaciente}
 *         />
 *       ))}
 *     </div>
 *   );
 * }
 * ```
 *
 * @see {@link usePaciente}
 * @see {@link useAllPacientes}
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
 * Hook para obtener un paciente por ID.
 *
 * Útil para páginas de detalle donde se necesita un paciente específico.
 * Solo ejecuta la query si hay un ID válido.
 *
 * @hook
 *
 * @param {string | undefined} id - ID del paciente
 *
 * @returns {Object} Paciente y estado de carga
 * @returns {Paciente} returns.paciente - Datos del paciente
 * @returns {boolean} returns.isLoading - Si está cargando
 * @returns {Error} returns.error - Error si ocurrió
 * @returns {Function} returns.refetch - Recargar datos
 *
 * @example
 * ```tsx
 * function PacienteDetalle({ id }: { id: string }) {
 *   const { paciente, isLoading, error } = usePaciente(id);
 *
 *   if (isLoading) return <Skeleton />;
 *   if (error) return <ErrorState />;
 *   if (!paciente) return <NotFound />;
 *
 *   return <PacienteView paciente={paciente} />;
 * }
 * ```
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
 * Hook para obtener todos los pacientes sin paginación.
 *
 * Útil para dropdowns y selects donde se necesitan todos los pacientes.
 * Tiene un staleTime de 1 minuto para minimizar peticiones.
 *
 * @hook
 *
 * @returns {Object} Pacientes y estado de carga
 * @returns {Paciente[]} returns.pacientes - Array de todos los pacientes
 * @returns {boolean} returns.isLoading - Si está cargando
 * @returns {Error} returns.error - Error si ocurrió
 *
 * @example
 * ```tsx
 * function PacienteSelect({ onSelect }: { onSelect: (id: string) => void }) {
 *   const { pacientes, isLoading } = useAllPacientes();
 *
 *   if (isLoading) return <Skeleton />;
 *
 *   return (
 *     <Select onValueChange={onSelect}>
 *       <SelectTrigger>
 *         <SelectValue placeholder="Seleccionar paciente" />
 *       </SelectTrigger>
 *       <SelectContent>
 *         {pacientes.map(p => (
 *           <SelectItem key={p.id} value={p.id}>
 *             {p.nombre} ({p.especie})
 *           </SelectItem>
 *         ))}
 *       </SelectContent>
 *     </Select>
 *   );
 * }
 * ```
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

