import axios from '@core/api/axios';
import { Cita, PageResponse, PageParams } from '@core/types';

export type EstadoCita = 'PENDIENTE' | 'CONFIRMADA' | 'EN_PROCESO' | 'COMPLETADA' | 'CANCELADA';

/**
 * DTO para crear/actualizar citas
 */
export interface CitaDTO {
  fecha: string; // ISO string (LocalDateTime en backend)
  motivo: string;
  observaciones?: string;
  pacienteId: string;
  propietarioId: string;
  profesionalId: string;
  estado?: EstadoCita;
}

/**
 * Parámetros de búsqueda para citas con paginación
 */
export interface CitaSearchParams extends PageParams {
  estado?: EstadoCita;
  profesionalId?: string;
  pacienteId?: string;
  fechaInicio?: string; // ISO string
  fechaFin?: string; // ISO string
}

/**
 * Helper para normalizar IDs (convertir números a strings)
 * 
 * PATRÓN: Data Mapper
 * Transforma la representación del backend (IDs numéricos)
 * a la representación del frontend (IDs string)
 */
const normalizeCita = (cita: any): Cita => ({
  ...cita,
  id: String(cita.id),
  pacienteId: String(cita.pacienteId),
  propietarioId: String(cita.propietarioId),
  profesionalId: String(cita.profesionalId),
});

/**
 * Servicio de citas - Capa de acceso a la API
 * 
 * PATRONES APLICADOS:
 * - Service Layer Pattern: Encapsula la lógica de comunicación con la API
 * - Adapter Pattern: Adapta las respuestas del backend al formato del frontend
 */
export const citaService = {
  /**
   * Obtiene todas las citas sin paginación
   * @deprecated Usar searchWithFilters para búsquedas paginadas
   */
  async getAll(): Promise<Cita[]> {
    const response = await axios.get<any[]>('/citas');
    return response.data.map(normalizeCita);
  },

  /**
   * Obtiene una cita por su ID
   */
  async getById(id: string): Promise<Cita> {
    const response = await axios.get<any>(`/citas/${id}`);
    return normalizeCita(response.data);
  },

  /**
   * Busca citas por paciente sin paginación
   * @deprecated Usar searchWithFilters con pacienteId
   */
  async getByPaciente(pacienteId: string): Promise<Cita[]> {
    const response = await axios.get<any[]>(`/citas/paciente/${pacienteId}`);
    return response.data.map(normalizeCita);
  },

  /**
   * Busca citas por profesional sin paginación
   * @deprecated Usar searchWithFilters con profesionalId
   */
  async getByProfesional(profesionalId: string): Promise<Cita[]> {
    const response = await axios.get<any[]>(`/citas/profesional/${profesionalId}`);
    return response.data.map(normalizeCita);
  },

  /**
   * Busca citas por estado sin paginación
   * @deprecated Usar searchWithFilters con estado
   */
  async getByEstado(estado: EstadoCita): Promise<Cita[]> {
    const response = await axios.get<any[]>(`/citas/estado/${estado}`);
    return response.data.map(normalizeCita);
  },
  
  /**
   * Busca citas con filtros combinados y paginación del lado del servidor.
   * 
   * <strong>VENTAJAS:</strong>
   * - ✓ Paginación en backend (eficiente para agendas grandes)
   * - ✓ Búsqueda multicritero (estado, profesional, paciente, fechas)
   * - ✓ Ordenamiento configurable
   * - ✓ Ideal para vistas de calendario y agendas
   * 
   * @param params Parámetros de búsqueda y paginación
   * @returns Respuesta paginada con citas y metadatos
   * 
   * @example
   * ```typescript
   * // Buscar citas pendientes
   * const result = await searchWithFilters({ 
   *   estado: 'PENDIENTE', 
   *   page: 0, 
   *   size: 20,
   *   sort: 'fecha,asc'
   * });
   * 
   * // Agenda de un veterinario esta semana
   * const result = await searchWithFilters({ 
   *   profesionalId: '5',
   *   fechaInicio: '2024-01-08T00:00:00',
   *   fechaFin: '2024-01-14T23:59:59',
   *   page: 0,
   *   size: 50
   * });
   * 
   * // Historial de una mascota
   * const result = await searchWithFilters({ 
   *   pacienteId: '10',
   *   page: 0,
   *   size: 10,
   *   sort: 'fecha,desc'
   * });
   * ```
   */
  async searchWithFilters(params: CitaSearchParams = {}): Promise<PageResponse<Cita>> {
    const response = await axios.get<PageResponse<any>>('/citas/search', {
      params: {
        estado: params.estado || undefined,
        profesionalId: params.profesionalId ? Number(params.profesionalId) : undefined,
        pacienteId: params.pacienteId ? Number(params.pacienteId) : undefined,
        fechaInicio: params.fechaInicio || undefined,
        fechaFin: params.fechaFin || undefined,
        page: params.page ?? 0,
        size: params.size ?? 20,
        sort: params.sort || 'fecha,asc',
      },
    });
    
    // Normalizar IDs en el contenido paginado
    return {
      ...response.data,
      content: response.data.content.map(normalizeCita),
    };
  },

  async create(data: CitaDTO): Promise<Cita> {
    // Convertir IDs de string a number para el backend
    const payload = {
      ...data,
      pacienteId: Number(data.pacienteId),
      propietarioId: Number(data.propietarioId),
      profesionalId: Number(data.profesionalId),
      fecha: data.fecha, // Ya debe ser ISO string
    };
    const response = await axios.post<any>('/citas', payload);
    return normalizeCita(response.data);
  },

  async update(id: string, data: CitaDTO): Promise<Cita> {
    // Convertir IDs de string a number para el backend
    const payload = {
      ...data,
      pacienteId: Number(data.pacienteId),
      propietarioId: Number(data.propietarioId),
      profesionalId: Number(data.profesionalId),
      fecha: data.fecha, // Ya debe ser ISO string
    };
    const response = await axios.put<any>(`/citas/${id}`, payload);
    return normalizeCita(response.data);
  },

  async updateEstado(id: string, estado: EstadoCita): Promise<Cita> {
    // Enviar el estado en el body para mayor confiabilidad
    const response = await axios.patch<any>(`/citas/${id}/estado`, { estado });
    return normalizeCita(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/citas/${id}`);
  },
};

