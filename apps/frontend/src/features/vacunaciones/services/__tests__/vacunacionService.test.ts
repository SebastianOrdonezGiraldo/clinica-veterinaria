import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from '@core/api/axios';
import { vacunacionService } from '../vacunacionService';

vi.mock('@core/api/axios');

describe('vacunacionService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getById', () => {
    it('debe obtener una vacunación por ID', async () => {
      const mockVacunacion = {
        id: 1,
        pacienteId: 1,
        vacunaId: 1,
        profesionalId: 1,
        fechaAplicacion: '2025-01-15',
        numeroDosis: 1,
      };

      (axios.get as any).mockResolvedValue({ data: mockVacunacion });

      const result = await vacunacionService.getById('1');

      expect(axios.get).toHaveBeenCalledWith('/vacunaciones/1');
      expect(result.id).toBe('1');
      expect(result.pacienteId).toBe('1');
    });
  });

  describe('getByPaciente', () => {
    it('debe obtener vacunaciones de un paciente', async () => {
      const mockVacunaciones = [
        { id: 1, pacienteId: 1, vacunaId: 1, numeroDosis: 1 },
        { id: 2, pacienteId: 1, vacunaId: 2, numeroDosis: 2 },
      ];

      (axios.get as any).mockResolvedValue({ data: mockVacunaciones });

      const result = await vacunacionService.getByPaciente('1');

      expect(axios.get).toHaveBeenCalledWith('/vacunaciones/paciente/1');
      expect(result).toHaveLength(2);
      expect(result[0].pacienteId).toBe('1');
    });
  });

  describe('create', () => {
    it('debe crear una nueva vacunación', async () => {
      const dto = {
        pacienteId: '1',
        vacunaId: '1',
        profesionalId: '1',
        fechaAplicacion: '2025-01-15',
        numeroDosis: 1,
      };

      const mockResponse = { id: 1, ...dto };

      (axios.post as any).mockResolvedValue({ data: mockResponse });

      const result = await vacunacionService.create(dto);

      expect(axios.post).toHaveBeenCalledWith('/vacunaciones', dto);
      expect(result.id).toBe('1');
      expect(result.pacienteId).toBe('1');
    });
  });

  describe('getProximasAVencer', () => {
    it('debe obtener vacunaciones próximas a vencer', async () => {
      const mockVacunaciones = [
        { id: 1, pacienteId: 1, proximaDosis: '2025-02-01' },
      ];

      (axios.get as any).mockResolvedValue({ data: mockVacunaciones });

      const result = await vacunacionService.getProximasAVencer(30);

      expect(axios.get).toHaveBeenCalledWith('/vacunaciones/proximas?dias=30');
      expect(result).toHaveLength(1);
    });
  });

  describe('getVencidas', () => {
    it('debe obtener vacunaciones vencidas', async () => {
      const mockVacunaciones = [
        { id: 1, pacienteId: 1, proximaDosis: '2024-12-01' },
      ];

      (axios.get as any).mockResolvedValue({ data: mockVacunaciones });

      const result = await vacunacionService.getVencidas();

      expect(axios.get).toHaveBeenCalledWith('/vacunaciones/vencidas');
      expect(result).toHaveLength(1);
    });
  });

  describe('getUltimaVacunacion', () => {
    it('debe obtener la última vacunación de un paciente para una vacuna', async () => {
      const mockVacunacion = {
        id: 1,
        pacienteId: 1,
        vacunaId: 1,
        numeroDosis: 2,
      };

      (axios.get as any).mockResolvedValue({ data: mockVacunacion });

      const result = await vacunacionService.getUltimaVacunacion('1', '1');

      expect(axios.get).toHaveBeenCalledWith('/vacunaciones/paciente/1/vacuna/1/ultima');
      expect(result).not.toBeNull();
      expect(result?.id).toBe('1');
    });

    it('debe retornar null si no hay última vacunación', async () => {
      (axios.get as any).mockRejectedValue({ response: { status: 404 } });

      const result = await vacunacionService.getUltimaVacunacion('1', '1');

      expect(result).toBeNull();
    });
  });
});

