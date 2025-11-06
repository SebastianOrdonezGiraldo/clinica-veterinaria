import React, { createContext, useContext, useState, useEffect } from 'react';
import { Usuario, Rol } from '@/types';
import { authService } from '@/services/authService';

interface AuthContextType {
  user: Usuario | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<Usuario | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Verificar si hay sesión guardada
    const token = localStorage.getItem('token');
    const savedUser = localStorage.getItem('user');
    
    if (token && savedUser) {
      // Validar el token con el backend
      authService.validateToken(token)
        .then((validatedUser) => {
          setUser(validatedUser);
        })
        .catch(() => {
          // Token inválido o expirado
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          setUser(null);
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
      const response = await authService.login(email, password);
      
      // Guardar token y usuario en localStorage
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response.usuario));
      
      setUser(response.usuario);
    } catch (error: any) {
      console.error('Error en login:', error);
      throw new Error(error.response?.data?.message || 'Error al iniciar sesión');
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth debe usarse dentro de un AuthProvider');
  }
  return context;
}

export function useRequireAuth(allowedRoles?: Rol[]) {
  const { user } = useAuth();
  
  if (!user) {
    return { hasAccess: false, user: null };
  }

  if (allowedRoles && !allowedRoles.includes(user.rol)) {
    return { hasAccess: false, user };
  }

  return { hasAccess: true, user };
}
