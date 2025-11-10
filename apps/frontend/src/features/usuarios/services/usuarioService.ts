import axios from '@core/api/axios';
import { Usuario } from '@core/types';

export interface UsuarioDTO {
  nombre: string;
  email: string;
  password: string;
  rol: 'ADMIN' | 'VET' | 'RECEPCION' | 'ESTUDIANTE';
  activo?: boolean;
}

// Función helper para normalizar IDs (convertir números a strings)
const normalizeUsuario = (usuario: any): Usuario => ({
  ...usuario,
  id: String(usuario.id),
});

export const usuarioService = {
  async getAll(): Promise<Usuario[]> {
    const response = await axios.get<any[]>('/usuarios');
    return response.data.map(normalizeUsuario);
  },

  async getById(id: string): Promise<Usuario> {
    const response = await axios.get<any>(`/usuarios/${id}`);
    return normalizeUsuario(response.data);
  },

  async search(email: string): Promise<Usuario | null> {
    try {
      const response = await axios.get<any>(`/usuarios/email/${email}`);
      return normalizeUsuario(response.data);
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async create(data: UsuarioDTO): Promise<Usuario> {
    const response = await axios.post<any>('/usuarios', data);
    return normalizeUsuario(response.data);
  },

  async update(id: string, data: Partial<UsuarioDTO>): Promise<Usuario> {
    const response = await axios.put<any>(`/usuarios/${id}`, data);
    return normalizeUsuario(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/usuarios/${id}`);
  },
};

