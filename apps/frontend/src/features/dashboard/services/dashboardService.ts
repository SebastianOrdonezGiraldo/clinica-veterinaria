import axios from '@core/api/axios';

export interface DashboardStats {
  citasHoy: number;
  pacientesActivos: number;
  consultasPendientes: number;
  totalPropietarios: number;
  vacunacionesProximas?: number;
  vacunacionesVencidas?: number;
  productosStockBajo?: number;
  prescripcionesMes?: number;
  proximasCitas: ProximaCita[];
  consultasPorDia: ConsultasPorDia[];
  distribucionEspecies: DistribucionEspecie[];
  citasPorEstado?: CitasPorEstado[];
  tendenciasConsultas?: TendenciaConsulta[];
  actividadReciente?: ActividadReciente[];
}

export interface ProximaCita {
  id: string;
  hora: string;
  pacienteNombre: string;
  propietarioNombre: string;
  estado: string;
  fecha: string;
}

export interface ConsultasPorDia {
  dia: string;
  consultas: number;
}

export interface DistribucionEspecie {
  nombre: string;
  valor: number;
  color: string;
}

export interface CitasPorEstado {
  estado: string;
  cantidad: number;
  color: string;
}

export interface TendenciaConsulta {
  fecha: string;
  consultas: number;
}

export interface ActividadReciente {
  tipo: string;
  descripcion: string;
  fecha: string;
  link: string;
}

export interface DashboardFilters {
  fechaInicio?: string; // formato: YYYY-MM-DD
  fechaFin?: string; // formato: YYYY-MM-DD
}

export const dashboardService = {
  async getStats(filters?: DashboardFilters): Promise<DashboardStats> {
    const params = new URLSearchParams();
    if (filters?.fechaInicio) params.append('fechaInicio', filters.fechaInicio);
    if (filters?.fechaFin) params.append('fechaFin', filters.fechaFin);
    
    const url = params.toString() 
      ? `/dashboard/stats?${params.toString()}`
      : '/dashboard/stats';
    
    const response = await axios.get<DashboardStats>(url);
    return response.data;
  },
};

