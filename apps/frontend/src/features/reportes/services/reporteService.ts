import axios from '@core/api/axios';

export interface ReporteDTO {
  totalCitas: number;
  totalConsultas: number;
  totalPacientes: number;
  totalVeterinarios: number;
  citasPorEstado: CitasPorEstadoDTO[];
  tendenciaCitas: TendenciaCitasDTO[];
  pacientesPorEspecie: PacientesPorEspecieDTO[];
  atencionesPorVeterinario: AtencionesPorVeterinarioDTO[];
  topMotivosConsulta: TopMotivoConsultaDTO[];
}

export interface CitasPorEstadoDTO {
  estado: string;
  cantidad: number;
}

export interface TendenciaCitasDTO {
  mes: string;
  citas: number;
}

export interface PacientesPorEspecieDTO {
  especie: string;
  cantidad: number;
}

export interface AtencionesPorVeterinarioDTO {
  nombre: string;
  consultas: number;
}

export interface TopMotivoConsultaDTO {
  motivo: string;
  cantidad: number;
  porcentaje: number;
}

export type PeriodoReporte = 'hoy' | 'semana' | 'mes' | 'año';

export const reporteService = {
  async generarReporte(periodo: PeriodoReporte = 'mes'): Promise<ReporteDTO> {
    const response = await axios.get<ReporteDTO>(`/reportes?periodo=${periodo}`);
    return response.data;
  },
};

