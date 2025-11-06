export type Rol = 'ADMIN' | 'VET' | 'RECEPCION' | 'ESTUDIANTE';

export interface Usuario {
  id: string;
  nombre: string;
  email: string;
  rol: Rol;
  activo: boolean;
}

export interface Propietario {
  id: string;
  nombre: string;
  documento?: string;
  email?: string;
  telefono?: string;
  direccion?: string;
}

export interface Paciente {
  id: string;
  nombre: string;
  especie: string;
  raza?: string;
  sexo?: 'M' | 'F';
  edadMeses?: number;
  pesoKg?: number;
  propietarioId: string;
  microchip?: string;
  observaciones?: string;
  propietario?: Propietario;
}

export interface Cita {
  id: string;
  pacienteId: string;
  profesionalId: string;
  fecha: string;
  estado: 'PENDIENTE' | 'CONFIRMADA' | 'ATENDIDA' | 'CANCELADA';
  motivo: string;
  observaciones?: string;
  paciente?: Paciente;
  profesional?: Usuario;
}

export interface SignosVitales {
  fc?: number;
  fr?: number;
  temp?: number;
  peso?: number;
}

export interface Consulta {
  id: string;
  pacienteId: string;
  profesionalId: string;
  fecha: string;
  signosVitales?: SignosVitales;
  examen?: string;
  diagnosticos?: string[];
  procedimientos?: string[];
  paciente?: Paciente;
  profesional?: Usuario;
}

export interface PrescripcionItem {
  medicamento: string;
  presentacion?: string;
  dosis: string;
  frecuencia: string;
  duracionDias: number;
  indicaciones?: string;
}

export interface Prescripcion {
  id: string;
  consultaId: string;
  items: PrescripcionItem[];
  consulta?: Consulta;
}
