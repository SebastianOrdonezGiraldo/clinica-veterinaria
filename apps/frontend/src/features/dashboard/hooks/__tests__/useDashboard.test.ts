import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useDashboard } from '../useDashboard';
import { dashboardService } from '../../services/dashboardService';
import * as useApiErrorModule from '@shared/hooks/useApiError';

vi.mock('../../services/dashboardService');
vi.mock('@shared/hooks/useApiError');

describe('useDashboard', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });
    vi.clearAllMocks();
    vi.mocked(useApiErrorModule.useApiError).mockReturnValue({
      handleError: vi.fn(),
    } as any);
  });

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );

  const mockStats = {
    citasHoy: 5,
    pacientesActivos: 10,
    consultasPendientes: 3,
    totalPropietarios: 8,
    vacunacionesProximas: 2,
    vacunacionesVencidas: 1,
    productosStockBajo: 3,
    prescripcionesMes: 15,
    proximasCitas: [],
    consultasPorDia: [],
    distribucionEspecies: [],
    citasPorEstado: [],
    tendenciasConsultas: [],
    actividadReciente: [],
  };

  it('debe obtener estadísticas correctamente', async () => {
    vi.mocked(dashboardService.getStats).mockResolvedValue(mockStats);

    const { result } = renderHook(() => useDashboard(), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.stats).toEqual(mockStats);
    expect(result.current.isLoading).toBe(false);
    expect(result.current.error).toBeNull();
  });

  it('debe obtener estadísticas con filtros', async () => {
    const filters = {
      fechaInicio: '2025-01-01',
      fechaFin: '2025-01-31',
    };

    vi.mocked(dashboardService.getStats).mockResolvedValue(mockStats);

    const { result } = renderHook(() => useDashboard(filters), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(dashboardService.getStats).toHaveBeenCalledWith(filters);
    expect(result.current.stats).toEqual(mockStats);
  });

  it('debe manejar estados de carga', async () => {
    vi.mocked(dashboardService.getStats).mockImplementation(
      () => new Promise((resolve) => setTimeout(() => resolve(mockStats), 100))
    );

    const { result } = renderHook(() => useDashboard(), { wrapper });

    expect(result.current.isLoading).toBe(true);
    expect(result.current.stats).toBeUndefined();

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.stats).toEqual(mockStats);
  });

  it('debe manejar errores correctamente', async () => {
    const error = new Error('Error al cargar estadísticas');
    vi.mocked(dashboardService.getStats).mockRejectedValue(error);
    const handleError = vi.fn();
    vi.mocked(useApiErrorModule.useApiError).mockReturnValue({
      handleError,
    } as any);

    const { result } = renderHook(() => useDashboard(), { wrapper });

    await waitFor(() => expect(result.current.error).toBeDefined());

    expect(handleError).toHaveBeenCalledWith(error);
    expect(result.current.stats).toBeUndefined();
  });

  it('debe incluir refetch en el retorno', async () => {
    vi.mocked(dashboardService.getStats).mockResolvedValue(mockStats);

    const { result } = renderHook(() => useDashboard(), { wrapper });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.refetch).toBeDefined();
    expect(typeof result.current.refetch).toBe('function');
  });

  it('debe actualizar query key cuando cambian los filtros', async () => {
    vi.mocked(dashboardService.getStats).mockResolvedValue(mockStats);

    const { result, rerender } = renderHook(
      ({ filters }) => useDashboard(filters),
      {
        wrapper,
        initialProps: { filters: undefined },
      }
    );

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    const filters = { fechaInicio: '2025-01-01' };
    rerender({ filters });

    await waitFor(() => expect(dashboardService.getStats).toHaveBeenCalledWith(filters));
  });
});

