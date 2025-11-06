import axios from '@/lib/axios';
import { Propietario } from '@/types';

export interface PropietarioDTO {
  nombre: string;
  documento: string;
  email: string;
  telefono?: string;
  direccion?: string;
}

export const propietarioService = {
  async getAll(): Promise<Propietario[]> {
    const response = await axios.get<Propietario[]>('/propietarios');
    return response.data;
  },

  async getById(id: string): Promise<Propietario> {
    const response = await axios.get<Propietario>(`/propietarios/${id}`);
    return response.data;
  },

  async search(nombre: string): Promise<Propietario[]> {
    const response = await axios.get<Propietario[]>('/propietarios/buscar', {
      params: { nombre },
    });
    return response.data;
  },

  async create(data: PropietarioDTO): Promise<Propietario> {
    const response = await axios.post<Propietario>('/propietarios', data);
    return response.data;
  },

  async update(id: string, data: PropietarioDTO): Promise<Propietario> {
    const response = await axios.put<Propietario>(`/propietarios/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/propietarios/${id}`);
  },
};

