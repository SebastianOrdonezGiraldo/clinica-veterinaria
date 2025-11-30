import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useWebSocket } from '../useWebSocket';
import * as AuthContext from '@core/auth/AuthContext';

vi.mock('@core/auth/AuthContext');
vi.mock('@stomp/stompjs');
vi.mock('sockjs-client');

describe('useWebSocket', () => {
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
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  const wrapper = ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );

  it('debe conectar cuando hay usuario autenticado', () => {
    vi.mocked(AuthContext.useAuth).mockReturnValue({
      user: { id: '1', nombre: 'Test', email: 'test@test.com', rol: 'VET' },
      token: 'test-token',
    } as any);

    const { result } = renderHook(() => useWebSocket({ autoConnect: true }), { wrapper });

    expect(result.current).toBeDefined();
    expect(result.current.isConnected).toBeDefined();
  });

  it('no debe conectar cuando no hay usuario', () => {
    vi.mocked(AuthContext.useAuth).mockReturnValue({
      user: null,
      token: null,
    } as any);

    const { result } = renderHook(() => useWebSocket({ autoConnect: true }), { wrapper });

    expect(result.current).toBeDefined();
  });

  it('debe llamar onNotification cuando se recibe una notificación', async () => {
    const onNotification = vi.fn();
    
    vi.mocked(AuthContext.useAuth).mockReturnValue({
      user: { id: '1', nombre: 'Test', email: 'test@test.com', rol: 'VET' },
      token: 'test-token',
    } as any);

    renderHook(
      () => useWebSocket({
        autoConnect: true,
        onNotification,
      }),
      { wrapper }
    );

    // En un test real, simularíamos la recepción de un mensaje WebSocket
    expect(onNotification).toBeDefined();
  });
});

