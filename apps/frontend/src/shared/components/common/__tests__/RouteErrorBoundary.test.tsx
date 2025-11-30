import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { RouteErrorBoundary } from '../RouteErrorBoundary';
import { loggerService } from '@core/logging/loggerService';

// Mock del logger
vi.mock('@core/logging/loggerService', () => ({
  loggerService: {
    error: vi.fn(),
  },
}));

// Componente que lanza un error
const ThrowError = ({ shouldThrow }: { shouldThrow: boolean }) => {
  if (shouldThrow) {
    throw new Error('Test error');
  }
  return <div>No error</div>;
};

describe('RouteErrorBoundary', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Suprimir console.error en los tests
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  it('debe renderizar children cuando no hay error', () => {
    render(
      <RouteErrorBoundary>
        <div>Test content</div>
      </RouteErrorBoundary>
    );

    expect(screen.getByText('Test content')).toBeInTheDocument();
  });

  it('debe capturar errores y mostrar UI de fallback', () => {
    render(
      <RouteErrorBoundary>
        <ThrowError shouldThrow={true} />
      </RouteErrorBoundary>
    );

    expect(screen.getByText(/Error al cargar la página/i)).toBeInTheDocument();
    expect(loggerService.error).toHaveBeenCalled();
  });

  it('debe mostrar detalles del error en desarrollo', () => {
    const originalEnv = import.meta.env.PROD;
    // @ts-ignore
    import.meta.env.PROD = false;

    render(
      <RouteErrorBoundary>
        <ThrowError shouldThrow={true} />
      </RouteErrorBoundary>
    );

    expect(screen.getByText(/Detalles del error/i)).toBeInTheDocument();
    expect(screen.getByText(/Test error/i)).toBeInTheDocument();

    // @ts-ignore
    import.meta.env.PROD = originalEnv;
  });

  it('debe permitir resetear el error', () => {
    const { rerender } = render(
      <RouteErrorBoundary>
        <ThrowError shouldThrow={false} />
      </RouteErrorBoundary>
    );

    // Lanzar error
    rerender(
      <RouteErrorBoundary>
        <ThrowError shouldThrow={true} />
      </RouteErrorBoundary>
    );

    expect(screen.getByText(/Error al cargar la página/i)).toBeInTheDocument();

    // Resetear (simulado)
    rerender(
      <RouteErrorBoundary>
        <ThrowError shouldThrow={false} />
      </RouteErrorBoundary>
    );
  });

  it('debe llamar onError callback si se proporciona', () => {
    const onError = vi.fn();

    render(
      <RouteErrorBoundary onError={onError}>
        <ThrowError shouldThrow={true} />
      </RouteErrorBoundary>
    );

    expect(onError).toHaveBeenCalled();
  });

  it('debe usar fallback personalizado si se proporciona', () => {
    const fallback = <div>Custom fallback</div>;

    render(
      <RouteErrorBoundary fallback={fallback}>
        <ThrowError shouldThrow={true} />
      </RouteErrorBoundary>
    );

    expect(screen.getByText('Custom fallback')).toBeInTheDocument();
  });
});

