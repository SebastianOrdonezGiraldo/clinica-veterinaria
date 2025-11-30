import axios from '@core/api/axios';
import { Vacunacion, PageResponse, PageParams } from '@core/types';

export interface VacunacionDTO {
  pacienteId: string;
  vacunaId: string;
  profesionalId: string;
  fechaAplicacion: string;
  numeroDosis: number;
  proximaDosis?: string;
  lote?: string;
  observaciones?: string;
}

export interface VacunacionSearchParams extends PageParams {
  pacienteId?: string;
}

const normalizeVacunacion = (vacunacion: any): Vacunacion => ({
  ...vacunacion,
  id: String(vacunacion.id),
  pacienteId: String(vacunacion.pacienteId),
  vacunaId: String(vacunacion.vacunaId),
  profesionalId: String(vacunacion.profesionalId),
});

export const vacunacionService = {
  async searchWithFilters(params: VacunacionSearchParams = {}): Promise<PageResponse<Vacunacion>> {
    const { page = 0, size = 20, sort = 'fechaAplicacion,desc', pacienteId } = params;
    
    const searchParams = new URLSearchParams();
    searchParams.append('page', String(page));
    searchParams.append('size', String(size));
    searchParams.append('sort', sort);

    const url = pacienteId 
      ? `/vacunaciones/paciente/${pacienteId}/page?${searchParams.toString()}`
      : `/vacunaciones?${searchParams.toString()}`;

    const response = await axios.get<PageResponse<any>>(url);
    return {
      ...response.data,
      content: response.data.content.map(normalizeVacunacion),
    };
  },

  async getById(id: string): Promise<Vacunacion> {
    const response = await axios.get<any>(`/vacunaciones/${id}`);
    return normalizeVacunacion(response.data);
  },

  async getByPaciente(pacienteId: string): Promise<Vacunacion[]> {
    const response = await axios.get<any[]>(`/vacunaciones/paciente/${pacienteId}`);
    return response.data.map(normalizeVacunacion);
  },

  async getProximasAVencer(dias: number = 30): Promise<Vacunacion[]> {
    const response = await axios.get<any[]>(`/vacunaciones/proximas?dias=${dias}`);
    return response.data.map(normalizeVacunacion);
  },

  async getVencidas(): Promise<Vacunacion[]> {
    const response = await axios.get<any[]>('/vacunaciones/vencidas');
    return response.data.map(normalizeVacunacion);
  },

  async getUltimaVacunacion(pacienteId: string, vacunaId: string): Promise<Vacunacion | null> {
    try {
      const response = await axios.get<any>(`/vacunaciones/paciente/${pacienteId}/vacuna/${vacunaId}/ultima`);
      return normalizeVacunacion(response.data);
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async create(dto: VacunacionDTO): Promise<Vacunacion> {
    const response = await axios.post<any>('/vacunaciones', dto);
    return normalizeVacunacion(response.data);
  },

  async update(id: string, dto: VacunacionDTO): Promise<Vacunacion> {
    const response = await axios.put<any>(`/vacunaciones/${id}`, dto);
    return normalizeVacunacion(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/vacunaciones/${id}`);
  },
};

