import { useState, useEffect, useMemo } from 'react';
import { Shield, Plus, Edit, Trash2, Check, X, AlertCircle, Loader2, Users, Search, RefreshCw, UserCog } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Badge } from '@shared/components/ui/badge';
import { Input } from '@shared/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Skeleton } from '@shared/components/ui/skeleton';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@shared/components/ui/table';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@shared/components/ui/alert-dialog';
import { toast } from 'sonner';
import { Usuario, Rol } from '@core/types';
import { usuarioService, UsuarioUpdateDTO } from '@features/usuarios/services/usuarioService';
import { useLogger } from '@shared/hooks/useLogger';
import { useApiError } from '@shared/hooks/useApiError';

const roleLabels: Record<Rol, string> = {
  ADMIN: 'Administrador',
  VET: 'Veterinario',
  RECEPCION: 'Recepcionista',
  ESTUDIANTE: 'Estudiante',
};

const roleDescriptions: Record<Rol, string> = {
  ADMIN: 'Acceso total al sistema. Puede gestionar usuarios, configuraciones y todos los módulos.',
  VET: 'Acceso a consultas, tratamientos, historias clínicas y prescripciones.',
  RECEPCION: 'Gestión de citas, registro de pacientes y propietarios.',
  ESTUDIANTE: 'Solo lectura de historias clínicas y consultas.',
};

const roleColors: Record<Rol, string> = {
  ADMIN: 'bg-destructive/10 text-destructive',
  VET: 'bg-primary/10 text-primary',
  RECEPCION: 'bg-secondary/10 text-secondary',
  ESTUDIANTE: 'bg-info/10 text-info',
};

const roles: Rol[] = ['ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE'];

// Matriz de permisos por rol (información de referencia)
const permisosPorRol: Record<Rol, Record<string, { ver: boolean; crear: boolean; editar: boolean; eliminar: boolean }>> = {
  ADMIN: {
    'Dashboard': { ver: true, crear: true, editar: true, eliminar: true },
    'Pacientes': { ver: true, crear: true, editar: true, eliminar: true },
    'Propietarios': { ver: true, crear: true, editar: true, eliminar: true },
    'Agenda': { ver: true, crear: true, editar: true, eliminar: true },
    'Consultas': { ver: true, crear: true, editar: true, eliminar: true },
    'Historias': { ver: true, crear: true, editar: true, eliminar: true },
    'Prescripciones': { ver: true, crear: true, editar: true, eliminar: true },
    'Inventario': { ver: true, crear: true, editar: true, eliminar: true },
    'Reportes': { ver: true, crear: true, editar: true, eliminar: true },
    'Seguridad': { ver: true, crear: true, editar: true, eliminar: true },
  },
  VET: {
    'Dashboard': { ver: true, crear: false, editar: false, eliminar: false },
    'Pacientes': { ver: true, crear: true, editar: true, eliminar: false },
    'Propietarios': { ver: true, crear: true, editar: true, eliminar: false },
    'Agenda': { ver: true, crear: true, editar: true, eliminar: true },
    'Consultas': { ver: true, crear: true, editar: true, eliminar: false },
    'Historias': { ver: true, crear: true, editar: true, eliminar: false },
    'Prescripciones': { ver: true, crear: true, editar: true, eliminar: false },
    'Inventario': { ver: true, crear: true, editar: true, eliminar: false },
    'Reportes': { ver: true, crear: false, editar: false, eliminar: false },
    'Seguridad': { ver: false, crear: false, editar: false, eliminar: false },
  },
  RECEPCION: {
    'Dashboard': { ver: true, crear: false, editar: false, eliminar: false },
    'Pacientes': { ver: true, crear: true, editar: true, eliminar: false },
    'Propietarios': { ver: true, crear: true, editar: true, eliminar: false },
    'Agenda': { ver: true, crear: true, editar: true, eliminar: true },
    'Consultas': { ver: false, crear: false, editar: false, eliminar: false },
    'Historias': { ver: true, crear: false, editar: false, eliminar: false },
    'Prescripciones': { ver: false, crear: false, editar: false, eliminar: false },
    'Inventario': { ver: true, crear: true, editar: true, eliminar: false },
    'Reportes': { ver: false, crear: false, editar: false, eliminar: false },
    'Seguridad': { ver: false, crear: false, editar: false, eliminar: false },
  },
  ESTUDIANTE: {
    'Dashboard': { ver: true, crear: false, editar: false, eliminar: false },
    'Pacientes': { ver: true, crear: false, editar: false, eliminar: false },
    'Propietarios': { ver: true, crear: false, editar: false, eliminar: false },
    'Agenda': { ver: true, crear: false, editar: false, eliminar: false },
    'Consultas': { ver: true, crear: false, editar: false, eliminar: false },
    'Historias': { ver: true, crear: false, editar: false, eliminar: false },
    'Prescripciones': { ver: true, crear: false, editar: false, eliminar: false },
    'Inventario': { ver: true, crear: false, editar: false, eliminar: false },
    'Reportes': { ver: false, crear: false, editar: false, eliminar: false },
    'Seguridad': { ver: false, crear: false, editar: false, eliminar: false },
  },
};

