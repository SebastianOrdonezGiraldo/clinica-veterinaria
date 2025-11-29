import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useApiError } from '@shared/hooks/useApiError';
import {
  categoriaService,
  proveedorService,
  productoService,
  movimientoService,
  CategoriaProducto,
  Proveedor,
  Producto,
  MovimientoInventario,
  TipoMovimiento,
} from '../services/inventarioService';

/**
 * Hook para gestión de categorías de productos
 */
export function useCategorias(includeInactivas: boolean = false) {
  return useQuery({
    queryKey: ['categorias', includeInactivas],
    queryFn: () => includeInactivas 
      ? categoriaService.getAllIncludingInactivas()
      : categoriaService.getAll(),
  });
}

export function useCategoria(id: number | null) {
  return useQuery({
    queryKey: ['categoria', id],
    queryFn: () => id ? categoriaService.getById(id) : null,
    enabled: !!id,
  });
}

export function useCreateCategoria() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: (data: Omit<CategoriaProducto, 'id' | 'createdAt' | 'updatedAt'>) =>
      categoriaService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categorias'] });
      toast.success('Categoría creada exitosamente');
    },
    onError: handleError,
  });
}

export function useUpdateCategoria() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<CategoriaProducto> }) =>
      categoriaService.update(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['categorias'] });
      queryClient.invalidateQueries({ queryKey: ['categoria', variables.id] });
      toast.success('Categoría actualizada exitosamente');
    },
    onError: handleError,
  });
}

export function useDeleteCategoria() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: (id: number) => categoriaService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categorias'] });
      toast.success('Categoría desactivada exitosamente');
    },
    onError: handleError,
  });
}

/**
 * Hook para gestión de proveedores
 */
export function useProveedores(includeInactivos: boolean = false) {
  return useQuery({
    queryKey: ['proveedores', includeInactivos],
    queryFn: () => includeInactivos
      ? proveedorService.getAllIncludingInactivos()
      : proveedorService.getAll(),
  });
}

export function useProveedor(id: number | null) {
  return useQuery({
    queryKey: ['proveedor', id],
    queryFn: () => id ? proveedorService.getById(id) : null,
    enabled: !!id,
  });
}

export function useCreateProveedor() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: (data: Omit<Proveedor, 'id' | 'createdAt' | 'updatedAt'>) =>
      proveedorService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['proveedores'] });
      toast.success('Proveedor creado exitosamente');
    },
    onError: handleError,
  });
}

export function useUpdateProveedor() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Proveedor> }) =>
      proveedorService.update(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['proveedores'] });
      queryClient.invalidateQueries({ queryKey: ['proveedor', variables.id] });
      toast.success('Proveedor actualizado exitosamente');
    },
    onError: handleError,
  });
}

export function useDeleteProveedor() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: (id: number) => proveedorService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['proveedores'] });
      toast.success('Proveedor desactivado exitosamente');
    },
    onError: handleError,
  });
}

/**
 * Hook para gestión de productos
 */
export function useProductos(includeInactivos: boolean = false) {
  return useQuery({
    queryKey: ['productos', includeInactivos],
    queryFn: () => includeInactivos
      ? productoService.getAllIncludingInactivos()
      : productoService.getAll(),
  });
}

export function useProducto(id: number | null) {
  return useQuery({
    queryKey: ['producto', id],
    queryFn: () => id ? productoService.getById(id) : null,
    enabled: !!id,
  });
}

export function useProductosStockBajo() {
  return useQuery({
    queryKey: ['productos', 'stock-bajo'],
    queryFn: () => productoService.getStockBajo(),
  });
}

export function useProductosSobrestock() {
  return useQuery({
    queryKey: ['productos', 'sobrestock'],
    queryFn: () => productoService.getSobrestock(),
  });
}

export function useValorTotalInventario() {
  return useQuery({
    queryKey: ['productos', 'valor-total'],
    queryFn: () => productoService.getValorTotal(),
  });
}

export function useCreateProducto() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: (data: Omit<Producto, 'id' | 'createdAt' | 'updatedAt' | 'categoriaNombre'>) =>
      productoService.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['productos'] });
      toast.success('Producto creado exitosamente');
    },
    onError: handleError,
  });
}

export function useUpdateProducto() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Producto> }) =>
      productoService.update(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['productos'] });
      queryClient.invalidateQueries({ queryKey: ['producto', variables.id] });
      toast.success('Producto actualizado exitosamente');
    },
    onError: handleError,
  });
}

export function useDeleteProducto() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: (id: number) => productoService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['productos'] });
      toast.success('Producto desactivado exitosamente');
    },
    onError: handleError,
  });
}

/**
 * Hook para gestión de movimientos de inventario
 */
export function useMovimientosByProducto(productoId: number | null) {
  return useQuery({
    queryKey: ['movimientos', 'producto', productoId],
    queryFn: () => productoId ? movimientoService.getByProducto(productoId) : [],
    enabled: !!productoId,
  });
}

export function useMovimientosByTipo(tipo: TipoMovimiento | null) {
  return useQuery({
    queryKey: ['movimientos', 'tipo', tipo],
    queryFn: () => tipo ? movimientoService.getByTipo(tipo) : [],
    enabled: !!tipo,
  });
}

export function useRegistrarEntrada() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: (data: Omit<MovimientoInventario, 'id' | 'fecha' | 'productoNombre' | 'productoCodigo' | 'usuarioNombre' | 'proveedorNombre' | 'stockAnterior' | 'stockResultante'>) =>
      movimientoService.registrarEntrada(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['movimientos'] });
      queryClient.invalidateQueries({ queryKey: ['productos'] });
      queryClient.invalidateQueries({ queryKey: ['producto', variables.productoId] });
      toast.success('Entrada registrada exitosamente');
    },
    onError: handleError,
  });
}

export function useRegistrarSalida() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: (data: Omit<MovimientoInventario, 'id' | 'fecha' | 'productoNombre' | 'productoCodigo' | 'usuarioNombre' | 'proveedorNombre' | 'stockAnterior' | 'stockResultante'>) =>
      movimientoService.registrarSalida(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['movimientos'] });
      queryClient.invalidateQueries({ queryKey: ['productos'] });
      queryClient.invalidateQueries({ queryKey: ['producto', variables.productoId] });
      toast.success('Salida registrada exitosamente');
    },
    onError: handleError,
  });
}

export function useRegistrarAjuste() {
  const queryClient = useQueryClient();
  const { handleError } = useApiError();

  return useMutation({
    mutationFn: (data: Omit<MovimientoInventario, 'id' | 'fecha' | 'productoNombre' | 'productoCodigo' | 'usuarioNombre' | 'proveedorNombre' | 'stockAnterior' | 'stockResultante'>) =>
      movimientoService.registrarAjuste(data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['movimientos'] });
      queryClient.invalidateQueries({ queryKey: ['productos'] });
      queryClient.invalidateQueries({ queryKey: ['producto', variables.productoId] });
      toast.success('Ajuste registrado exitosamente');
    },
    onError: handleError,
  });
}

