import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from '@core/api/axios';
import { busquedaGlobalService } from '../busquedaGlobalService';

// Mock de axios
vi.mock('@core/api/axios', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('busquedaGlobalService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('buscar', () => {
    it('debe retornar arrays vacíos si el query es muy corto', async () => {
      const resultado = await busquedaGlobalService.buscar('a');
      expect(resultado).toEqual({
        pacientes: [],
        propietarios: [],
        citas: [],
      });
      expect(axios.get).not.toHaveBeenCalled();
    });

    it('debe retornar arrays vacíos si el query está vacío', async () => {
      const resultado = await busquedaGlobalService.buscar('');
      expect(resultado).toEqual({
        pacientes: [],
        propietarios: [],
        citas: [],
      });
      expect(axios.get).not.toHaveBeenCalled();
    });

    it('debe realizar búsquedas en paralelo para pacientes y propietarios', async () => {
      const mockPacientes = {
        data: {
          content: [
            { id: 1, nombre: 'Max', especie: 'Canino', raza: 'Labrador', propietarioId: 1 },
          ],
        },
      };

      const mockPropietarios = {
        data: {
          content: [
            { id: 1, nombre: 'Juan Pérez', email: 'juan@email.com' },
          ],
        },
      };

      (axios.get as any)
        .mockResolvedValueOnce(mockPacientes)
        .mockResolvedValueOnce(mockPropietarios);

      const resultado = await busquedaGlobalService.buscar('max');

      expect(axios.get).toHaveBeenCalledTimes(2);
      expect(axios.get).toHaveBeenCalledWith('/pacientes/search', {
        params: {
          nombre: 'max',
          page: 0,
          size: 5,
          sort: 'nombre,asc',
        },
      });
      expect(axios.get).toHaveBeenCalledWith('/propietarios/search', {
        params: {
          nombre: 'max',
          page: 0,
          size: 5,
          sort: 'nombre,asc',
        },
      });

      expect(resultado.pacientes).toHaveLength(1);
      expect(resultado.pacientes[0].id).toBe('1');
      expect(resultado.pacientes[0].nombre).toBe('Max');
      expect(resultado.propietarios).toHaveLength(1);
      expect(resultado.propietarios[0].id).toBe('1');
    });

    it('debe manejar errores en las búsquedas sin fallar', async () => {
      (axios.get as any)
        .mockRejectedValueOnce(new Error('Error en pacientes'))
        .mockResolvedValueOnce({
          data: {
            content: [{ id: 1, nombre: 'Juan' }],
          },
        });

      const resultado = await busquedaGlobalService.buscar('test');

      expect(resultado.pacientes).toEqual([]);
      expect(resultado.propietarios).toHaveLength(1);
    });

    it('debe normalizar los IDs a strings', async () => {
      (axios.get as any)
        .mockResolvedValueOnce({
          data: {
            content: [
              { id: 123, nombre: 'Max', especie: 'Canino', propietarioId: 456 },
            ],
          },
        })
        .mockResolvedValueOnce({
          data: { content: [] },
        });

      const resultado = await busquedaGlobalService.buscar('max');

      expect(resultado.pacientes[0].id).toBe('123');
      expect(resultado.pacientes[0].propietarioId).toBe('456');
    });

    it('debe usar el límite personalizado si se proporciona', async () => {
      (axios.get as any)
        .mockResolvedValueOnce({ data: { content: [] } })
        .mockResolvedValueOnce({ data: { content: [] } });

      await busquedaGlobalService.buscar('test', 10);

      expect(axios.get).toHaveBeenCalledWith('/pacientes/search', {
        params: expect.objectContaining({ size: 10 }),
      });
    });
  });

  describe('normalizarResultados', () => {
    it('debe normalizar pacientes correctamente', () => {
      const resultados = {
        pacientes: [
          {
            id: '1',
            nombre: 'Max',
            especie: 'Canino',
            raza: 'Labrador',
          },
          {
            id: '2',
            nombre: 'Luna',
            especie: 'Felino',
            raza: undefined,
          },
        ],
        propietarios: [],
        citas: [],
      };

      const normalizados = busquedaGlobalService.normalizarResultados(resultados);

      expect(normalizados).toHaveLength(2);
      expect(normalizados[0]).toEqual({
        tipo: 'paciente',
        id: '1',
        titulo: 'Max',
        subtitulo: 'Canino • Labrador',
        url: '/pacientes/1',
        icono: '🐾',
      });
      expect(normalizados[1].subtitulo).toBe('Felino');
    });

    it('debe normalizar propietarios correctamente', () => {
      const resultados = {
        pacientes: [],
        propietarios: [
          {
            id: '1',
            nombre: 'Juan Pérez',
            email: 'juan@email.com',
            telefono: '123456789',
          },
          {
            id: '2',
            nombre: 'María García',
            email: undefined,
            telefono: '987654321',
          },
        ],
        citas: [],
      };

      const normalizados = busquedaGlobalService.normalizarResultados(resultados);

      expect(normalizados).toHaveLength(2);
      expect(normalizados[0]).toEqual({
        tipo: 'propietario',
        id: '1',
        titulo: 'Juan Pérez',
        subtitulo: 'juan@email.com',
        url: '/propietarios/1',
        icono: '👤',
      });
      expect(normalizados[1].subtitulo).toBe('987654321');
    });

    it('debe normalizar citas correctamente', () => {
      const fecha = new Date('2024-01-15T10:30:00');
      const resultados = {
        pacientes: [],
        propietarios: [],
        citas: [
          {
            id: '1',
            pacienteNombre: 'Max',
            fecha: fecha.toISOString(),
            motivo: 'Consulta general',
          },
          {
            id: '2',
            pacienteNombre: undefined,
            fecha: fecha.toISOString(),
            motivo: undefined,
          },
        ],
      };

      const normalizados = busquedaGlobalService.normalizarResultados(resultados);

      expect(normalizados).toHaveLength(2);
      expect(normalizados[0]).toEqual({
        tipo: 'cita',
        id: '1',
        titulo: 'Max',
        subtitulo: expect.stringContaining('Consulta general'),
        url: '/agenda/1',
        icono: '📅',
      });
      expect(normalizados[1].titulo).toBe('Paciente');
      expect(normalizados[1].subtitulo).toContain('Sin motivo');
    });

    it('debe mantener el orden: pacientes, propietarios, citas', () => {
      const resultados = {
        pacientes: [{ id: '1', nombre: 'Paciente', especie: 'Canino' }],
        propietarios: [{ id: '1', nombre: 'Propietario' }],
        citas: [{ id: '1', pacienteNombre: 'Cita', fecha: new Date().toISOString(), motivo: 'Test' }],
      };

      const normalizados = busquedaGlobalService.normalizarResultados(resultados);

      expect(normalizados[0].tipo).toBe('paciente');
      expect(normalizados[1].tipo).toBe('propietario');
      expect(normalizados[2].tipo).toBe('cita');
    });
  });
});

