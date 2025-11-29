import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { propietarioService, PropietarioDTO, PropietarioSearchParams } from '../services/propietarioService';
import { Propietario, PageResponse } from '@core/types';
import { useApiError } from '@shared/hooks/useApiError';

/**
 * Hook personalizado para gestionar propietarios con React Query.
 *
 * Proporciona funcionalidades CRUD completas para propietarios con:
 * - Cache automático de datos
 * - Refetch automático en background
 * - Invalidación de cache después de mutaciones
 * - Manejo de estados de carga y error
 *
 * @hook
 *
 * @param {PropietarioSearchParams} params - Parámetros de búsqueda y paginación
 * @param {number} [params.page=0] - Número de página (0-indexed)
 * @param {number} [params.size=10] - Elementos por página
 * @param {string} [params.nombre] - Filtrar por nombre
 * @param {string} [params.documento] - Filtrar por documento
 *
 * @returns {Object} Estado y funciones de propietarios
 * @returns {PageResponse<Propietario>} returns.propietariosPage - Respuesta paginada completa
 * @returns {Propietario[]} returns.propietarios - Array de propietarios
 * @returns {boolean} returns.isLoading - Si está cargando
 * @returns {Function} returns.createPropietario - Crear propietario
 * @returns {Function} returns.updatePropietario - Actualizar propietario
 * @returns {Function} returns.deletePropietario - Eliminar propietario
 *
 * @example
 * ```tsx
 * function PropietariosList() {
 *   const {
 *     propietarios,
 *     isLoading,
 *     createPropietario,
 *     deletePropietario
 *   } = usePropietarios({ page: 0, size: 10 });
 *
 *   if (isLoading) return <LoadingCards />;
 *
 *   return (
 *     <div>
 *       {propietarios.map(p => <PropietarioCard key={p.id} propietario={p} />)}
 *     </div>
 *   );
 * }
 * ```
 *
 * @see {@link useAllPropietarios}
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
 * Hook para obtener todos los propietarios sin paginación.
 *
 * Útil para dropdowns y selects donde se necesitan todos los propietarios.
 * Tiene un staleTime de 1 minuto para minimizar peticiones.
 *
 * @hook
 *
 * @returns {Object} Propietarios y estado de carga
 * @returns {Propietario[]} returns.propietarios - Array de todos los propietarios
 * @returns {boolean} returns.isLoading - Si está cargando
 * @returns {Error} returns.error - Error si ocurrió
 *
 * @example
 * ```tsx
 * function PropietarioSelect() {
 *   const { propietarios, isLoading } = useAllPropietarios();
 *
 *   return (
 *     <Select>
 *       {propietarios.map(p => (
 *         <SelectItem key={p.id} value={p.id}>{p.nombre}</SelectItem>
 *       ))}
 *     </Select>
 *   );
 * }
 * ```
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

