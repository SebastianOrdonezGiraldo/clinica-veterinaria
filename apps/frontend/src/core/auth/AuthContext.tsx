import React, { createContext, useContext, useState, useEffect } from 'react';
import { Usuario, Rol, Propietario } from '@core/types';
import { authService } from '@core/auth/authService';

interface AuthContextType {
  user: Usuario | null;
  cliente: Propietario | null;
  userType: 'SISTEMA' | 'CLIENTE' | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  updateUser: (updatedUser: Usuario) => void;
  updateCliente: (updatedCliente: Propietario) => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<Usuario | null>(null);
  const [cliente, setCliente] = useState<Propietario | null>(null);
  const [userType, setUserType] = useState<'SISTEMA' | 'CLIENTE' | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Verificar si hay sesión guardada
    const token = localStorage.getItem('token');
    const clienteToken = localStorage.getItem('clienteToken');
    const savedUser = localStorage.getItem('user');
    const savedCliente = localStorage.getItem('cliente');
    const savedUserType = localStorage.getItem('userType') as 'SISTEMA' | 'CLIENTE' | null;
    
    if (token && savedUser && savedUserType === 'SISTEMA') {
      // Validar el token con el backend
      authService.validateToken(token)
        .then((isValid) => {
          if (isValid) {
            setUser(JSON.parse(savedUser));
            setUserType('SISTEMA');
          } else {
            // Token inválido
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            localStorage.removeItem('userType');
            setUser(null);
            setUserType(null);
          }
        })
        .catch(() => {
          // Error al validar token
          localStorage.removeItem('token');
          localStorage.removeItem('user');
          localStorage.removeItem('userType');
          setUser(null);
          setUserType(null);
        })
        .finally(() => {
          setIsLoading(false);
        });
    } else if (clienteToken && savedCliente && savedUserType === 'CLIENTE') {
      // Cliente autenticado
      setCliente(JSON.parse(savedCliente));
      setUserType('CLIENTE');
      setIsLoading(false);
    } else {
      setIsLoading(false);
    }
  }, []);

  const login = async (email: string, password: string) => {
    try {
      const response = await authService.login(email, password);
      
      // Guardar token y tipo de usuario
      if (response.userType === 'CLIENTE' && response.propietario) {
        localStorage.setItem('clienteToken', response.token);
        localStorage.setItem('cliente', JSON.stringify(response.propietario));
        localStorage.setItem('userType', 'CLIENTE');
        // Limpiar tokens del sistema si existen
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setCliente(response.propietario);
        setUser(null);
        setUserType('CLIENTE');
      } else if (response.userType === 'SISTEMA' && response.usuario) {
        localStorage.setItem('token', response.token);
        localStorage.setItem('user', JSON.stringify(response.usuario));
        localStorage.setItem('userType', 'SISTEMA');
        // Limpiar tokens de cliente si existen
        localStorage.removeItem('clienteToken');
        localStorage.removeItem('cliente');
        setUser(response.usuario);
        setCliente(null);
        setUserType('SISTEMA');
      }
    } catch (error: any) {
      console.error('Error en login:', error);
      throw new Error(error.response?.data?.message || 'Error al iniciar sesión');
    }
  };

  const logout = () => {
    authService.logout();
    localStorage.removeItem('clienteToken');
    localStorage.removeItem('cliente');
    localStorage.removeItem('userType');
    setUser(null);
    setCliente(null);
    setUserType(null);
  };

  const updateUser = (updatedUser: Usuario) => {
    setUser(updatedUser);
    localStorage.setItem('user', JSON.stringify(updatedUser));
  };

  const updateCliente = (updatedCliente: Propietario) => {
    setCliente(updatedCliente);
    localStorage.setItem('cliente', JSON.stringify(updatedCliente));
  };

  return (
    <AuthContext.Provider value={{ 
      user, 
      cliente, 
      userType, 
      login, 
      logout, 
      updateUser, 
      updateCliente, 
      isLoading 
    }}>
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
