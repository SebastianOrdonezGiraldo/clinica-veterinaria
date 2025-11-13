import axios from '@core/api/axios';
import { Propietario, PageResponse, PageParams } from '@core/types';

/**
 * DTO para crear/actualizar propietarios
 */
export interface PropietarioDTO {
  nombre: string;
  documento?: string;
  email?: string;
  telefono?: string;
  direccion?: string;
}

/**
 * Parámetros de búsqueda para propietarios con paginación
 */
export interface PropietarioSearchParams extends PageParams {
  nombre?: string;
  documento?: string;
  telefono?: string;
}

/**
 * Helper para normalizar IDs (convertir números a strings)
 * 
 * PATRÓN: Data Mapper
 * Transforma la representación del backend (IDs numéricos) 
 * a la representación del frontend (IDs string)
 */
const normalizePropietario = (propietario: any): Propietario => ({
  ...propietario,
  id: String(propietario.id),
});

/**
 * Servicio de propietarios - Capa de acceso a la API
 * 
 * PATRONES APLICADOS:
 * - Service Layer Pattern: Encapsula la lógica de comunicación con la API
 * - Adapter Pattern: Adapta las respuestas del backend al formato del frontend
 */
export const propietarioService = {
  /**
   * Obtiene todos los propietarios sin paginación
   * @deprecated Usar searchWithFilters para búsquedas paginadas
   */
  async getAll(): Promise<Propietario[]> {
    const response = await axios.get<any[]>('/propietarios');
    return response.data.map(normalizePropietario);
  },

  /**
   * Obtiene un propietario por su ID
   */
  async getById(id: string): Promise<Propietario> {
    const response = await axios.get<any>(`/propietarios/${id}`);
    return normalizePropietario(response.data);
  },

  /**
   * Busca propietarios por nombre sin paginación
   * @deprecated Usar searchWithFilters para búsquedas paginadas
   */
  async search(nombre: string): Promise<Propietario[]> {
    const response = await axios.get<any[]>('/propietarios/buscar', {
      params: { nombre },
    });
    return response.data.map(normalizePropietario);
  },
  
  /**
   * Busca propietarios con filtros combinados y paginación del lado del servidor.
   * 
   * <strong>VENTAJAS:</strong>
   * - ✓ Paginación en backend (eficiente para grandes datasets)
   * - ✓ Búsqueda multicritero (nombre, documento, teléfono)
   * - ✓ Ordenamiento configurable
   * - ✓ Solo carga los datos necesarios (reduce transferencia)
   * 
   * @param params Parámetros de búsqueda y paginación
   * @returns Respuesta paginada con propietarios y metadatos
   * 
   * @example
   * ```typescript
   * // Buscar por nombre con paginación
   * const result = await searchWithFilters({ 
   *   nombre: 'Juan', 
   *   page: 0, 
   *   size: 20,
   *   sort: 'nombre,asc'
   * });
   * 
   * // Buscar por documento
   * const result = await searchWithFilters({ 
   *   documento: '12345',
   *   page: 0,
   *   size: 10
   * });
   * 
   * // Todos los propietarios paginados (sin filtros)
   * const result = await searchWithFilters({ page: 0, size: 20 });
   * ```
   */
  async searchWithFilters(params: PropietarioSearchParams = {}): Promise<PageResponse<Propietario>> {
    const response = await axios.get<PageResponse<any>>('/propietarios/search', {
      params: {
        nombre: params.nombre || undefined,
        documento: params.documento || undefined,
        telefono: params.telefono || undefined,
        page: params.page ?? 0,
        size: params.size ?? 20,
        sort: params.sort || 'nombre,asc',
      },
    });
    
    // Normalizar IDs en el contenido paginado
    return {
      ...response.data,
      content: response.data.content.map(normalizePropietario),
    };
  },

  async create(data: PropietarioDTO): Promise<Propietario> {
    const response = await axios.post<any>('/propietarios', data);
    return normalizePropietario(response.data);
  },

  async update(id: string, data: PropietarioDTO): Promise<Propietario> {
    const response = await axios.put<any>(`/propietarios/${id}`, data);
    return normalizePropietario(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/propietarios/${id}`);
  },
};

