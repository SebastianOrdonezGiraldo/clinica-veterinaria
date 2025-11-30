import axios from '@core/api/axios';

export interface TemplateConsulta {
  id: string;
  nombre: string;
  descripcion?: string;
  categoria?: string;
  examenFisico?: string;
  diagnostico?: string;
  tratamiento?: string;
  observaciones?: string;
  activo: boolean;
  usuarioId?: string;
  usuarioNombre?: string;
  vecesUsado: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface TemplateConsultaCreate {
  nombre: string;
  descripcion?: string;
  categoria?: string;
  examenFisico?: string;
  diagnostico?: string;
  tratamiento?: string;
  observaciones?: string;
}

export const templateConsultaService = {
  async getAll(): Promise<TemplateConsulta[]> {
    const response = await axios.get<TemplateConsulta[]>('/templates/consultas');
    return response.data.map(t => ({ ...t, id: String(t.id) }));
  },

  async getByCategoria(categoria: string): Promise<TemplateConsulta[]> {
    const response = await axios.get<TemplateConsulta[]>(`/templates/consultas/categoria/${categoria}`);
    return response.data.map(t => ({ ...t, id: String(t.id) }));
  },

  async search(nombre: string): Promise<TemplateConsulta[]> {
    const response = await axios.get<TemplateConsulta[]>('/templates/consultas/search', {
      params: { nombre }
    });
    return response.data.map(t => ({ ...t, id: String(t.id) }));
  },

  async getById(id: string): Promise<TemplateConsulta> {
    const response = await axios.get<TemplateConsulta>(`/templates/consultas/${id}`);
    return { ...response.data, id: String(response.data.id) };
  },

  async create(data: TemplateConsultaCreate): Promise<TemplateConsulta> {
    const response = await axios.post<TemplateConsulta>('/templates/consultas', data);
    return { ...response.data, id: String(response.data.id) };
  },

  async update(id: string, data: TemplateConsultaCreate): Promise<TemplateConsulta> {
    const response = await axios.put<TemplateConsulta>(`/templates/consultas/${id}`, data);
    return { ...response.data, id: String(response.data.id) };
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/templates/consultas/${id}`);
  },

  async incrementarUso(id: string): Promise<void> {
    await axios.post(`/templates/consultas/${id}/usar`);
  },
};