const modulos = Object.keys(permisosPorRol.ADMIN);

export default function SeguridadRoles() {
  const logger = useLogger('SeguridadRoles');
  const { handleError, showSuccess } = useApiError();
  
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [selectedRole, setSelectedRole] = useState<Rol | null>('VET');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [updatingUserId, setUpdatingUserId] = useState<string | null>(null);
  const [showChangeRoleDialog, setShowChangeRoleDialog] = useState(false);
  const [userToUpdate, setUserToUpdate] = useState<Usuario | null>(null);
  const [newRole, setNewRole] = useState<Rol | ''>('');

  useEffect(() => {
    loadUsuarios();
  }, []);

  const loadUsuarios = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await usuarioService.getAll();
      setUsuarios(data);
    } catch (error: any) {
      logger.error('Error al cargar usuarios para gestión de roles', error, {
        action: 'loadUsuarios',
      });
      const errorMessage = error?.response?.data?.message || 'Error al cargar los usuarios';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  // Contar usuarios por rol
  const usuariosPorRol = useMemo(() => {
    const counts: Record<Rol, number> = {
      ADMIN: 0,
      VET: 0,
      RECEPCION: 0,
      ESTUDIANTE: 0,
    };
    usuarios.forEach(u => {
      if (u.rol in counts) {
        counts[u.rol as Rol]++;
      }
    });
    return counts;
  }, [usuarios]);

  // Filtrar usuarios del rol seleccionado
  const usuariosDelRol = useMemo(() => {
    if (!selectedRole) return [];
    let filtered = usuarios.filter(u => u.rol === selectedRole && u.activo !== false);
    
    // Aplicar búsqueda
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase().trim();
      filtered = filtered.filter(u => 
        u.nombre.toLowerCase().includes(query) ||
        u.email.toLowerCase().includes(query)
      );
    }
    
    return filtered;
  }, [usuarios, selectedRole, searchQuery]);

  // Obtener permisos del rol seleccionado
  const permisos = useMemo(() => {
    if (!selectedRole) return [];
    return modulos.map(modulo => ({
      modulo,
      ...permisosPorRol[selectedRole][modulo],
    }));
  }, [selectedRole]);

  const handleChangeRole = async () => {
    if (!userToUpdate || !newRole || newRole === '') return;

    try {
      setUpdatingUserId(userToUpdate.id);
      const updateData: UsuarioUpdateDTO = {
        nombre: userToUpdate.nombre,
        email: userToUpdate.email,
        rol: newRole as Rol,
        activo: userToUpdate.activo,
      };
      
      await usuarioService.update(userToUpdate.id, updateData);
      showSuccess(`Rol de ${userToUpdate.nombre} actualizado a ${roleLabels[newRole as Rol]}`);
      await loadUsuarios();
      setShowChangeRoleDialog(false);
      setUserToUpdate(null);
      setNewRole('');
    } catch (error: any) {
      handleError(error, 'Error al actualizar el rol del usuario');
    } finally {
      setUpdatingUserId(null);
    }
  };

  const openChangeRoleDialog = (usuario: Usuario) => {
    setUserToUpdate(usuario);
    setNewRole(usuario.rol);
    setShowChangeRoleDialog(true);
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Roles y Permisos</h1>
          <p className="text-muted-foreground mt-1">Gestión de roles y control de acceso</p>
        </div>
        <div className="flex gap-2">
          <Button 
            variant="outline"
            onClick={loadUsuarios}
            disabled={isLoading}
            className="gap-2"
          >
            <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
            Actualizar
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Panel de Roles */}
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="h-5 w-5 text-primary" />
              Roles del Sistema
            </CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-2">
                {[1, 2, 3, 4].map((i) => (
                  <Skeleton key={i} className="h-24 w-full" />
                ))}
              </div>
            ) : error ? (
              <div className="text-center py-8">
                <AlertCircle className="h-8 w-8 text-destructive mx-auto mb-2" />
                <p className="text-sm text-muted-foreground">{error}</p>
                <Button variant="outline" size="sm" onClick={loadUsuarios} className="mt-4">
                  Reintentar
                </Button>
              </div>
            ) : (
              <div className="space-y-2">
                {roles.map((rolId) => {
                  const rol = rolId as Rol;
                  const count = usuariosPorRol[rol];
                  return (
                    <div
                      key={rol}
                      className={`p-4 rounded-lg border cursor-pointer transition-all ${
                        selectedRole === rol
                          ? 'border-primary bg-primary/5 shadow-sm'
                          : 'border-border hover:bg-accent/50'
                      }`}
                      onClick={() => {
                        setSelectedRole(rol);
                        setSearchQuery(''); // Limpiar búsqueda al cambiar rol
                      }}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <h4 className="font-semibold text-foreground">{roleLabels[rol]}</h4>
                        <Badge className={roleColors[rol]}>{count} usuarios</Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">{roleDescriptions[rol]}</p>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Matriz de Permisos */}
        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>
              Matriz de Permisos - {selectedRole ? roleLabels[selectedRole] : 'Selecciona un rol'}
            </CardTitle>
            <p className="text-sm text-muted-foreground mt-1">
              Vista informativa de los permisos definidos en el sistema para cada rol
            </p>
          </CardHeader>
          <CardContent>
            {!selectedRole ? (
              <div className="text-center py-12">
                <Shield className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground">Selecciona un rol para ver sus permisos</p>
              </div>
            ) : (
              <div className="rounded-lg border overflow-hidden">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-[200px]">Módulo</TableHead>
                      <TableHead className="text-center">Ver</TableHead>
                      <TableHead className="text-center">Crear</TableHead>
                      <TableHead className="text-center">Editar</TableHead>
                      <TableHead className="text-center">Eliminar</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {permisos.map((permiso, index) => (
                      <TableRow key={index}>
                        <TableCell className="font-medium">{permiso.modulo}</TableCell>
                        <TableCell className="text-center">
                          {permiso.ver ? (
                            <Check className="h-5 w-5 text-success mx-auto" />
                          ) : (
                            <X className="h-5 w-5 text-muted-foreground mx-auto" />
                          )}
                        </TableCell>
                        <TableCell className="text-center">
                          {permiso.crear ? (
                            <Check className="h-5 w-5 text-success mx-auto" />
                          ) : (
                            <X className="h-5 w-5 text-muted-foreground mx-auto" />
                          )}
                        </TableCell>
                        <TableCell className="text-center">
                          {permiso.editar ? (
                            <Check className="h-5 w-5 text-success mx-auto" />
                          ) : (
                            <X className="h-5 w-5 text-muted-foreground mx-auto" />
                          )}
                        </TableCell>
                        <TableCell className="text-center">
                          {permiso.eliminar ? (
                            <Check className="h-5 w-5 text-success mx-auto" />
                          ) : (
                            <X className="h-5 w-5 text-muted-foreground mx-auto" />
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Usuarios Asignados */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle className="flex items-center gap-2">
              <Users className="h-5 w-5" />
              Usuarios Asignados - {selectedRole ? roleLabels[selectedRole] : 'Selecciona un rol'}
              {selectedRole && (
                <Badge variant="outline" className="ml-2">
                  {usuariosDelRol.length}
                </Badge>
              )}
            </CardTitle>
            {selectedRole && (
              <div className="flex items-center gap-2">
                <div className="relative w-64">
                  <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                  <Input
                    placeholder="Buscar usuarios..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="pl-8"
                  />
                </div>
              </div>
            )}
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-2">
              {[1, 2].map((i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          ) : !selectedRole ? (
            <div className="text-center py-12">
              <Shield className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-sm text-muted-foreground">Selecciona un rol para ver los usuarios asignados</p>
            </div>
          ) : usuariosDelRol.length === 0 ? (
            <div className="text-center py-12">
              <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-sm text-muted-foreground">
                {searchQuery ? 'No se encontraron usuarios con ese criterio' : 'No hay usuarios activos con este rol'}
              </p>
            </div>
          ) : (
            <div className="space-y-2">
              {usuariosDelRol.map((usuario) => {
                const iniciales = usuario.nombre
                  .split(' ')
                  .map(n => n[0])
                  .join('')
                  .toUpperCase()
                  .slice(0, 2);
                
                return (
                  <div key={usuario.id} className="flex items-center justify-between p-4 rounded-lg border border-border hover:bg-accent/50 transition-colors">
                    <div className="flex items-center gap-3">
                      <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
                        <span className="font-semibold text-primary">{iniciales}</span>
                      </div>
                      <div>
                        <p className="font-medium text-foreground">{usuario.nombre}</p>
                        <p className="text-sm text-muted-foreground">{usuario.email}</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-3">
                      <Badge className={usuario.activo !== false ? 'bg-success/10 text-success border-success/20' : 'bg-muted'}>
                        {usuario.activo !== false ? 'Activo' : 'Inactivo'}
                      </Badge>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => openChangeRoleDialog(usuario)}
                        disabled={updatingUserId === usuario.id}
                        className="gap-2"
                      >
                        <UserCog className="h-4 w-4" />
                        Cambiar Rol
                      </Button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Diálogo para cambiar rol */}
      <AlertDialog open={showChangeRoleDialog} onOpenChange={setShowChangeRoleDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Cambiar Rol de Usuario</AlertDialogTitle>
            <AlertDialogDescription>
              {userToUpdate && (
                <>
                  Estás a punto de cambiar el rol de <strong>{userToUpdate.nombre}</strong> ({userToUpdate.email}).
                  <br />
                  <br />
                  Rol actual: <Badge className={roleColors[userToUpdate.rol]}>{roleLabels[userToUpdate.rol]}</Badge>
                </>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <div className="py-4">
            <label className="text-sm font-medium mb-2 block">Nuevo Rol</label>
            <Select value={newRole} onValueChange={(value) => setNewRole(value as Rol)}>
              <SelectTrigger>
                <SelectValue placeholder="Selecciona un rol" />
              </SelectTrigger>
              <SelectContent>
                {roles.map((rol) => (
                  <SelectItem key={rol} value={rol}>
                    {roleLabels[rol]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => {
              setShowChangeRoleDialog(false);
              setUserToUpdate(null);
              setNewRole('');
            }}>
              Cancelar
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleChangeRole}
              disabled={!newRole || newRole === userToUpdate?.rol || updatingUserId === userToUpdate?.id}
            >
              {updatingUserId === userToUpdate?.id ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Actualizando...
                </>
              ) : (
                'Confirmar Cambio'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
