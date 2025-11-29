import axios from '@core/api/axios';
import { Cita, Paciente, Propietario } from '@core/types';

/**
 * Helper para normalizar IDs de pacientes (convertir números a strings)
 */
const normalizePaciente = (paciente: any): Paciente => ({
  ...paciente,
  id: String(paciente.id),
  propietarioId: String(paciente.propietarioId),
});

/**
 * Helper para normalizar IDs de citas (convertir números a strings)
 */
const normalizeCita = (cita: any): Cita => ({
  ...cita,
  id: String(cita.id),
  pacienteId: String(cita.pacienteId),
  propietarioId: String(cita.propietarioId),
  profesionalId: String(cita.profesionalId),
});

/**
 * Servicio para que los clientes vean sus citas y mascotas
 */
export const clienteService = {
  /**
   * Obtiene el perfil del cliente autenticado
   */
  async getMiPerfil(): Promise<Propietario> {
    const response = await axios.get<Propietario>('/clientes/mi-perfil');
    return {
      ...response.data,
      id: String(response.data.id),
    };
  },

  /**
   * Obtiene todas las citas del cliente autenticado
   */
  async getMisCitas(): Promise<Cita[]> {
    const response = await axios.get<Cita[]>('/clientes/mis-citas');
    return response.data.map(normalizeCita);
  },

  /**
   * Obtiene todas las mascotas del cliente autenticado
   */
  async getMisMascotas(): Promise<Paciente[]> {
    const response = await axios.get<Paciente[]>('/clientes/mis-mascotas');
    return response.data.map(normalizePaciente);
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
