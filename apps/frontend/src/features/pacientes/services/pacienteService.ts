import axios from '@core/api/axios';
import { Paciente } from '@core/types';

export interface PacienteDTO {
  nombre: string;
  especie: string;
  raza?: string;
  sexo?: string;
  edadMeses?: number;
  pesoKg?: number;
  propietarioId: string;
  microchip?: string;
  notas?: string;
}

// Función helper para normalizar IDs (convertir números a strings)
const normalizePaciente = (paciente: any): Paciente => ({
  ...paciente,
  id: String(paciente.id),
  propietarioId: String(paciente.propietarioId),
});

export const pacienteService = {
  async getAll(): Promise<Paciente[]> {
    const response = await axios.get<any[]>('/pacientes');
    return response.data.map(normalizePaciente);
  },

  async getById(id: string): Promise<Paciente> {
    const response = await axios.get<any>(`/pacientes/${id}`);
    return normalizePaciente(response.data);
  },

  async search(nombre: string): Promise<Paciente[]> {
    const response = await axios.get<any[]>('/pacientes/buscar', {
      params: { nombre },
    });
    return response.data.map(normalizePaciente);
  },

  async getByPropietario(propietarioId: string): Promise<Paciente[]> {
    const response = await axios.get<any[]>(`/pacientes/propietario/${propietarioId}`);
    return response.data.map(normalizePaciente);
  },

  async create(data: PacienteDTO): Promise<Paciente> {
    const response = await axios.post<any>('/pacientes', data);
    return normalizePaciente(response.data);
  },

  async update(id: string, data: PacienteDTO): Promise<Paciente> {
    const response = await axios.put<any>(`/pacientes/${id}`, data);
    return normalizePaciente(response.data);
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/pacientes/${id}`);
  },
};

