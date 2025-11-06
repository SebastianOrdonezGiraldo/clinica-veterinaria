import axios from '@/lib/axios';
import { Cita } from '@/types';

export type EstadoCita = 'PENDIENTE' | 'CONFIRMADA' | 'ATENDIDA' | 'CANCELADA';

export interface CitaDTO {
  fecha: string;
  motivo: string;
  observaciones?: string;
  pacienteId: string;
  profesionalId: string;
  estado?: EstadoCita;
}

export const citaService = {
  async getAll(): Promise<Cita[]> {
    const response = await axios.get<Cita[]>('/citas');
    return response.data;
  },

  async getById(id: string): Promise<Cita> {
    const response = await axios.get<Cita>(`/citas/${id}`);
    return response.data;
  },

  async getByPaciente(pacienteId: string): Promise<Cita[]> {
    const response = await axios.get<Cita[]>('/citas/paciente', {
      params: { pacienteId },
    });
    return response.data;
  },

  async getByProfesional(profesionalId: string): Promise<Cita[]> {
    const response = await axios.get<Cita[]>('/citas/profesional', {
      params: { profesionalId },
    });
    return response.data;
  },

  async getByEstado(estado: EstadoCita): Promise<Cita[]> {
    const response = await axios.get<Cita[]>(`/citas/estado/${estado}`);
    return response.data;
  },

  async create(data: CitaDTO): Promise<Cita> {
    const response = await axios.post<Cita>('/citas', data);
    return response.data;
  },

  async update(id: string, data: CitaDTO): Promise<Cita> {
    const response = await axios.put<Cita>(`/citas/${id}`, data);
    return response.data;
  },

  async updateEstado(id: string, estado: EstadoCita): Promise<Cita> {
    const response = await axios.patch<Cita>(`/citas/${id}/estado`, null, {
      params: { estado },
    });
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/citas/${id}`);
  },
};

