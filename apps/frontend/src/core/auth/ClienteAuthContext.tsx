import React, { createContext, useContext, useState, useEffect } from 'react';
import { clienteAuthService, Propietario } from './clienteAuthService';

interface ClienteAuthContextType {
  cliente: Propietario | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

const ClienteAuthContext = createContext<ClienteAuthContextType | undefined>(undefined);

export function ClienteAuthProvider({ children }: { children: React.ReactNode }) {
  const [cliente, setCliente] = useState<Propietario | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Verificar si hay sesión guardada
    const token = localStorage.getItem('clienteToken');
    const savedCliente = localStorage.getItem('cliente');
    
    if (token && savedCliente) {
      // Validar el token con el backend
      clienteAuthService.validateToken(token)
        .then((isValid) => {
          if (isValid) {
            setCliente(JSON.parse(savedCliente));
          } else {
            // Token inválido
            localStorage.removeItem('clienteToken');
            localStorage.removeItem('cliente');
            setCliente(null);
          }
        })
        .catch(() => {
          // Error al validar token
          localStorage.removeItem('clienteToken');
          localStorage.removeItem('cliente');
          setCliente(null);
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = async (email: string, password: string) => {
    try {
      const response = await clienteAuthService.login(email, password);
      
      // Guardar token y cliente en localStorage
      localStorage.setItem('clienteToken', response.token);
      localStorage.setItem('cliente', JSON.stringify(response.propietario));
      
      setCliente(response.propietario);
    } catch (error: any) {
      console.error('Error en login:', error);
      throw new Error(error.response?.data?.message || 'Error al iniciar sesión');
    }
  };

  const logout = () => {
    clienteAuthService.logout();
    setCliente(null);
  };

  return (
    <ClienteAuthContext.Provider value={{ cliente, login, logout, isLoading }}>
      {children}
    </ClienteAuthContext.Provider>
  );
}

export function useClienteAuth() {
  const context = useContext(ClienteAuthContext);
  if (context === undefined) {
    throw new Error('useClienteAuth debe usarse dentro de un ClienteAuthProvider');
  }
  return context;
}

