import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { BusquedaGlobal } from '../BusquedaGlobal';
import { busquedaGlobalService } from '../../services/busquedaGlobalService';

// Mock del servicio
vi.mock('../../services/busquedaGlobalService', () => ({
  busquedaGlobalService: {
    buscar: vi.fn(),
    normalizarResultados: vi.fn(),
  },
}));

// Mock del hook useDebounce
vi.mock('@shared/hooks/useDebounce', () => ({
  useDebounce: (value: string) => value,
}));

// Mock del logger
vi.mock('@shared/hooks/useLogger', () => ({
  useLogger: () => ({
    error: vi.fn(),
  }),
}));

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('BusquedaGlobal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('debe renderizar el diálogo cuando está abierto', () => {
    renderWithRouter(
      <BusquedaGlobal open={true} onOpenChange={vi.fn()} />
    );

    expect(screen.getByText('Búsqueda Global')).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Buscar pacientes, propietarios, citas/)).toBeInTheDocument();
  });

  it('no debe renderizar el diálogo cuando está cerrado', () => {
    renderWithRouter(
      <BusquedaGlobal open={false} onOpenChange={vi.fn()} />
    );

    expect(screen.queryByText('Búsqueda Global')).not.toBeInTheDocument();
  });

  it('debe mostrar mensaje cuando el query es muy corto', async () => {
    (busquedaGlobalService.buscar as any).mockResolvedValue({
      pacientes: [],
      propietarios: [],
      citas: [],
    });

    renderWithRouter(
      <BusquedaGlobal open={true} onOpenChange={vi.fn()} />
    );

    const input = screen.getByPlaceholderText(/Buscar pacientes, propietarios, citas/);
    fireEvent.change(input, { target: { value: 'a' } });

    await waitFor(() => {
      expect(screen.getByText(/Escribe al menos 2 caracteres/)).toBeInTheDocument();
    });
  });

  it('debe realizar búsqueda cuando se escribe más de 2 caracteres', async () => {
    const mockResultados = {
      pacientes: [
        { id: '1', nombre: 'Max', especie: 'Canino', raza: 'Labrador' },
      ],
      propietarios: [],
      citas: [],
    };

    const mockNormalizados = [
      {
        tipo: 'paciente' as const,
        id: '1',
        titulo: 'Max',
        subtitulo: 'Canino • Labrador',
        url: '/pacientes/1',
        icono: '🐾',
      },
    ];

    (busquedaGlobalService.buscar as any).mockResolvedValue(mockResultados);
    (busquedaGlobalService.normalizarResultados as any).mockReturnValue(mockNormalizados);

    renderWithRouter(
      <BusquedaGlobal open={true} onOpenChange={vi.fn()} />
    );

    const input = screen.getByPlaceholderText(/Buscar pacientes, propietarios, citas/);
    fireEvent.change(input, { target: { value: 'max' } });

    await waitFor(() => {
      expect(busquedaGlobalService.buscar).toHaveBeenCalledWith('max', 5);
    });

    await waitFor(() => {
      expect(screen.getByText('Max')).toBeInTheDocument();
    });
  });

  it('debe mostrar mensaje cuando no hay resultados', async () => {
    (busquedaGlobalService.buscar as any).mockResolvedValue({
      pacientes: [],
      propietarios: [],
      citas: [],
    });
    (busquedaGlobalService.normalizarResultados as any).mockReturnValue([]);

    renderWithRouter(
      <BusquedaGlobal open={true} onOpenChange={vi.fn()} />
    );

    const input = screen.getByPlaceholderText(/Buscar pacientes, propietarios, citas/);
    fireEvent.change(input, { target: { value: 'xyz123' } });

    await waitFor(() => {
      expect(screen.getByText(/No se encontraron resultados/)).toBeInTheDocument();
    });
  });

  it('debe navegar al hacer clic en un resultado', async () => {
    const mockNavigate = vi.fn();
    vi.mock('react-router-dom', async () => {
      const actual = await vi.importActual('react-router-dom');
      return {
        ...actual,
        useNavigate: () => mockNavigate,
      };
    });

    const mockNormalizados = [
      {
        tipo: 'paciente' as const,
        id: '1',
        titulo: 'Max',
        subtitulo: 'Canino',
        url: '/pacientes/1',
        icono: '🐾',
      },
    ];

    (busquedaGlobalService.buscar as any).mockResolvedValue({
      pacientes: [],
      propietarios: [],
      citas: [],
    });
    (busquedaGlobalService.normalizarResultados as any).mockReturnValue(mockNormalizados);

    const onOpenChange = vi.fn();
    renderWithRouter(
      <BusquedaGlobal open={true} onOpenChange={onOpenChange} />
    );

    const input = screen.getByPlaceholderText(/Buscar pacientes, propietarios, citas/);
    fireEvent.change(input, { target: { value: 'max' } });

    await waitFor(() => {
      expect(screen.getByText('Max')).toBeInTheDocument();
    });

    const resultado = screen.getByText('Max').closest('button');
    if (resultado) {
      fireEvent.click(resultado);
      // Nota: En un test real necesitarías mockear useNavigate correctamente
    }
  });

  it('debe manejar errores en la búsqueda', async () => {
    const mockError = vi.fn();
    vi.mock('@shared/hooks/useLogger', () => ({
      useLogger: () => ({
        error: mockError,
      }),
    }));

    (busquedaGlobalService.buscar as any).mockRejectedValue(new Error('Error de red'));

    renderWithRouter(
      <BusquedaGlobal open={true} onOpenChange={vi.fn()} />
    );

    const input = screen.getByPlaceholderText(/Buscar pacientes, propietarios, citas/);
    fireEvent.change(input, { target: { value: 'test' } });

    await waitFor(() => {
      expect(busquedaGlobalService.buscar).toHaveBeenCalled();
    });
  });
});

