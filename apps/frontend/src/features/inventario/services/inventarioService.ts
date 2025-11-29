import axios from '@core/api/axios';

/**
 * Tipos para el módulo de inventario
 */
export interface CategoriaProducto {
  id: number;
  nombre: string;
  descripcion?: string;
  activo: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Proveedor {
  id: number;
  nombre: string;
  ruc?: string;
  email?: string;
  telefono?: string;
  direccion?: string;
  notas?: string;
  activo: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface Producto {
  id: number;
  nombre: string;
  codigo: string;
  descripcion?: string;
  categoriaId: number;
  categoriaNombre?: string;
  unidadMedida: string;
  stockActual: number;
  stockMinimo?: number;
  stockMaximo?: number;
  costo: number;
  precioVenta?: number;
  activo: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export type TipoMovimiento = 'ENTRADA' | 'SALIDA' | 'AJUSTE';

export interface MovimientoInventario {
  id: number;
  productoId: number;
  productoNombre?: string;
  productoCodigo?: string;
  tipo: TipoMovimiento;
  cantidad: number;
  precioUnitario?: number;
  motivo: string;
  usuarioId: number;
  usuarioNombre?: string;
  proveedorId?: number;
  proveedorNombre?: string;
  stockAnterior?: number;
  stockResultante?: number;
  notas?: string;
  fecha: string;
}

/**
 * Servicio para gestión de categorías de productos
 */
export const categoriaService = {
  /**
   * Obtiene todas las categorías activas
   */
  async getAll(): Promise<CategoriaProducto[]> {
    const response = await axios.get<CategoriaProducto[]>('/inventario/categorias');
    return response.data;
  },

  /**
   * Obtiene todas las categorías (activas e inactivas)
   */
  async getAllIncludingInactivas(): Promise<CategoriaProducto[]> {
    const response = await axios.get<CategoriaProducto[]>('/inventario/categorias/all');
    return response.data;
  },

  /**
   * Obtiene una categoría por ID
   */
  async getById(id: number): Promise<CategoriaProducto> {
    const response = await axios.get<CategoriaProducto>(`/inventario/categorias/${id}`);
    return response.data;
  },

  /**
   * Crea una nueva categoría
   */
  async create(data: Omit<CategoriaProducto, 'id' | 'createdAt' | 'updatedAt'>): Promise<CategoriaProducto> {
    const response = await axios.post<CategoriaProducto>('/inventario/categorias', data);
    return response.data;
  },

  /**
   * Actualiza una categoría
   */
  async update(id: number, data: Partial<CategoriaProducto>): Promise<CategoriaProducto> {
    const response = await axios.put<CategoriaProducto>(`/inventario/categorias/${id}`, data);
    return response.data;
  },

  /**
   * Desactiva una categoría (soft delete)
   */
  async delete(id: number): Promise<void> {
    await axios.delete(`/inventario/categorias/${id}`);
  },
};

/**
 * Servicio para gestión de proveedores
 */
export const proveedorService = {
  /**
   * Obtiene todos los proveedores activos
   */
  async getAll(): Promise<Proveedor[]> {
    const response = await axios.get<Proveedor[]>('/inventario/proveedores');
    return response.data;
  },

  /**
   * Obtiene todos los proveedores (activos e inactivos)
   */
  async getAllIncludingInactivos(): Promise<Proveedor[]> {
    const response = await axios.get<Proveedor[]>('/inventario/proveedores/all');
    return response.data;
  },

  /**
   * Obtiene un proveedor por ID
   */
  async getById(id: number): Promise<Proveedor> {
    const response = await axios.get<Proveedor>(`/inventario/proveedores/${id}`);
    return response.data;
  },

  /**
   * Busca proveedores por nombre
   */
  async buscarPorNombre(nombre: string, soloActivos: boolean = true): Promise<Proveedor[]> {
    const response = await axios.get<Proveedor[]>('/inventario/proveedores/buscar', {
      params: { nombre, soloActivos },
    });
    return response.data;
  },

  /**
   * Crea un nuevo proveedor
   */
  async create(data: Omit<Proveedor, 'id' | 'createdAt' | 'updatedAt'>): Promise<Proveedor> {
    const response = await axios.post<Proveedor>('/inventario/proveedores', data);
    return response.data;
  },

  /**
   * Actualiza un proveedor
   */
  async update(id: number, data: Partial<Proveedor>): Promise<Proveedor> {
    const response = await axios.put<Proveedor>(`/inventario/proveedores/${id}`, data);
    return response.data;
  },

  /**
   * Desactiva un proveedor (soft delete)
   */
  async delete(id: number): Promise<void> {
    await axios.delete(`/inventario/proveedores/${id}`);
  },
};

/**
 * Servicio para gestión de productos
 */
export const productoService = {
  /**
   * Obtiene todos los productos activos
   */
  async getAll(): Promise<Producto[]> {
    const response = await axios.get<Producto[]>('/inventario/productos');
    return response.data;
  },

  /**
   * Obtiene todos los productos (activos e inactivos)
   */
  async getAllIncludingInactivos(): Promise<Producto[]> {
    const response = await axios.get<Producto[]>('/inventario/productos/all');
    return response.data;
  },

  /**
   * Obtiene un producto por ID
   */
  async getById(id: number): Promise<Producto> {
    const response = await axios.get<Producto>(`/inventario/productos/${id}`);
    return response.data;
  },

  /**
   * Busca un producto por código
   */
  async getByCodigo(codigo: string): Promise<Producto | null> {
    try {
      const response = await axios.get<Producto>(`/inventario/productos/codigo/${codigo}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  /**
   * Busca productos por nombre o código
   */
  async buscar(nombre?: string, codigo?: string, soloActivos: boolean = true): Promise<Producto[]> {
    const params: any = { soloActivos };
    if (nombre) params.nombre = nombre;
    if (codigo) params.codigo = codigo;
    
    const response = await axios.get<Producto[]>('/inventario/productos/buscar', { params });
    return response.data;
  },

  /**
   * Obtiene productos por categoría
   */
  async getByCategoria(categoriaId: number, soloActivos: boolean = true): Promise<Producto[]> {
    const response = await axios.get<Producto[]>(`/inventario/productos/categoria/${categoriaId}`, {
      params: { soloActivos },
    });
    return response.data;
  },

  /**
   * Obtiene productos con stock bajo
   */
  async getStockBajo(): Promise<Producto[]> {
    const response = await axios.get<Producto[]>('/inventario/productos/stock-bajo');
    return response.data;
  },

  /**
   * Obtiene productos con sobrestock
   */
  async getSobrestock(): Promise<Producto[]> {
    const response = await axios.get<Producto[]>('/inventario/productos/sobrestock');
    return response.data;
  },

  /**
   * Calcula el valor total del inventario
   */
  async getValorTotal(): Promise<number> {
    const response = await axios.get<number>('/inventario/productos/valor-total');
    return response.data;
  },

  /**
   * Crea un nuevo producto
   */
  async create(data: Omit<Producto, 'id' | 'createdAt' | 'updatedAt' | 'categoriaNombre'>): Promise<Producto> {
    const response = await axios.post<Producto>('/inventario/productos', data);
    return response.data;
  },

  /**
   * Actualiza un producto
   */
  async update(id: number, data: Partial<Producto>): Promise<Producto> {
    const response = await axios.put<Producto>(`/inventario/productos/${id}`, data);
    return response.data;
  },

  /**
   * Desactiva un producto (soft delete)
   */
  async delete(id: number): Promise<void> {
    await axios.delete(`/inventario/productos/${id}`);
  },
};

/**
 * Servicio para gestión de movimientos de inventario
 */
export const movimientoService = {
  /**
   * Obtiene movimientos de un producto
   */
  async getByProducto(productoId: number): Promise<MovimientoInventario[]> {
    const response = await axios.get<MovimientoInventario[]>(`/inventario/movimientos/producto/${productoId}`);
    return response.data;
  },

  /**
   * Obtiene movimientos por tipo
   */
  async getByTipo(tipo: TipoMovimiento): Promise<MovimientoInventario[]> {
    const response = await axios.get<MovimientoInventario[]>(`/inventario/movimientos/tipo/${tipo}`);
    return response.data;
  },

  /**
   * Obtiene movimientos en un rango de fechas
   */
  async getByFechaRange(fechaInicio: string, fechaFin: string): Promise<MovimientoInventario[]> {
    const response = await axios.get<MovimientoInventario[]>('/inventario/movimientos/fecha', {
      params: { fechaInicio, fechaFin },
    });
    return response.data;
  },

  /**
   * Obtiene el historial de un producto en un rango de fechas
   */
  async getHistorialProducto(productoId: number, fechaInicio: string, fechaFin: string): Promise<MovimientoInventario[]> {
    const response = await axios.get<MovimientoInventario[]>('/inventario/movimientos/historial', {
      params: { productoId, fechaInicio, fechaFin },
    });
    return response.data;
  },

  /**
   * Registra una entrada de inventario
   */
  async registrarEntrada(data: Omit<MovimientoInventario, 'id' | 'fecha' | 'productoNombre' | 'productoCodigo' | 'usuarioNombre' | 'proveedorNombre' | 'stockAnterior' | 'stockResultante'>): Promise<MovimientoInventario> {
    const response = await axios.post<MovimientoInventario>('/inventario/movimientos/entrada', data);
    return response.data;
  },

  /**
   * Registra una salida de inventario
   */
  async registrarSalida(data: Omit<MovimientoInventario, 'id' | 'fecha' | 'productoNombre' | 'productoCodigo' | 'usuarioNombre' | 'proveedorNombre' | 'stockAnterior' | 'stockResultante'>): Promise<MovimientoInventario> {
    const response = await axios.post<MovimientoInventario>('/inventario/movimientos/salida', data);
    return response.data;
  },

  /**
   * Registra un ajuste de inventario
   */
  async registrarAjuste(data: Omit<MovimientoInventario, 'id' | 'fecha' | 'productoNombre' | 'productoCodigo' | 'usuarioNombre' | 'proveedorNombre' | 'stockAnterior' | 'stockResultante'>): Promise<MovimientoInventario> {
    const response = await axios.post<MovimientoInventario>('/inventario/movimientos/ajuste', data);
    return response.data;
  },
};

