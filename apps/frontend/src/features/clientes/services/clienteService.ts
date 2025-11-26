import axios from '@core/api/axios';
import { Cita, Paciente, Propietario } from '@core/types';

/**
 * Servicio para que los clientes vean sus citas y mascotas
 */
export const clienteService = {
  /**
   * Obtiene el perfil del cliente autenticado
   */
  async getMiPerfil(): Promise<Propietario> {
    const response = await axios.get<Propietario>('/clientes/mi-perfil');
    return response.data;
  },

  /**
   * Obtiene todas las citas del cliente autenticado
   */
  async getMisCitas(): Promise<Cita[]> {
    const response = await axios.get<Cita[]>('/clientes/mis-citas');
    return response.data;
  },

  /**
   * Obtiene todas las mascotas del cliente autenticado
   */
  async getMisMascotas(): Promise<Paciente[]> {
    const response = await axios.get<Paciente[]>('/clientes/mis-mascotas');
    return response.data;
  },

  /**
   * Establece una contraseña para un propietario existente que no tiene contraseña
   */
  async establecerPassword(email: string, password: string): Promise<void> {
    await axios.post('/public/clientes/establecer-password', {
      email,
      password,
    });
  },
};
