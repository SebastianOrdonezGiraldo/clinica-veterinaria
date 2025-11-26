import axios from '@core/api/axios';
import { Prescripcion, PrescripcionItem, PageResponse, PageParams } from '@core/types';

export interface PrescripcionDTO {
  consultaId: string;
  fechaEmision?: string; // ISO string
  indicacionesGenerales?: string;
  items: ItemPrescripcionDTO[];
}

export interface ItemPrescripcionDTO {
  medicamento: string;
  presentacion?: string;
  dosis: string;
  frecuencia: string;
  duracionDias: number;
  viaAdministracion: 'ORAL' | 'INYECTABLE' | 'TOPICA' | 'OFTALMICA' | 'OTICA' | 'OTRA';
  indicaciones?: string;
}

export interface PrescripcionSearchParams extends PageParams {
  pacienteId?: string;
  consultaId?: string;
  fechaInicio?: string; // ISO string
  fechaFin?: string; // ISO string
}

// Función helper para normalizar IDs (convertir números a strings)
const normalizePrescripcion = (prescripcion: any): Prescripcion => ({
  ...prescripcion,
  id: String(prescripcion.id),
  consultaId: String(prescripcion.consultaId),
  fechaEmision: prescripcion.fechaEmision,
  indicacionesGenerales: prescripcion.indicacionesGenerales,
  items: prescripcion.items?.map((item: any) => ({
    medicamento: item.medicamento,
    presentacion: item.presentacion,
    dosis: item.dosis,
    frecuencia: item.frecuencia,
    duracionDias: item.duracionDias,
    viaAdministracion: item.viaAdministracion,
    indicaciones: item.indicaciones,
  } as PrescripcionItem)) || [],
  createdAt: prescripcion.createdAt,
  updatedAt: prescripcion.updatedAt,
});

export const prescripcionService = {
  /**
   * Obtiene todas las prescripciones sin paginación
   * @deprecated Usar searchWithFilters para mejor rendimiento
   */
  async getAll(): Promise<Prescripcion[]> {
    const response = await axios.get<any[]>('/prescripciones');
    return response.data.map(normalizePrescripcion);
  },

  /**
   * Busca prescripciones con filtros y paginación del lado del servidor.
   * Este es el método recomendado para listados.
   */
  async searchWithFilters(params: PrescripcionSearchParams = {}): Promise<PageResponse<Prescripcion>> {
    const response = await axios.get<PageResponse<any>>('/prescripciones/search', {
      params: {
        pacienteId: params.pacienteId ? Number(params.pacienteId) : undefined,
        consultaId: params.consultaId ? Number(params.consultaId) : undefined,
        fechaInicio: params.fechaInicio || undefined,
        fechaFin: params.fechaFin || undefined,
        page: params.page ?? 0,
        size: params.size ?? 20,
        sort: params.sort || 'fechaEmision,desc',
      },
    });
    
    return {
      ...response.data,
      content: response.data.content.map(normalizePrescripcion),
    };
  },

  async getById(id: string): Promise<Prescripcion> {
    const response = await axios.get<any>(`/prescripciones/${id}`);
    return normalizePrescripcion(response.data);
  },

  /**
   * @deprecated Usar searchWithFilters con consultaId
   */
  async getByConsulta(consultaId: string): Promise<Prescripcion[]> {
    const response = await axios.get<any[]>(`/prescripciones/consulta/${consultaId}`);
    return response.data.map(normalizePrescripcion);
  },

  /**
   * @deprecated Usar searchWithFilters con pacienteId
   */
  async getByPaciente(pacienteId: string): Promise<Prescripcion[]> {
    const response = await axios.get<any[]>(`/prescripciones/paciente/${pacienteId}`);
    return response.data.map(normalizePrescripcion);
  },

  async create(data: PrescripcionDTO): Promise<Prescripcion> {
    // Convertir IDs de string a number para el backend
    const payload = {
      ...data,
      consultaId: Number(data.consultaId),
      fechaEmision: data.fechaEmision || new Date().toISOString(),
      items: data.items.map(item => ({
        ...item,
        prescripcionId: undefined, // Se asignará automáticamente en el backend
      })),
    };
    const response = await axios.post<any>('/prescripciones', payload);
    return normalizePrescripcion(response.data);
  },

  async update(id: string, data: PrescripcionDTO): Promise<Prescripcion> {
    // Convertir IDs de string a number para el backend
    const payload = {
      ...data,
      consultaId: Number(data.consultaId),
      fechaEmision: data.fechaEmision || new Date().toISOString(),
      items: data.items.map(item => ({
        ...item,
        prescripcionId: undefined, // Se asignará automáticamente en el backend
      })),
    };
    const response = await axios.put<any>(`/prescripciones/${id}`, payload);
    return normalizePrescripcion(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/prescripciones/${id}`);
  },
};

