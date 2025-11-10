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
  activo?: boolean;
  pacientesIds?: string[];
  createdAt?: string;
  updatedAt?: string;
}

export interface Paciente {
  id: string;
  nombre: string;
  especie: string;
  raza?: string;
  sexo?: string;
  edadMeses?: number;
  pesoKg?: number;
  propietarioId: string;
  microchip?: string;
  notas?: string;
  activo?: boolean;
  propietarioNombre?: string;
  createdAt?: string;
  updatedAt?: string;
  propietario?: Propietario;
}

export interface Cita {
  id: string;
  pacienteId: string;
  propietarioId: string;
  profesionalId: string;
  fecha: string;
  estado: 'PENDIENTE' | 'CONFIRMADA' | 'ATENDIDA' | 'CANCELADA';
  motivo: string;
  observaciones?: string;
  pacienteNombre?: string;
  propietarioNombre?: string;
  profesionalNombre?: string;
  createdAt?: string;
  updatedAt?: string;
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
  frecuenciaCardiaca?: number;
  frecuenciaRespiratoria?: number;
  temperatura?: number;
  pesoKg?: number;
  examenFisico?: string;
  diagnostico?: string;
  tratamiento?: string;
  observaciones?: string;
  pacienteNombre?: string;
  profesionalNombre?: string;
  prescripcionesIds?: string[];
  createdAt?: string;
  updatedAt?: string;
  paciente?: Paciente;
  profesional?: Usuario;
}

export interface PrescripcionItem {
  medicamento: string;
  presentacion?: string;
  dosis: string;
  frecuencia: string;
  duracionDias: number;
  viaAdministracion?: 'ORAL' | 'INYECTABLE' | 'TOPICA' | 'OFTALMICA' | 'OTICA' | 'OTRA';
  indicaciones?: string;
}

export interface Prescripcion {
  id: string;
  consultaId: string;
  fechaEmision?: string;
  indicacionesGenerales?: string;
  items: PrescripcionItem[];
  consulta?: Consulta;
  createdAt?: string;
  updatedAt?: string;
}
