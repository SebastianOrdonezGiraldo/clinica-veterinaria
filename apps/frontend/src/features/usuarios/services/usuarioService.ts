import axios from '@core/api/axios';
import { Usuario } from '@core/types';

export interface UsuarioDTO {
  nombre: string;
  email: string;
  password: string;
  rol: 'ADMIN' | 'VET' | 'RECEPCION' | 'ESTUDIANTE';
  activo?: boolean;
}

export const usuarioService = {
  async getAll(): Promise<Usuario[]> {
    const response = await axios.get<Usuario[]>('/usuarios');
    return response.data;
  },

  async getById(id: string): Promise<Usuario> {
    const response = await axios.get<Usuario>(`/usuarios/${id}`);
    return response.data;
  },

  async search(email: string): Promise<Usuario | null> {
    try {
      const response = await axios.get<Usuario>(`/usuarios/email/${email}`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async create(data: UsuarioDTO): Promise<Usuario> {
    const response = await axios.post<Usuario>('/usuarios', data);
    return response.data;
  },

  async update(id: string, data: Partial<UsuarioDTO>): Promise<Usuario> {
    const response = await axios.put<Usuario>(`/usuarios/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/usuarios/${id}`);
  },
};

