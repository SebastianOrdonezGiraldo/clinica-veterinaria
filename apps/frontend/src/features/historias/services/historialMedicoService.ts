import axios from '@core/api/axios';

export interface ConsultaTimeline {
  id: string;
  fecha: string;
  profesionalNombre: string;
  diagnostico?: string;
  tratamiento?: string;
  temperatura?: number;
  pesoKg?: number;
  frecuenciaCardiaca?: number;
  frecuenciaRespiratoria?: number;
  tienePrescripciones: boolean;
  cantidadPrescripciones: number;
}

export interface EvolucionSignosVitales {
  fecha: string;
  pesoKg?: number;
  temperatura?: number;
  frecuenciaCardiaca?: number;
  frecuenciaRespiratoria?: number;
}

export interface HistorialMedicamento {
  medicamento: string;
  presentacion?: string;
  dosis?: string;
  frecuencia?: string;
  duracion?: string;
  fechaPrescripcion: string;
  diagnostico?: string;
  profesionalNombre: string;
  consultaId: string;
}

export interface ResumenMedico {
  totalConsultas: number;
  primeraConsulta?: string;
  ultimaConsulta?: string;
  pesoInicial?: number;
  pesoActual?: number;
  variacionPeso?: number;
  temperaturaPromedio?: number;
  frecuenciaCardiacaPromedio?: number;
  frecuenciaRespiratoriaPromedio?: number;
  totalPrescripciones: number;
  diagnosticosFrecuentes: string[];
  medicamentosMasUsados: string[];
}

export interface HistorialMedico {
  pacienteId: string;
  pacienteNombre: string;
  timelineConsultas: ConsultaTimeline[];
  evolucionSignosVitales: EvolucionSignosVitales[];
  historialMedicamentos: HistorialMedicamento[];
  resumen: ResumenMedico;
}

const normalizeHistorial = (data: any): HistorialMedico => ({
  ...data,
  pacienteId: String(data.pacienteId),
  timelineConsultas: data.timelineConsultas?.map((c: any) => ({
    ...c,
    id: String(c.id),
  })) || [],
  evolucionSignosVitales: data.evolucionSignosVitales || [],
  historialMedicamentos: data.historialMedicamentos?.map((m: any) => ({
    ...m,
    consultaId: String(m.consultaId),
  })) || [],
});

export const historialMedicoService = {
  async getHistorialCompleto(pacienteId: string): Promise<HistorialMedico> {
    const response = await axios.get<any>(`/historial-medico/paciente/${pacienteId}`);
    return normalizeHistorial(response.data);
  },
};

