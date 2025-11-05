import React, { createContext, useContext, useState, useEffect } from 'react';
import { Usuario, Rol } from '@/types';

interface AuthContextType {
  user: Usuario | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Usuarios de prueba
const MOCK_USERS: Usuario[] = [
  { id: '1', nombre: 'Dr. Admin', email: 'admin@vetclinic.com', rol: 'ADMIN', activo: true },
  { id: '2', nombre: 'Dra. María Pérez', email: 'maria@vetclinic.com', rol: 'VET', activo: true },
  { id: '3', nombre: 'Juan Recepción', email: 'recepcion@vetclinic.com', rol: 'RECEPCION', activo: true },
  { id: '4', nombre: 'Ana Estudiante', email: 'estudiante@vetclinic.com', rol: 'ESTUDIANTE', activo: true },
];

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<Usuario | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Verificar si hay sesión guardada
    const savedUser = localStorage.getItem('user');
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    }
    setIsLoading(false);
  }, []);

  const login = async (email: string, password: string) => {
    // Simular login (password: "demo123" para todos)
    const foundUser = MOCK_USERS.find(u => u.email === email);
    
    if (!foundUser || password !== 'demo123') {
      throw new Error('Credenciales inválidas');
    }

    setUser(foundUser);
    localStorage.setItem('user', JSON.stringify(foundUser));
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
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
