import axios from '@core/api/axios';
import { Paciente } from '@core/types';

export interface PacienteDTO {
  nombre: string;
  especie: string;
  raza: string;
  sexo: string;
  edadMeses: number;
  pesoKg: number;
  propietarioId: string;
  microchip?: string;
  observaciones?: string;
}

export const pacienteService = {
  async getAll(): Promise<Paciente[]> {
    const response = await axios.get<Paciente[]>('/pacientes');
    return response.data;
  },

  async getById(id: string): Promise<Paciente> {
    const response = await axios.get<Paciente>(`/pacientes/${id}`);
    return response.data;
  },

  async search(nombre: string): Promise<Paciente[]> {
    const response = await axios.get<Paciente[]>('/pacientes/buscar', {
      params: { nombre },
    });
    return response.data;
  },

  async getByPropietario(propietarioId: string): Promise<Paciente[]> {
    const response = await axios.get<Paciente[]>('/pacientes/propietario', {
      params: { propietarioId },
    });
    return response.data;
  },

  async create(data: PacienteDTO): Promise<Paciente> {
    const response = await axios.post<Paciente>('/pacientes', data);
    return response.data;
  },

  async update(id: string, data: PacienteDTO): Promise<Paciente> {
    const response = await axios.put<Paciente>(`/pacientes/${id}`, data);
    return response.data;
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/pacientes/${id}`);
  },
};

