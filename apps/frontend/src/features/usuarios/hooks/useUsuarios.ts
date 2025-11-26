import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { usuarioService, UsuarioCreateDTO, UsuarioUpdateDTO, UsuarioSearchParams } from '../services/usuarioService';
import { Usuario, PageResponse } from '@core/types';
import { useApiError } from '@shared/hooks/useApiError';

/**
 * Hook personalizado para gestionar usuarios con React Query
 */
export function useUsuarios(params: UsuarioSearchParams = {}) {
  const { handleError, showSuccess } = useApiError();
  const queryClient = useQueryClient();

  const {
    data: usuariosPage,
    isLoading,
    error,
    refetch,
  } = useQuery<PageResponse<Usuario>>({
    queryKey: ['usuarios', params],
    queryFn: () => usuarioService.searchWithFilters(params),
    staleTime: 30000, // 30 segundos
    gcTime: 5 * 60 * 1000, // 5 minutos
  });

  const createMutation = useMutation({
    mutationFn: (data: UsuarioCreateDTO) => usuarioService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      showSuccess('Usuario creado exitosamente');
    },
    onError: handleError,
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: UsuarioUpdateDTO }) =>
      usuarioService.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      queryClient.invalidateQueries({ queryKey: ['usuario'] });
      showSuccess('Usuario actualizado exitosamente');
    },
    onError: handleError,
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => usuarioService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      showSuccess('Usuario eliminado exitosamente');
    },
    onError: handleError,
  });

  const resetPasswordMutation = useMutation({
    mutationFn: ({ id, password }: { id: string; password: string }) =>
      usuarioService.resetPassword(id, password),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      queryClient.invalidateQueries({ queryKey: ['usuario'] });
      showSuccess('Contraseña restablecida exitosamente');
    },
    onError: handleError,
  });

  return {
    usuariosPage,
    usuarios: usuariosPage?.content || [],
    isLoading,
    error,
    refetch,
    createUsuario: createMutation.mutate,
    updateUsuario: updateMutation.mutate,
    deleteUsuario: deleteMutation.mutate,
    resetPassword: resetPasswordMutation.mutate,
    isCreating: createMutation.isPending,
    isUpdating: updateMutation.isPending,
    isDeleting: deleteMutation.isPending,
    isResettingPassword: resetPasswordMutation.isPending,
  };
}

/**
 * Hook para obtener un usuario por ID
 */
export function useUsuario(id: string | undefined) {
  const { handleError } = useApiError();

  const {
    data: usuario,
    isLoading,
    error,
    refetch,
  } = useQuery<Usuario>({
    queryKey: ['usuario', id],
    queryFn: () => {
      if (!id) throw new Error('ID es requerido');
      return usuarioService.getById(id);
    },
    enabled: !!id,
    staleTime: 60000, // 1 minuto
  });

  return {
    usuario,
    isLoading,
    error,
    refetch,
  };
}

/**
 * Hook para obtener todos los usuarios (sin paginación)
 * Útil para dropdowns y selecciones
 */
export function useAllUsuarios() {
  const { handleError } = useApiError();

  const {
    data: usuarios = [],
    isLoading,
    error,
    refetch,
  } = useQuery<Usuario[]>({
    queryKey: ['usuarios', 'all'],
    queryFn: () => usuarioService.getAll(),
    staleTime: 60000, // 1 minuto
    onError: handleError,
  });

  return {
    usuarios,
    isLoading,
    error,
    refetch,
  };
}

/**
 * Hook para obtener veterinarios activos
 */
export function useVeterinarios() {
  const { handleError } = useApiError();

  const {
    data: veterinarios = [],
    isLoading,
    error,
    refetch,
  } = useQuery<Usuario[]>({
    queryKey: ['usuarios', 'veterinarios'],
    queryFn: async () => {
      const response = await usuarioService.getAll();
      return response.filter(u => u.rol === 'VET' && u.activo !== false);
    },
    staleTime: 60000, // 1 minuto
    onError: handleError,
  });

  return {
    veterinarios,
    isLoading,
    error,
    refetch,
  };
}

