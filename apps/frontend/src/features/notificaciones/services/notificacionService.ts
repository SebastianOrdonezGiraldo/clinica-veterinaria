import axios from '@core/api/axios';

export interface Notificacion {
  id: string;
  usuarioId: string;
  titulo: string;
  mensaje: string;
  tipo: 'CITA' | 'CONSULTA' | 'PACIENTE' | 'SISTEMA' | 'PRESCRIPCION' | 'RECORDATORIO';
  leida: boolean;
  fechaCreacion: string;
  entidadTipo?: string;
  entidadId?: string;
}

export interface NotificacionCreateDTO {
  usuarioId: string;
  titulo: string;
  mensaje: string;
  tipo: 'CITA' | 'CONSULTA' | 'PACIENTE' | 'SISTEMA' | 'PRESCRIPCION' | 'RECORDATORIO';
  entidadTipo?: string;
  entidadId?: string;
}

// Función helper para normalizar IDs
const normalizeNotificacion = (notificacion: any): Notificacion => ({
  ...notificacion,
  id: String(notificacion.id),
  usuarioId: String(notificacion.usuarioId),
  entidadId: notificacion.entidadId ? String(notificacion.entidadId) : undefined,
});

export const notificacionService = {
  /**
   * Obtiene todas las notificaciones del usuario autenticado
   */
  async getAll(): Promise<Notificacion[]> {
    const response = await axios.get<any[]>('/notificaciones');
    return response.data.map(normalizeNotificacion);
  },

  /**
   * Obtiene las notificaciones no leídas del usuario autenticado
   */
  async getNoLeidas(): Promise<Notificacion[]> {
    const response = await axios.get<any[]>('/notificaciones/no-leidas');
    return response.data.map(normalizeNotificacion);
  },

  /**
   * Cuenta las notificaciones no leídas del usuario autenticado
   */
  async getCountNoLeidas(): Promise<number> {
    const response = await axios.get<{ count: number }>('/notificaciones/no-leidas/count');
    return response.data.count;
  },

  /**
   * Crea una nueva notificación
   */
  async create(data: NotificacionCreateDTO): Promise<Notificacion> {
    const response = await axios.post<any>('/notificaciones', data);
    return normalizeNotificacion(response.data);
  },

  /**
   * Marca una notificación como leída
   */
  async marcarComoLeida(id: string): Promise<void> {
    await axios.put(`/notificaciones/${id}/leer`);
  },

  /**
   * Marca todas las notificaciones como leídas
   */
  async marcarTodasComoLeidas(): Promise<void> {
    await axios.put('/notificaciones/leer-todas');
  },

  /**
   * Elimina una notificación
   */
  async delete(id: string): Promise<void> {
    await axios.delete(`/notificaciones/${id}`);
  },
};

