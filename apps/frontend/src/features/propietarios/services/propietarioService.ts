import axios from '@core/api/axios';
import { Propietario } from '@core/types';

export interface PropietarioDTO {
  nombre: string;
  documento?: string;
  email?: string;
  telefono?: string;
  direccion?: string;
}

// Función helper para normalizar IDs (convertir números a strings)
const normalizePropietario = (propietario: any): Propietario => ({
  ...propietario,
  id: String(propietario.id),
});

export const propietarioService = {
  async getAll(): Promise<Propietario[]> {
    const response = await axios.get<any[]>('/propietarios');
    return response.data.map(normalizePropietario);
  },

  async getById(id: string): Promise<Propietario> {
    const response = await axios.get<any>(`/propietarios/${id}`);
    return normalizePropietario(response.data);
  },

  async search(nombre: string): Promise<Propietario[]> {
    const response = await axios.get<any[]>('/propietarios/buscar', {
      params: { nombre },
    });
    return response.data.map(normalizePropietario);
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

