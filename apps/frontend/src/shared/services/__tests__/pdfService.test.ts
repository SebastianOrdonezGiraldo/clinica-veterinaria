import { describe, it, expect, vi, beforeEach } from 'vitest';
import jsPDF from 'jspdf';
import { pdfService } from '../pdfService';
import { Paciente, Propietario, Consulta, Prescripcion, Usuario } from '@core/types';

// Mock de jsPDF
vi.mock('jspdf', () => {
  const mockDoc = {
    setFontSize: vi.fn().mockReturnThis(),
    setFont: vi.fn().mockReturnThis(),
    text: vi.fn().mockReturnThis(),
    addPage: vi.fn().mockReturnThis(),
    save: vi.fn(),
    getNumberOfPages: vi.fn().mockReturnValue(1),
    setPage: vi.fn().mockReturnThis(),
    splitTextToSize: vi.fn((text: string) => [text]),
    setDrawColor: vi.fn().mockReturnThis(),
    line: vi.fn().mockReturnThis(),
    internal: {
      pageSize: {
        getWidth: () => 210,
        getHeight: () => 297,
      },
    },
  };
  return {
    default: vi.fn(() => mockDoc),
  };
});

describe('pdfService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('generarHistorialClinico', () => {
    const mockPaciente: Paciente = {
      id: '1',
      nombre: 'Max',
      especie: 'Canino',
      raza: 'Labrador',
      sexo: 'M',
      edadMeses: 36,
      pesoKg: 30.5,
      propietarioId: '1',
    };

    const mockPropietario: Propietario = {
      id: '1',
      nombre: 'Juan Pérez',
      email: 'juan@email.com',
      telefono: '123456789',
    };

    const mockConsultas: Consulta[] = [
      {
        id: '1',
        pacienteId: '1',
        profesionalId: '1',
        fecha: new Date().toISOString(),
        frecuenciaCardiaca: 90,
        frecuenciaRespiratoria: 25,
        temperatura: 38.5,
        pesoKg: 30.5,
        examenFisico: 'Paciente activo, alerta',
        diagnostico: 'Salud general óptima',
        tratamiento: 'Control rutinario',
        observaciones: 'Sin observaciones',
        profesionalNombre: 'Dr. Veterinario',
      },
    ];

    it('debe generar un PDF con información del paciente', () => {
      pdfService.generarHistorialClinico(mockPaciente, mockPropietario, mockConsultas);

      expect(jsPDF).toHaveBeenCalled();
      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.text).toHaveBeenCalledWith('HISTORIAL CLÍNICO', expect.any(Number), expect.any(Number), { align: 'center' });
      expect(doc.text).toHaveBeenCalledWith(expect.stringContaining('Max'), expect.any(Number), expect.any(Number));
      expect(doc.save).toHaveBeenCalled();
    });

    it('debe incluir información del propietario si está disponible', () => {
      pdfService.generarHistorialClinico(mockPaciente, mockPropietario, []);

      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.text).toHaveBeenCalledWith(expect.stringContaining('Juan Pérez'), expect.any(Number), expect.any(Number));
    });

    it('debe funcionar sin propietario', () => {
      pdfService.generarHistorialClinico(mockPaciente, null, []);

      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.save).toHaveBeenCalled();
    });

    it('debe incluir todas las consultas en el PDF', () => {
      pdfService.generarHistorialClinico(mockPaciente, mockPropietario, mockConsultas);

      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.text).toHaveBeenCalledWith(expect.stringContaining('Consulta del'), expect.any(Number), expect.any(Number));
      expect(doc.text).toHaveBeenCalledWith(expect.stringContaining('Salud general óptima'), expect.any(Number), expect.any(Number));
    });

    it('debe manejar pacientes sin algunos campos opcionales', () => {
      const pacienteMinimo: Paciente = {
        id: '1',
        nombre: 'Luna',
        especie: 'Felino',
        propietarioId: '1',
      };

      pdfService.generarHistorialClinico(pacienteMinimo, null, []);

      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.save).toHaveBeenCalled();
    });

    it('debe generar nombre de archivo correcto', () => {
      pdfService.generarHistorialClinico(mockPaciente, mockPropietario, []);

      const doc = (jsPDF as any).mock.results[0].value;
      const saveCall = doc.save.mock.calls[0][0];
      expect(saveCall).toContain('Historial_Clinico');
      expect(saveCall).toContain('Max');
    });
  });

  describe('generarPrescripcion', () => {
    const mockPrescripcion: Prescripcion = {
      id: '1',
      consultaId: '1',
      fechaEmision: new Date().toISOString(),
      indicacionesGenerales: 'Administrar con alimento',
      items: [
        {
          medicamento: 'Amoxicilina',
          presentacion: 'Suspensión 250mg/5ml',
          dosis: '0.5 ml',
          frecuencia: 'Cada 12 horas',
          duracionDias: 10,
          viaAdministracion: 'ORAL',
          indicaciones: 'Administrar con alimento',
        },
      ],
    };

    const mockConsulta: Consulta = {
      id: '1',
      pacienteId: '1',
      profesionalId: '1',
      fecha: new Date().toISOString(),
      diagnostico: 'Infección respiratoria',
    };

    const mockPaciente: Paciente = {
      id: '1',
      nombre: 'Max',
      especie: 'Canino',
      raza: 'Labrador',
      pesoKg: 30.5,
      propietarioId: '1',
    };

    const mockPropietario: Propietario = {
      id: '1',
      nombre: 'Juan Pérez',
      documento: '12345678',
    };

    const mockProfesional: Usuario = {
      id: '1',
      nombre: 'Dr. Veterinario',
      email: 'vet@clinica.com',
      rol: 'VET',
      activo: true,
    };

    it('debe generar un PDF de prescripción', () => {
      pdfService.generarPrescripcion(
        mockPrescripcion,
        mockConsulta,
        mockPaciente,
        mockPropietario,
        mockProfesional
      );

      expect(jsPDF).toHaveBeenCalled();
      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.text).toHaveBeenCalledWith('PRESCRIPCIÓN MÉDICA', expect.any(Number), expect.any(Number), { align: 'center' });
      expect(doc.save).toHaveBeenCalled();
    });

    it('debe incluir información del medicamento', () => {
      pdfService.generarPrescripcion(
        mockPrescripcion,
        mockConsulta,
        mockPaciente,
        mockPropietario,
        mockProfesional
      );

      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.text).toHaveBeenCalledWith(expect.stringContaining('Amoxicilina'), expect.any(Number), expect.any(Number));
      expect(doc.text).toHaveBeenCalledWith(expect.stringContaining('0.5 ml'), expect.any(Number), expect.any(Number));
    });

    it('debe funcionar sin algunos datos opcionales', () => {
      pdfService.generarPrescripcion(
        mockPrescripcion,
        null,
        null,
        null,
        null
      );

      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.save).toHaveBeenCalled();
    });

    it('debe incluir indicaciones generales si están disponibles', () => {
      pdfService.generarPrescripcion(
        mockPrescripcion,
        mockConsulta,
        mockPaciente,
        mockPropietario,
        mockProfesional
      );

      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.text).toHaveBeenCalledWith(expect.stringContaining('INDICACIONES GENERALES'), expect.any(Number), expect.any(Number));
    });

    it('debe incluir firma del profesional', () => {
      pdfService.generarPrescripcion(
        mockPrescripcion,
        mockConsulta,
        mockPaciente,
        mockPropietario,
        mockProfesional
      );

      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.text).toHaveBeenCalledWith(expect.stringContaining('Dr. Veterinario'), expect.any(Number), expect.any(Number));
    });

    it('debe generar nombre de archivo correcto', () => {
      pdfService.generarPrescripcion(
        mockPrescripcion,
        mockConsulta,
        mockPaciente,
        mockPropietario,
        mockProfesional
      );

      const doc = (jsPDF as any).mock.results[0].value;
      const saveCall = doc.save.mock.calls[0][0];
      expect(saveCall).toContain('Prescripcion');
      expect(saveCall).toContain('Max');
    });

    it('debe manejar múltiples medicamentos', () => {
      const prescripcionMultiple: Prescripcion = {
        ...mockPrescripcion,
        items: [
          ...mockPrescripcion.items,
          {
            medicamento: 'Paracetamol',
            dosis: '1 tableta',
            frecuencia: 'Cada 8 horas',
            duracionDias: 5,
            viaAdministracion: 'ORAL',
          },
        ],
      };

      pdfService.generarPrescripcion(
        prescripcionMultiple,
        mockConsulta,
        mockPaciente,
        mockPropietario,
        mockProfesional
      );

      const doc = (jsPDF as any).mock.results[0].value;
      expect(doc.text).toHaveBeenCalledWith(expect.stringContaining('Amoxicilina'), expect.any(Number), expect.any(Number));
      expect(doc.text).toHaveBeenCalledWith(expect.stringContaining('Paracetamol'), expect.any(Number), expect.any(Number));
    });
  });
});

