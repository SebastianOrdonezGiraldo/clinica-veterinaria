import axios from '@/lib/axios';
import { Consulta } from '@/types';

export interface ConsultaDTO {
  citaId: string;
  motivoConsulta: string;
  anamnesis?: string;
  examenFisico?: string;
  diagnostico?: string;
  tratamiento?: string;
  observaciones?: string;
  peso?: number;
  temperatura?: number;
  frecuenciaCardiaca?: number;
  frecuenciaRespiratoria?: number;
}

export const consultaService = {
  async getAll(): Promise<Consulta[]> {
    const response = await axios.get<Consulta[]>('/consultas');
    return response.data;
  },

  async getById(id: string): Promise<Consulta> {
    const response = await axios.get<Consulta>(`/consultas/${id}`);
    return response.data;
  },

  async getByCita(citaId: string): Promise<Consulta | null> {
    try {
      const response = await axios.get<Consulta>('/consultas/cita', {
        params: { citaId },
      });
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async getByPaciente(pacienteId: string): Promise<Consulta[]> {
    const response = await axios.get<Consulta[]>('/consultas/paciente', {
      params: { pacienteId },
    });
    return response.data;
  },

  async create(data: ConsultaDTO): Promise<Consulta> {
    const response = await axios.post<Consulta>('/consultas', data);
    return response.data;
  },

  async update(id: string, data: ConsultaDTO): Promise<Consulta> {
    const response = await axios.put<Consulta>(`/consultas/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/consultas/${id}`);
  },
};

