import axios from '@core/api/axios';
import { Usuario, Propietario } from '@core/types';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  type: string;
  usuario?: Usuario;
  propietario?: Propietario;
  userType: 'SISTEMA' | 'CLIENTE';
}

export const authService = {
  async login(email: string, password: string): Promise<LoginResponse> {
    const response = await axios.post<LoginResponse>('/auth/login', {
      email,
      password,
    });
    return response.data;
  },

  async validateToken(token: string): Promise<boolean> {
    const response = await axios.get<boolean>('/auth/validate', {
      params: { token },
    });
    return response.data;
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },
};

