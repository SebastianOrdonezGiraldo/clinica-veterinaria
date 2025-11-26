import axios from '@core/api/axios';
import { Cita, Usuario } from '@core/types';

/**
 * DTO para solicitud pública de cita
 */
export interface CitaPublicaRequestDTO {
  fecha: string; // ISO string
  motivo: string;
  observaciones?: string;
  profesionalId: string;
  // Opción 1: Usar IDs existentes
  propietarioId?: string;
  pacienteId?: string;
  // Opción 2: Crear nuevos registros
  propietarioNuevo?: {
    nombre: string;
    documento?: string;
    email: string;
    telefono?: string;
    direccion?: string;
    password?: string;
  };
  pacienteNuevo?: {
    nombre: string;
    especie: string;
    raza?: string;
    sexo?: string;
    edadMeses?: number;
    pesoKg?: number;
    microchip?: string;
    notas?: string;
  };
}

/**
 * Helper para normalizar IDs
 */
const normalizeCita = (cita: any): Cita => ({
  ...cita,
  id: String(cita.id),
  pacienteId: String(cita.pacienteId),
  propietarioId: String(cita.propietarioId),
  profesionalId: String(cita.profesionalId),
});

const normalizeUsuario = (usuario: any): Usuario => ({
  ...usuario,
  id: String(usuario.id),
});

/**
 * Servicio público para agendar citas sin autenticación
 */
export const citaPublicaService = {
  /**
   * Obtiene la lista de veterinarios activos disponibles
   */
  async getVeterinarios(): Promise<Usuario[]> {
    const response = await axios.get<any[]>('/public/veterinarios');
    return response.data.map(normalizeUsuario);
  },

  /**
   * Crea una nueva cita pública con registro opcional
   */
  async crearCita(data: CitaPublicaRequestDTO): Promise<Cita> {
    const payload = {
      ...data,
      profesionalId: Number(data.profesionalId),
      propietarioId: data.propietarioId ? Number(data.propietarioId) : undefined,
      pacienteId: data.pacienteId ? Number(data.pacienteId) : undefined,
      fecha: data.fecha, // ISO string
    };
    
    const response = await axios.post<any>('/public/citas', payload);
    return normalizeCita(response.data);
  },
};

