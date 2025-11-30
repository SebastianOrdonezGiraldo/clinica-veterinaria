import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from '@core/api/axios';
import { vacunaService } from '../vacunaService';

vi.mock('@core/api/axios');

describe('vacunaService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getAll', () => {
    it('debe obtener todas las vacunas', async () => {
      const mockVacunas = [
        { id: 1, nombre: 'Antirrábica', especie: 'Canino', numeroDosis: 1 },
        { id: 2, nombre: 'Triple Felina', especie: 'Felino', numeroDosis: 3 },
      ];

      (axios.get as any).mockResolvedValue({ data: mockVacunas });

      const result = await vacunaService.getAll();

      expect(axios.get).toHaveBeenCalledWith('/vacunas');
      expect(result).toHaveLength(2);
      expect(result[0].id).toBe('1');
      expect(result[0].nombre).toBe('Antirrábica');
    });
  });

  describe('getById', () => {
    it('debe obtener una vacuna por ID', async () => {
      const mockVacuna = { id: 1, nombre: 'Antirrábica', numeroDosis: 1 };

      (axios.get as any).mockResolvedValue({ data: mockVacuna });

      const result = await vacunaService.getById('1');

      expect(axios.get).toHaveBeenCalledWith('/vacunas/1');
      expect(result.id).toBe('1');
      expect(result.nombre).toBe('Antirrábica');
    });
  });

  describe('create', () => {
    it('debe crear una nueva vacuna', async () => {
      const dto = {
        nombre: 'Nueva Vacuna',
        especie: 'Canino',
        numeroDosis: 2,
        intervaloDias: 21,
      };

      const mockResponse = { id: 1, ...dto };

      (axios.post as any).mockResolvedValue({ data: mockResponse });

      const result = await vacunaService.create(dto);

      expect(axios.post).toHaveBeenCalledWith('/vacunas', dto);
      expect(result.id).toBe('1');
      expect(result.nombre).toBe('Nueva Vacuna');
    });
  });

  describe('update', () => {
    it('debe actualizar una vacuna existente', async () => {
      const dto = {
        nombre: 'Vacuna Actualizada',
        numeroDosis: 3,
      };

      const mockResponse = { id: 1, ...dto };

      (axios.put as any).mockResolvedValue({ data: mockResponse });

      const result = await vacunaService.update('1', dto);

      expect(axios.put).toHaveBeenCalledWith('/vacunas/1', dto);
      expect(result.nombre).toBe('Vacuna Actualizada');
    });
  });

  describe('delete', () => {
    it('debe eliminar una vacuna', async () => {
      (axios.delete as any).mockResolvedValue({ data: {} });

      await vacunaService.delete('1');

      expect(axios.delete).toHaveBeenCalledWith('/vacunas/1');
    });
  });

  describe('searchWithFilters', () => {
    it('debe buscar vacunas con filtros', async () => {
      const params = {
        page: 0,
        size: 20,
        sort: 'nombre,asc',
        nombre: 'Antirrábica',
        especie: 'Canino',
      };

      const mockResponse = {
        content: [{ id: 1, nombre: 'Antirrábica', especie: 'Canino' }],
        totalElements: 1,
        totalPages: 1,
      };

      (axios.get as any).mockResolvedValue({ data: mockResponse });

      const result = await vacunaService.searchWithFilters(params);

      expect(axios.get).toHaveBeenCalled();
      expect(result.content).toHaveLength(1);
      expect(result.totalElements).toBe(1);
    });
  });
});

