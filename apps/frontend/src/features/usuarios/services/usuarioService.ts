import axios from '@core/api/axios';
import { Usuario } from '@core/types';

export interface UsuarioCreateDTO {
  nombre: string;
  email: string;
  password: string;
  rol: 'ADMIN' | 'VET' | 'RECEPCION' | 'ESTUDIANTE';
  activo?: boolean;
}

export interface UsuarioUpdateDTO {
  nombre: string;
  email: string;
  password?: string; // Opcional para actualización
  rol: 'ADMIN' | 'VET' | 'RECEPCION' | 'ESTUDIANTE';
  activo?: boolean;
}

export interface ResetPasswordDTO {
  password: string;
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

  async create(data: UsuarioCreateDTO): Promise<Usuario> {
    const response = await axios.post<any>('/usuarios', data);
    return normalizeUsuario(response.data);
  },

  async update(id: string, data: UsuarioUpdateDTO): Promise<Usuario> {
    // Si password está vacío o es "NO_CHANGE", no incluirlo en el payload
    const payload: any = {
      nombre: data.nombre,
      email: data.email,
      rol: data.rol,
      activo: data.activo,
    };
    
    // Solo incluir password si se proporciona y no es el marcador especial
    if (data.password && data.password.trim() && data.password !== 'NO_CHANGE') {
      payload.password = data.password;
    }
    
    const response = await axios.put<any>(`/usuarios/${id}`, payload);
    return normalizeUsuario(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/usuarios/${id}`);
  },

  async resetPassword(id: string, newPassword: string): Promise<void> {
    await axios.post(`/usuarios/${id}/reset-password`, { password: newPassword });
  },

  /**
   * Actualizar el perfil del usuario autenticado
   * Permite actualizar nombre, email y contraseña (sin cambiar rol ni estado)
   */
  async updateMyProfile(data: { nombre: string; email: string; password?: string }): Promise<Usuario> {
    const payload: any = {
      nombre: data.nombre,
      email: data.email,
    };
    
    if (data.password && data.password.trim()) {
      payload.password = data.password;
    }
    
    const response = await axios.put<any>('/usuarios/me', payload);
    return normalizeUsuario(response.data);
  },
};

