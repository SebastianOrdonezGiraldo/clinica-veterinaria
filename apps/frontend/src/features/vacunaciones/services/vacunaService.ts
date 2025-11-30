import axios from '@core/api/axios';
import { Vacuna, PageResponse, PageParams } from '@core/types';

export interface VacunaDTO {
  nombre: string;
  especie?: string;
  numeroDosis: number;
  intervaloDias?: number;
  descripcion?: string;
  fabricante?: string;
  activo?: boolean;
}

export interface VacunaSearchParams extends PageParams {
  nombre?: string;
  especie?: string;
}

const normalizeVacuna = (vacuna: any): Vacuna => ({
  ...vacuna,
  id: String(vacuna.id),
});

export const vacunaService = {
  async getAll(): Promise<Vacuna[]> {
    const response = await axios.get<any[]>('/vacunas');
    return response.data.map(normalizeVacuna);
  },

  async searchWithFilters(params: VacunaSearchParams = {}): Promise<PageResponse<Vacuna>> {
    const { page = 0, size = 20, sort = 'nombre,asc', nombre, especie } = params;
    
    const searchParams = new URLSearchParams();
    searchParams.append('page', String(page));
    searchParams.append('size', String(size));
    searchParams.append('sort', sort);
    if (nombre) searchParams.append('nombre', nombre);
    if (especie) searchParams.append('especie', especie);

    const response = await axios.get<PageResponse<any>>(`/vacunas/search?${searchParams.toString()}`);
    return {
      ...response.data,
      content: response.data.content.map(normalizeVacuna),
    };
  },

  async getById(id: string): Promise<Vacuna> {
    const response = await axios.get<any>(`/vacunas/${id}`);
    return normalizeVacuna(response.data);
  },

  async getActivas(): Promise<Vacuna[]> {
    const response = await axios.get<any[]>('/vacunas/activas');
    return response.data.map(normalizeVacuna);
  },

  async getByEspecie(especie: string): Promise<Vacuna[]> {
    const response = await axios.get<any[]>(`/vacunas/especie/${especie}`);
    return response.data.map(normalizeVacuna);
  },

  async create(dto: VacunaDTO): Promise<Vacuna> {
    const response = await axios.post<any>('/vacunas', dto);
    return normalizeVacuna(response.data);
  },

  async update(id: string, dto: VacunaDTO): Promise<Vacuna> {
    const response = await axios.put<any>(`/vacunas/${id}`, dto);
    return normalizeVacuna(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/vacunas/${id}`);
  },
};

