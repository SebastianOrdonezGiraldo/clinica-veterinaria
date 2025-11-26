import axios from '@core/api/axios';

export interface ClienteLoginRequest {
  email: string;
  password: string;
}

export interface ClienteLoginResponse {
  token: string;
  type: string;
  propietario: {
    id: number;
    nombre: string;
    email: string;
    telefono?: string;
    direccion?: string;
    documento?: string;
    activo: boolean;
  };
}

export interface Propietario {
  id: number;
  nombre: string;
  email: string;
  telefono?: string;
  direccion?: string;
  documento?: string;
  activo: boolean;
}

export const clienteAuthService = {
  async login(email: string, password: string): Promise<ClienteLoginResponse> {
    const response = await axios.post<ClienteLoginResponse>('/public/clientes/auth/login', {
      email,
      password,
    });
    return response.data;
  },

  async validateToken(token: string): Promise<boolean> {
    try {
      const response = await axios.get<boolean>('/public/clientes/auth/validate', {
        params: { token },
      });
      return response.data;
    } catch {
      return false;
    }
  },

  logout() {
    localStorage.removeItem('clienteToken');
    localStorage.removeItem('cliente');
  },
};

