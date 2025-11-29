import axios from '@core/api/axios';
import { Consulta, PageResponse, PageParams } from '@core/types';

/**
 * DTO para crear/actualizar consultas
 */
export interface ConsultaDTO {
  fecha?: string;
  pacienteId: string;
  profesionalId: string;
  frecuenciaCardiaca?: number;
  frecuenciaRespiratoria?: number;
  temperatura?: number;
  pesoKg?: number;
  examenFisico?: string;
  diagnostico?: string;
  tratamiento?: string;
  observaciones?: string;
}

/**
 * Parámetros de búsqueda para consultas con paginación
 */
export interface ConsultaSearchParams extends PageParams {
  pacienteId?: string;
  profesionalId?: string;
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
const normalizeConsulta = (consulta: any): Consulta => ({
  ...consulta,
  id: String(consulta.id),
  pacienteId: String(consulta.pacienteId),
  profesionalId: String(consulta.profesionalId),
  prescripcionesIds: consulta.prescripcionesIds?.map((id: any) => String(id)) || [],
});

/**
 * Servicio de consultas - Capa de acceso a la API
 * 
 * PATRONES APLICADOS:
 * - Service Layer Pattern: Encapsula la lógica de comunicación con la API
 * - Adapter Pattern: Adapta las respuestas del backend al formato del frontend
 */
export const consultaService = {
  /**
   * Obtiene todas las consultas sin paginación
   * @deprecated Usar searchWithFilters para búsquedas paginadas
   */
  async getAll(): Promise<Consulta[]> {
    const response = await axios.get<any[]>('/consultas');
    return response.data.map(normalizeConsulta);
  },

  /**
   * Obtiene una consulta por su ID
   */
  async getById(id: string): Promise<Consulta> {
    const response = await axios.get<any>(`/consultas/${id}`);
    return normalizeConsulta(response.data);
  },

  /**
   * Busca consultas por paciente sin paginación
   * @deprecated Usar searchWithFilters con pacienteId
   */
  async getByPaciente(pacienteId: string): Promise<Consulta[]> {
    const response = await axios.get<any[]>(`/consultas/paciente/${pacienteId}`);
    return response.data.map(normalizeConsulta);
  },
  
  /**
   * Busca consultas con filtros combinados y paginación del lado del servidor.
   * 
   * <strong>VENTAJAS:</strong>
   * - ✓ Paginación en backend (eficiente para historiales extensos)
   * - ✓ Búsqueda multicritero (paciente, profesional, fechas)
   * - ✓ Ordenamiento configurable
   * - ✓ Ideal para historiales médicos completos
   * 
   * @param params Parámetros de búsqueda y paginación
   * @returns Respuesta paginada con consultas y metadatos
   * 
   * @example
   * ```typescript
   * // Historial completo de mascota
   * const result = await searchWithFilters({ 
   *   pacienteId: '10', 
   *   page: 0, 
   *   size: 20,
   *   sort: 'fecha,desc'
   * });
   * 
   * // Consultas de un veterinario este mes
   * const result = await searchWithFilters({ 
   *   profesionalId: '5',
   *   fechaInicio: '2024-01-01T00:00:00',
   *   fechaFin: '2024-01-31T23:59:59',
   *   page: 0,
   *   size: 50
   * });
   * 
   * // Historial de mascota del último año
   * const result = await searchWithFilters({ 
   *   pacienteId: '10',
   *   fechaInicio: '2023-01-01T00:00:00',
   *   fechaFin: '2024-01-01T00:00:00',
   *   page: 0,
   *   size: 10
   * });
   * ```
   */
  async searchWithFilters(params: ConsultaSearchParams = {}): Promise<PageResponse<Consulta>> {
    const response = await axios.get<PageResponse<any>>('/consultas/search', {
      params: {
        pacienteId: params.pacienteId ? Number(params.pacienteId) : undefined,
        profesionalId: params.profesionalId ? Number(params.profesionalId) : undefined,
        fechaInicio: params.fechaInicio || undefined,
        fechaFin: params.fechaFin || undefined,
        page: params.page ?? 0,
        size: params.size ?? 20,
        sort: params.sort || 'fecha,desc',
      },
    });
    
    // Normalizar IDs en el contenido paginado
    return {
      ...response.data,
      content: response.data.content.map(normalizeConsulta),
    };
  },

  async create(data: ConsultaDTO): Promise<Consulta> {
    // Convertir IDs de string a number para el backend
    // Limpiar valores undefined para evitar problemas de serialización
    const payload: any = {
      pacienteId: Number(data.pacienteId),
      profesionalId: Number(data.profesionalId),
      fecha: data.fecha || new Date().toISOString(),
    };

    // Solo agregar campos que tengan valores válidos (no undefined, null, o string vacío)
    if (data.frecuenciaCardiaca !== undefined && data.frecuenciaCardiaca !== null && data.frecuenciaCardiaca !== '') {
      payload.frecuenciaCardiaca = typeof data.frecuenciaCardiaca === 'number' ? data.frecuenciaCardiaca : Number(data.frecuenciaCardiaca);
    }
    if (data.frecuenciaRespiratoria !== undefined && data.frecuenciaRespiratoria !== null && data.frecuenciaRespiratoria !== '') {
      payload.frecuenciaRespiratoria = typeof data.frecuenciaRespiratoria === 'number' ? data.frecuenciaRespiratoria : Number(data.frecuenciaRespiratoria);
    }
    if (data.temperatura !== undefined && data.temperatura !== null && data.temperatura !== '') {
      payload.temperatura = typeof data.temperatura === 'number' ? data.temperatura : Number(data.temperatura);
    }
    if (data.pesoKg !== undefined && data.pesoKg !== null && data.pesoKg !== '') {
      payload.pesoKg = typeof data.pesoKg === 'number' ? data.pesoKg : Number(data.pesoKg);
    }
    if (data.examenFisico && data.examenFisico.trim()) payload.examenFisico = data.examenFisico.trim();
    if (data.diagnostico && data.diagnostico.trim()) payload.diagnostico = data.diagnostico.trim();
    if (data.tratamiento && data.tratamiento.trim()) payload.tratamiento = data.tratamiento.trim();
    if (data.observaciones && data.observaciones.trim()) payload.observaciones = data.observaciones.trim();

    const response = await axios.post<any>('/consultas', payload);
    return normalizeConsulta(response.data);
  },

  async update(id: string, data: ConsultaDTO): Promise<Consulta> {
    // Convertir IDs de string a number para el backend
    const payload = {
      ...data,
      pacienteId: Number(data.pacienteId),
      profesionalId: Number(data.profesionalId),
    };
    const response = await axios.put<any>(`/consultas/${id}`, payload);
    return normalizeConsulta(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/consultas/${id}`);
  },
};

