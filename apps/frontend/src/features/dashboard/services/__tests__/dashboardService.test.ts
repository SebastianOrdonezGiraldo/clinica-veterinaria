import { describe, it, expect, vi, beforeEach } from 'vitest';
import axios from '@core/api/axios';
import { dashboardService, DashboardStats } from '../dashboardService';

vi.mock('@core/api/axios');

describe('dashboardService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mockStats: DashboardStats = {
    citasHoy: 5,
    pacientesActivos: 10,
    consultasPendientes: 3,
    totalPropietarios: 8,
    vacunacionesProximas: 2,
    vacunacionesVencidas: 1,
    productosStockBajo: 3,
    prescripcionesMes: 15,
    proximasCitas: [
      {
        id: '1',
        hora: '10:00',
        pacienteNombre: 'Max',
        propietarioNombre: 'Juan Pérez',
        estado: 'CONFIRMADA',
        fecha: '2025-01-15T10:00:00',
      },
    ],
    consultasPorDia: [
      { dia: 'Lun', consultas: 5 },
      { dia: 'Mar', consultas: 3 },
    ],
    distribucionEspecies: [
      { nombre: 'Caninos', valor: 5, color: 'hsl(var(--primary))' },
      { nombre: 'Felinos', valor: 3, color: 'hsl(var(--secondary))' },
    ],
    citasPorEstado: [
      { estado: 'CONFIRMADA', cantidad: 3, color: 'hsl(var(--status-confirmed))' },
      { estado: 'PENDIENTE', cantidad: 2, color: 'hsl(var(--status-pending))' },
    ],
    tendenciasConsultas: [
      { fecha: '01/01', consultas: 5 },
      { fecha: '02/01', consultas: 3 },
    ],
    actividadReciente: [
      {
        tipo: 'CONSULTA',
        descripcion: 'Consulta completada - Max',
        fecha: '15/01/2025 10:00',
        link: '/historias/1',
      },
    ],
  };

  describe('getStats', () => {
    it('debe obtener estadísticas sin filtros', async () => {
      (axios.get as any).mockResolvedValue({ data: mockStats });

      const result = await dashboardService.getStats();

      expect(axios.get).toHaveBeenCalledWith('/dashboard/stats');
      expect(result).toEqual(mockStats);
      expect(result.citasHoy).toBe(5);
      expect(result.pacientesActivos).toBe(10);
    });

    it('debe obtener estadísticas con filtros de fecha', async () => {
      const filters = {
        fechaInicio: '2025-01-01',
        fechaFin: '2025-01-31',
      };

      (axios.get as any).mockResolvedValue({ data: mockStats });

      const result = await dashboardService.getStats(filters);

      expect(axios.get).toHaveBeenCalledWith(
        '/dashboard/stats?fechaInicio=2025-01-01&fechaFin=2025-01-31'
      );
      expect(result).toEqual(mockStats);
    });

    it('debe obtener estadísticas solo con fecha de inicio', async () => {
      const filters = {
        fechaInicio: '2025-01-01',
      };

      (axios.get as any).mockResolvedValue({ data: mockStats });

      const result = await dashboardService.getStats(filters);

      expect(axios.get).toHaveBeenCalledWith(
        '/dashboard/stats?fechaInicio=2025-01-01'
      );
      expect(result).toEqual(mockStats);
    });

    it('debe obtener estadísticas solo con fecha de fin', async () => {
      const filters = {
        fechaFin: '2025-01-31',
      };

      (axios.get as any).mockResolvedValue({ data: mockStats });

      const result = await dashboardService.getStats(filters);

      expect(axios.get).toHaveBeenCalledWith(
        '/dashboard/stats?fechaFin=2025-01-31'
      );
      expect(result).toEqual(mockStats);
    });

    it('debe manejar errores correctamente', async () => {
      const error = new Error('Network error');
      (axios.get as any).mockRejectedValue(error);

      await expect(dashboardService.getStats()).rejects.toThrow('Network error');
    });

    it('debe incluir todas las nuevas métricas', async () => {
      (axios.get as any).mockResolvedValue({ data: mockStats });

      const result = await dashboardService.getStats();

      expect(result.vacunacionesProximas).toBe(2);
      expect(result.vacunacionesVencidas).toBe(1);
      expect(result.productosStockBajo).toBe(3);
      expect(result.prescripcionesMes).toBe(15);
      expect(result.citasPorEstado).toBeDefined();
      expect(result.tendenciasConsultas).toBeDefined();
      expect(result.actividadReciente).toBeDefined();
    });
  });
});

