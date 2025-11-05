import { Propietario, Paciente, Cita, Usuario, Consulta, Prescripcion } from '@/types';

export const mockUsuarios: Usuario[] = [
  { id: '1', nombre: 'Dr. Admin', email: 'admin@vetclinic.com', rol: 'ADMIN', activo: true },
  { id: '2', nombre: 'Dra. María Pérez', email: 'maria@vetclinic.com', rol: 'VET', activo: true },
  { id: '3', nombre: 'Juan Recepción', email: 'recepcion@vetclinic.com', rol: 'RECEPCION', activo: true },
  { id: '4', nombre: 'Ana Estudiante', email: 'estudiante@vetclinic.com', rol: 'ESTUDIANTE', activo: true },
  { id: '5', nombre: 'Dr. Carlos Ruiz', email: 'carlos@vetclinic.com', rol: 'VET', activo: true },
];

export const mockPropietarios: Propietario[] = [
  { id: '1', nombre: 'Juan Pérez', documento: '12345678', email: 'juan@email.com', telefono: '555-0101', direccion: 'Calle Principal 123' },
  { id: '2', nombre: 'María García', documento: '87654321', email: 'maria@email.com', telefono: '555-0102', direccion: 'Av. Central 456' },
  { id: '3', nombre: 'Carlos López', documento: '11223344', email: 'carlos@email.com', telefono: '555-0103', direccion: 'Jr. Los Olivos 789' },
  { id: '4', nombre: 'Ana Martínez', documento: '44332211', email: 'ana@email.com', telefono: '555-0104', direccion: 'Calle Las Flores 321' },
  { id: '5', nombre: 'Luis Fernández', documento: '55667788', email: 'luis@email.com', telefono: '555-0105', direccion: 'Av. Los Pinos 555' },
];

export const mockPacientes: Paciente[] = [
  { id: '1', nombre: 'Max', especie: 'Canino', raza: 'Golden Retriever', sexo: 'M', edadMeses: 24, pesoKg: 28, propietarioId: '1', microchip: 'MX001' },
  { id: '2', nombre: 'Luna', especie: 'Felino', raza: 'Siamés', sexo: 'F', edadMeses: 18, pesoKg: 3.5, propietarioId: '2', microchip: 'LN001' },
  { id: '3', nombre: 'Rocky', especie: 'Canino', raza: 'Bulldog', sexo: 'M', edadMeses: 36, pesoKg: 22, propietarioId: '3' },
  { id: '4', nombre: 'Mia', especie: 'Felino', raza: 'Persa', sexo: 'F', edadMeses: 12, pesoKg: 4.2, propietarioId: '4', microchip: 'MI001' },
  { id: '5', nombre: 'Toby', especie: 'Canino', raza: 'Labrador', sexo: 'M', edadMeses: 6, pesoKg: 15, propietarioId: '1' },
  { id: '6', nombre: 'Bella', especie: 'Canino', raza: 'Beagle', sexo: 'F', edadMeses: 30, pesoKg: 12, propietarioId: '2' },
];

export const mockCitas: Cita[] = [
  { 
    id: '1', 
    pacienteId: '1', 
    propietarioId: '1', 
    profesionalId: '2', 
    fecha: new Date().toISOString(), 
    estado: 'Confirmada', 
    motivo: 'Vacunación anual' 
  },
  { 
    id: '2', 
    pacienteId: '2', 
    propietarioId: '2', 
    profesionalId: '2', 
    fecha: new Date(Date.now() + 3600000).toISOString(), 
    estado: 'Pendiente', 
    motivo: 'Consulta general' 
  },
  { 
    id: '3', 
    pacienteId: '3', 
    propietarioId: '3', 
    profesionalId: '5', 
    fecha: new Date(Date.now() - 86400000).toISOString(), 
    estado: 'Atendida', 
    motivo: 'Control de peso' 
  },
  { 
    id: '4', 
    pacienteId: '4', 
    propietarioId: '4', 
    profesionalId: '2', 
    fecha: new Date(Date.now() + 7200000).toISOString(), 
    estado: 'Confirmada', 
    motivo: 'Desparasitación' 
  },
];

export const mockConsultas: Consulta[] = [
  {
    id: '1',
    pacienteId: '1',
    profesionalId: '2',
    fecha: new Date(Date.now() - 86400000 * 7).toISOString(),
    signosVitales: { fc: 90, fr: 25, temp: 38.5, peso: 28 },
    examen: 'Paciente activo, alerta. Mucosas rosadas. Linfonodos normales. Auscultación cardiopulmonar sin alteraciones.',
    diagnosticos: ['Salud general óptima', 'Vacunación al día'],
    procedimientos: ['Vacuna antirrábica', 'Desparasitación'],
  },
  {
    id: '2',
    pacienteId: '1',
    profesionalId: '2',
    fecha: new Date(Date.now() - 86400000 * 30).toISOString(),
    signosVitales: { fc: 88, fr: 24, temp: 38.3, peso: 27.5 },
    examen: 'Control rutinario. Estado general bueno.',
    diagnosticos: ['Control preventivo'],
    procedimientos: ['Examen físico completo'],
  },
  {
    id: '3',
    pacienteId: '2',
    profesionalId: '5',
    fecha: new Date(Date.now() - 86400000 * 15).toISOString(),
    signosVitales: { fc: 180, fr: 30, temp: 38.8, peso: 3.5 },
    examen: 'Paciente felino, conducta normal. Pelaje en buen estado.',
    diagnosticos: ['Otitis leve oído derecho'],
    procedimientos: ['Limpieza ótica', 'Prescripción antibiótica'],
  },
];

export const mockPrescripciones: Prescripcion[] = [
  {
    id: '1',
    consultaId: '3',
    items: [
      {
        medicamento: 'Otiderm',
        presentacion: 'Gotas óticas 15ml',
        dosis: '3 gotas',
        frecuencia: 'Cada 12 horas',
        duracionDias: 7,
        indicaciones: 'Aplicar en el oído afectado después de limpieza',
      },
      {
        medicamento: 'Amoxicilina',
        presentacion: 'Suspensión 250mg/5ml',
        dosis: '0.5 ml',
        frecuencia: 'Cada 12 horas',
        duracionDias: 10,
        indicaciones: 'Administrar con alimento',
      },
    ],
  },
];

export function getPacienteById(id: string): Paciente | undefined {
  const paciente = mockPacientes.find(p => p.id === id);
  if (paciente) {
    return {
      ...paciente,
      propietario: mockPropietarios.find(pr => pr.id === paciente.propietarioId)
    };
  }
  return undefined;
}

export function getPropietarioById(id: string): Propietario | undefined {
  return mockPropietarios.find(p => p.id === id);
}

export function getConsultasByPaciente(pacienteId: string): Consulta[] {
  return mockConsultas
    .filter(c => c.pacienteId === pacienteId)
    .map(c => ({
      ...c,
      paciente: getPacienteById(c.pacienteId),
      profesional: mockUsuarios.find(u => u.id === c.profesionalId),
    }))
    .sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime());
}

export function getPrescripcionByConsulta(consultaId: string): Prescripcion | undefined {
  const prescripcion = mockPrescripciones.find(p => p.consultaId === consultaId);
  if (prescripcion) {
    const consulta = mockConsultas.find(c => c.id === consultaId);
    return {
      ...prescripcion,
      consulta: consulta ? {
        ...consulta,
        paciente: getPacienteById(consulta.pacienteId),
        profesional: mockUsuarios.find(u => u.id === consulta.profesionalId),
      } : undefined,
    };
  }
  return undefined;
}
