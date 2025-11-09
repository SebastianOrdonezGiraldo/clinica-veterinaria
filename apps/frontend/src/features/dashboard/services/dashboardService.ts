import axios from '@core/api/axios';

export interface DashboardStats {
  citasHoy: number;
  pacientesActivos: number;
  consultasPendientes: number;
  totalPropietarios: number;
  proximasCitas: ProximaCita[];
  consultasPorDia: ConsultasPorDia[];
  distribucionEspecies: DistribucionEspecie[];
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

export const dashboardService = {
  async getStats(): Promise<DashboardStats> {
    const response = await axios.get<DashboardStats>('/dashboard/stats');
    return response.data;
  },
};

