import axios from '@core/api/axios';

export interface TemplatePrescripcionItem {
  id?: string;
  medicamento: string;
  presentacion?: string;
  dosis?: string;
  frecuencia?: string;
  duracion?: string;
  indicaciones?: string;
  orden?: number;
  createdAt?: string;
}

export interface TemplatePrescripcion {
  id: string;
  nombre: string;
  descripcion?: string;
  categoria?: string;
  indicacionesGenerales?: string;
  activo: boolean;
  usuarioId?: string;
  usuarioNombre?: string;
  vecesUsado: number;
  items: TemplatePrescripcionItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface TemplatePrescripcionCreate {
  nombre: string;
  descripcion?: string;
  categoria?: string;
  indicacionesGenerales?: string;
  items?: TemplatePrescripcionItem[];
}

export const templatePrescripcionService = {
  async getAll(): Promise<TemplatePrescripcion[]> {
    const response = await axios.get<TemplatePrescripcion[]>('/templates/prescripciones');
    return response.data.map(t => ({ ...t, id: String(t.id) }));
  },

  async getCategorias(): Promise<string[]> {
    const response = await axios.get<string[]>('/templates/prescripciones/categorias');
    return response.data;
  },

  async getByCategoria(categoria: string): Promise<TemplatePrescripcion[]> {
    const response = await axios.get<TemplatePrescripcion[]>(`/templates/prescripciones/categoria/${categoria}`);
    return response.data.map(t => ({ ...t, id: String(t.id) }));
  },

  async search(nombre: string): Promise<TemplatePrescripcion[]> {
    const response = await axios.get<TemplatePrescripcion[]>('/templates/prescripciones/search', {
      params: { nombre }
    });
    return response.data.map(t => ({ ...t, id: String(t.id) }));
  },

  async getById(id: string): Promise<TemplatePrescripcion> {
    const response = await axios.get<TemplatePrescripcion>(`/templates/prescripciones/${id}`);
    return { ...response.data, id: String(response.data.id) };
  },

  async create(data: TemplatePrescripcionCreate): Promise<TemplatePrescripcion> {
    const response = await axios.post<TemplatePrescripcion>('/templates/prescripciones', data);
    return { ...response.data, id: String(response.data.id) };
  },

  async update(id: string, data: TemplatePrescripcionCreate): Promise<TemplatePrescripcion> {
    const response = await axios.put<TemplatePrescripcion>(`/templates/prescripciones/${id}`, data);
    return { ...response.data, id: String(response.data.id) };
  },

  async delete(id: string): Promise<void> {
    await axios.delete(`/templates/prescripciones/${id}`);
  },

  async incrementarUso(id: string): Promise<void> {
    await axios.post(`/templates/prescripciones/${id}/usar`);
  },
};

