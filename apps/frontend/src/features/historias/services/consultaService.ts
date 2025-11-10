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

// Función helper para normalizar IDs (convertir números a strings)
const normalizeConsulta = (consulta: any): Consulta => ({
  ...consulta,
  id: String(consulta.id),
  pacienteId: String(consulta.pacienteId),
  profesionalId: String(consulta.profesionalId),
  prescripcionesIds: consulta.prescripcionesIds?.map((id: any) => String(id)) || [],
});

export const consultaService = {
  async getAll(): Promise<Consulta[]> {
    const response = await axios.get<any[]>('/consultas');
    return response.data.map(normalizeConsulta);
  },

  async getById(id: string): Promise<Consulta> {
    const response = await axios.get<any>(`/consultas/${id}`);
    return normalizeConsulta(response.data);
  },

  async getByPaciente(pacienteId: string): Promise<Consulta[]> {
    const response = await axios.get<any[]>(`/consultas/paciente/${pacienteId}`);
    return response.data.map(normalizeConsulta);
  },

  async create(data: ConsultaDTO): Promise<Consulta> {
    // Convertir IDs de string a number para el backend
    const payload = {
      ...data,
      pacienteId: Number(data.pacienteId),
      profesionalId: Number(data.profesionalId),
      fecha: data.fecha || new Date().toISOString(),
    };
    const response = await axios.post<any>('/consultas', payload);
    return normalizeConsulta(response.data);
  },

  async update(id: string, data: ConsultaDTO): Promise<Consulta> {
    // Convertir IDs de string a number para el backend
    const payload = {
      ...data,
      pacienteId: Number(data.pacienteId),
      profesionalId: Number(data.profesionalId),
    };
    const response = await axios.put<any>(`/consultas/${id}`, payload);
    return normalizeConsulta(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/consultas/${id}`);
  },
};

