import { Propietario, Paciente, Cita, Usuario } from '@/types';

export const mockPropietarios: Propietario[] = [
  { id: '1', nombre: 'Juan Pérez', documento: '12345678', email: 'juan@email.com', telefono: '555-0101', direccion: 'Calle Principal 123' },
  { id: '2', nombre: 'María García', documento: '87654321', email: 'maria@email.com', telefono: '555-0102', direccion: 'Av. Central 456' },
  { id: '3', nombre: 'Carlos López', documento: '11223344', email: 'carlos@email.com', telefono: '555-0103', direccion: 'Jr. Los Olivos 789' },
  { id: '4', nombre: 'Ana Martínez', documento: '44332211', email: 'ana@email.com', telefono: '555-0104', direccion: 'Calle Las Flores 321' },
];

export const mockPacientes: Paciente[] = [
  { id: '1', nombre: 'Max', especie: 'Canino', raza: 'Golden Retriever', sexo: 'M', edadMeses: 24, pesoKg: 28, propietarioId: '1', microchip: 'MX001' },
  { id: '2', nombre: 'Luna', especie: 'Felino', raza: 'Siamés', sexo: 'F', edadMeses: 18, pesoKg: 3.5, propietarioId: '2', microchip: 'LN001' },
  { id: '3', nombre: 'Rocky', especie: 'Canino', raza: 'Bulldog', sexo: 'M', edadMeses: 36, pesoKg: 22, propietarioId: '3' },
  { id: '4', nombre: 'Mia', especie: 'Felino', raza: 'Persa', sexo: 'F', edadMeses: 12, pesoKg: 4.2, propietarioId: '4', microchip: 'MI001' },
  { id: '5', nombre: 'Toby', especie: 'Canino', raza: 'Labrador', sexo: 'M', edadMeses: 6, pesoKg: 15, propietarioId: '1' },
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
