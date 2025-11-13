import axios from '@core/api/axios';
import { Paciente, PageResponse, PageParams } from '@core/types';

export interface PacienteDTO {
  nombre: string;
  especie: string;
  raza?: string;
  sexo?: string;
  edadMeses?: number;
  pesoKg?: number;
  propietarioId: string;
  microchip?: string;
  notas?: string;
}

export interface PacienteSearchParams extends PageParams {
  nombre?: string;
  especie?: string;
}

// Función helper para normalizar IDs (convertir números a strings)
const normalizePaciente = (paciente: any): Paciente => ({
  ...paciente,
  id: String(paciente.id),
  propietarioId: String(paciente.propietarioId),
});

export const pacienteService = {
  /**
   * Obtiene todos los pacientes (sin paginación).
   * @deprecated Usar searchWithFilters para mejor rendimiento
   */
  async getAll(): Promise<Paciente[]> {
    const response = await axios.get<any[]>('/pacientes');
    return response.data.map(normalizePaciente);
  },

  /**
   * Busca pacientes con filtros y paginación del lado del servidor.
   * Este es el método recomendado para listados.
   * 
   * @param params Parámetros de búsqueda y paginación
   * @returns Respuesta paginada con pacientes
   * @example
   * // Página 1, 20 items, ordenar por nombre
   * searchWithFilters({ page: 0, size: 20, sort: 'nombre,asc' })
   * 
   * // Buscar "max" en caninos, página 0
   * searchWithFilters({ nombre: 'max', especie: 'Canino', page: 0, size: 20 })
   */
  async searchWithFilters(params: PacienteSearchParams = {}): Promise<PageResponse<Paciente>> {
    const response = await axios.get<PageResponse<any>>('/pacientes/search', {
      params: {
        nombre: params.nombre || undefined,
        especie: params.especie || undefined,
        page: params.page ?? 0,
        size: params.size ?? 20,
        sort: params.sort || 'nombre,asc',
      },
    });
    
    return {
      ...response.data,
      content: response.data.content.map(normalizePaciente),
    };
  },

  async getById(id: string): Promise<Paciente> {
    const response = await axios.get<any>(`/pacientes/${id}`);
    return normalizePaciente(response.data);
  },

  /**
   * Busca pacientes por nombre (sin paginación).
   * @deprecated Usar searchWithFilters para mejor rendimiento
   */
  async search(nombre: string): Promise<Paciente[]> {
    const response = await axios.get<any[]>('/pacientes/buscar', {
      params: { nombre },
    });
    return response.data.map(normalizePaciente);
  },

  async getByPropietario(propietarioId: string): Promise<Paciente[]> {
    const response = await axios.get<any[]>(`/pacientes/propietario/${propietarioId}`);
    return response.data.map(normalizePaciente);
  },

  async create(data: PacienteDTO): Promise<Paciente> {
    const response = await axios.post<any>('/pacientes', data);
    return normalizePaciente(response.data);
  },

  async update(id: string, data: PacienteDTO): Promise<Paciente> {
    const response = await axios.put<any>(`/pacientes/${id}`, data);
    return normalizePaciente(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/pacientes/${id}`);
  },
};

