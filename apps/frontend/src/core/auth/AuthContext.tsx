import React, { createContext, useContext, useState, useEffect } from 'react';
import { Usuario, Rol, Propietario } from '@core/types';
import { authService } from '@core/auth/authService';
import { loggerService } from '@core/logging/loggerService';

/**
 * Tipo del contexto de autenticación.
 *
 * Define la estructura del estado y funciones disponibles para autenticación.
 *
 * @interface AuthContextType
 */
interface AuthContextType {
  /** Usuario del sistema autenticado (veterinario, admin, recepcionista) */
  user: Usuario | null;
  /** Cliente (propietario) autenticado */
  cliente: Propietario | null;
  /** Tipo de usuario actual */
  userType: 'SISTEMA' | 'CLIENTE' | null;
  /** Función para iniciar sesión */
  login: (email: string, password: string) => Promise<void>;
  /** Función para cerrar sesión */
  logout: () => void;
  /** Función para actualizar datos del usuario del sistema */
  updateUser: (updatedUser: Usuario) => void;
  /** Función para actualizar datos del cliente */
  updateCliente: (updatedCliente: Propietario) => void;
  /** Indica si está cargando el estado de autenticación */
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Proveedor del contexto de autenticación.
 *
 * Maneja el estado de autenticación para dos tipos de usuarios:
 * - Usuarios del sistema: veterinarios, recepcionistas, administradores
 * - Clientes: propietarios de mascotas
 *
 * Persiste la sesión en localStorage y valida tokens al cargar.
 *
 * @component
 *
 * @param {Object} props - Propiedades del componente
 * @param {React.ReactNode} props.children - Componentes hijos que tendrán acceso al contexto
 *
 * @returns {JSX.Element} Provider del contexto de autenticación
 *
 * @example
 * ```tsx
 * // En el App.tsx principal
 * <AuthProvider>
 *   <App />
 * </AuthProvider>
 * ```
 *
 * @see {@link useAuth}
 * @see {@link useRequireAuth}
 */
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
        .catch((error) => {
          // Error al validar token - usar logger centralizado
          loggerService.warn('Error al validar token de usuario del sistema', {
            component: 'AuthContext',
            action: 'validateToken',
          });
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
      // Log estructurado del error de login
      loggerService.error(
        'Error al iniciar sesión',
        error,
        {
          component: 'AuthContext',
          action: 'login',
          email: email, // No es sensible, es solo para debugging
          userType: error.response?.data?.userType,
        }
      );
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

/**
 * Hook para acceder al contexto de autenticación.
 *
 * Proporciona acceso al estado de autenticación y funciones para
 * login, logout y actualización de datos del usuario.
 *
 * @hook
 *
 * @returns {AuthContextType} Estado y funciones de autenticación
 * @throws {Error} Si se usa fuera de AuthProvider
 *
 * @example
 * ```tsx
 * function MyComponent() {
 *   const { user, login, logout, isLoading } = useAuth();
 *
 *   if (isLoading) return <Loading />;
 *   if (!user) return <LoginForm onLogin={login} />;
 *
 *   return (
 *     <div>
 *       <p>Bienvenido, {user.nombre}</p>
 *       <Button onClick={logout}>Cerrar sesión</Button>
 *     </div>
 *   );
 * }
 * ```
 *
 * @see {@link AuthProvider}
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth debe usarse dentro de un AuthProvider');
  }
  return context;
}

/**
 * Hook para verificar autenticación y autorización.
 *
 * Verifica si el usuario actual tiene acceso basado en roles opcionales.
 * Útil para mostrar/ocultar elementos según permisos.
 *
 * @hook
 *
 * @param {Rol[]} [allowedRoles] - Roles permitidos (opcional)
 *
 * @returns {{ hasAccess: boolean, user: Usuario | null }} Estado de acceso y usuario
 *
 * @example
 * ```tsx
 * function AdminButton() {
 *   const { hasAccess } = useRequireAuth(['ADMIN']);
 *
 *   if (!hasAccess) return null;
 *
 *   return <Button>Opciones de Admin</Button>;
 * }
 * ```
 *
 * @see {@link useAuth}
 */
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
