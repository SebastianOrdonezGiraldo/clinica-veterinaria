import axios from '@core/api/axios';
import { Cita } from '@core/types';

export type EstadoCita = 'PENDIENTE' | 'CONFIRMADA' | 'ATENDIDA' | 'CANCELADA';

export interface CitaDTO {
  fecha: string; // ISO string (LocalDateTime en backend)
  motivo: string;
  observaciones?: string;
  pacienteId: string;
  propietarioId: string;
  profesionalId: string;
  estado?: EstadoCita;
}

// Función helper para normalizar IDs (convertir números a strings)
const normalizeCita = (cita: any): Cita => ({
  ...cita,
  id: String(cita.id),
  pacienteId: String(cita.pacienteId),
  propietarioId: String(cita.propietarioId),
  profesionalId: String(cita.profesionalId),
});

export const citaService = {
  async getAll(): Promise<Cita[]> {
    const response = await axios.get<any[]>('/citas');
    return response.data.map(normalizeCita);
  },

  async getById(id: string): Promise<Cita> {
    const response = await axios.get<any>(`/citas/${id}`);
    return normalizeCita(response.data);
  },

  async getByPaciente(pacienteId: string): Promise<Cita[]> {
    const response = await axios.get<any[]>(`/citas/paciente/${pacienteId}`);
    return response.data.map(normalizeCita);
  },

  async getByProfesional(profesionalId: string): Promise<Cita[]> {
    const response = await axios.get<any[]>(`/citas/profesional/${profesionalId}`);
    return response.data.map(normalizeCita);
  },

  async getByEstado(estado: EstadoCita): Promise<Cita[]> {
    const response = await axios.get<any[]>(`/citas/estado/${estado}`);
    return response.data.map(normalizeCita);
  },

  async create(data: CitaDTO): Promise<Cita> {
    // Convertir IDs de string a number para el backend
    const payload = {
      ...data,
      pacienteId: Number(data.pacienteId),
      propietarioId: Number(data.propietarioId),
      profesionalId: Number(data.profesionalId),
      fecha: data.fecha, // Ya debe ser ISO string
    };
    const response = await axios.post<any>('/citas', payload);
    return normalizeCita(response.data);
  },

  async update(id: string, data: CitaDTO): Promise<Cita> {
    // Convertir IDs de string a number para el backend
    const payload = {
      ...data,
      pacienteId: Number(data.pacienteId),
      propietarioId: Number(data.propietarioId),
      profesionalId: Number(data.profesionalId),
      fecha: data.fecha, // Ya debe ser ISO string
    };
    const response = await axios.put<any>(`/citas/${id}`, payload);
    return normalizeCita(response.data);
  },

  async updateEstado(id: string, estado: EstadoCita): Promise<Cita> {
    const response = await axios.patch<any>(`/citas/${id}/estado?estado=${estado}`);
    return normalizeCita(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/citas/${id}`);
  },
};

