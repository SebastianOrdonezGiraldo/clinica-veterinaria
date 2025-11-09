import axios from '@core/api/axios';
import { Consulta } from '@core/types';

export interface ConsultaDTO {
  fecha?: string;
  pacienteId: string;
  profesionalId: string;
  frecuenciaCardiaca?: number;
  frecuenciaRespiratoria?: number;
  temperatura?: number;
  pesoKg?: number;
  examenFisico?: string;
  diagnostico?: string;
  tratamiento?: string;
  observaciones?: string;
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

  async getByPaciente(pacienteId: string): Promise<Consulta[]> {
    const response = await axios.get<Consulta[]>(`/consultas/paciente/${pacienteId}`);
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

