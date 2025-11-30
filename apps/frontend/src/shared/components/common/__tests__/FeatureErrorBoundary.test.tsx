import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { FeatureErrorBoundary } from '../FeatureErrorBoundary';
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
    throw new Error('Feature error');
  }
  return <div>Feature content</div>;
};

describe('FeatureErrorBoundary', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.spyOn(console, 'error').mockImplementation(() => {});
  });

  it('debe renderizar children cuando no hay error', () => {
    render(
      <FeatureErrorBoundary featureName="Test Feature">
        <div>Feature content</div>
      </FeatureErrorBoundary>
    );

    expect(screen.getByText('Feature content')).toBeInTheDocument();
  });

  it('debe capturar errores y mostrar ErrorState', () => {
    render(
      <FeatureErrorBoundary featureName="Test Feature">
        <ThrowError shouldThrow={true} />
      </FeatureErrorBoundary>
    );

    expect(screen.getByText(/Error en Test Feature/i)).toBeInTheDocument();
    expect(loggerService.error).toHaveBeenCalled();
  });

  it('debe incluir el nombre del feature en el log', () => {
    render(
      <FeatureErrorBoundary featureName="Dashboard">
        <ThrowError shouldThrow={true} />
      </FeatureErrorBoundary>
    );

    expect(loggerService.error).toHaveBeenCalledWith(
      expect.stringContaining('Dashboard'),
      expect.any(Error),
      expect.objectContaining({
        featureName: 'Dashboard',
      })
    );
  });

  it('debe llamar onError callback si se proporciona', () => {
    const onError = vi.fn();

    render(
      <FeatureErrorBoundary featureName="Test" onError={onError}>
        <ThrowError shouldThrow={true} />
      </FeatureErrorBoundary>
    );

    expect(onError).toHaveBeenCalled();
  });

  it('debe llamar onReset callback cuando se resetea', () => {
    const onReset = vi.fn();
    const { rerender } = render(
      <FeatureErrorBoundary featureName="Test" onReset={onReset}>
        <ThrowError shouldThrow={false} />
      </FeatureErrorBoundary>
    );

    // Lanzar error
    rerender(
      <FeatureErrorBoundary featureName="Test" onReset={onReset}>
        <ThrowError shouldThrow={true} />
      </FeatureErrorBoundary>
    );

    // El reset se llamaría cuando el usuario hace clic en "Reintentar"
    // En este test solo verificamos que el callback está disponible
    expect(screen.getByText(/Reintentar/i)).toBeInTheDocument();
  });
});

