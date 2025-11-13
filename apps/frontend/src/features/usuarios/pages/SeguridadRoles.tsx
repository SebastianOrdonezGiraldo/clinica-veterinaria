import { useState, useEffect, useMemo } from 'react';
import { Shield, Plus, Edit, Trash2, Check, X, AlertCircle, Loader2, Users } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Badge } from '@shared/components/ui/badge';
import { Checkbox } from '@shared/components/ui/checkbox';
import { Skeleton } from '@shared/components/ui/skeleton';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@shared/components/ui/table';
import { toast } from 'sonner';
import { Usuario, Rol } from '@core/types';
import { usuarioService } from '@features/usuarios/services/usuarioService';

const roleLabels: Record<Rol, string> = {
  ADMIN: 'Administrador',
  VET: 'Veterinario',
  RECEPCION: 'Recepcionista',
  ESTUDIANTE: 'Estudiante',
};

const roleDescriptions: Record<Rol, string> = {
  ADMIN: 'Acceso total al sistema',
  VET: 'Acceso a consultas y tratamientos',
  RECEPCION: 'Gestión de citas y registro',
  ESTUDIANTE: 'Solo lectura de historias',
};

const roleColors: Record<Rol, string> = {
  ADMIN: 'bg-destructive/10 text-destructive',
  VET: 'bg-primary/10 text-primary',
  RECEPCION: 'bg-secondary/10 text-secondary',
  ESTUDIANTE: 'bg-info/10 text-info',
};

const roles: Rol[] = ['ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE'];

const permisosIniciales = [
  { modulo: 'Dashboard', ver: true, crear: false, editar: false, eliminar: false },
  { modulo: 'Pacientes', ver: true, crear: true, editar: true, eliminar: true },
  { modulo: 'Propietarios', ver: true, crear: true, editar: true, eliminar: false },
  { modulo: 'Agenda', ver: true, crear: true, editar: true, eliminar: true },
  { modulo: 'Historias', ver: true, crear: true, editar: true, eliminar: false },
  { modulo: 'Prescripciones', ver: true, crear: true, editar: false, eliminar: false },
  { modulo: 'Reportes', ver: true, crear: false, editar: false, eliminar: false },
  { modulo: 'Seguridad', ver: true, crear: true, editar: true, eliminar: true },
];

type Permiso = {
  modulo: string;
  ver: boolean;
  crear: boolean;
  editar: boolean;
  eliminar: boolean;
};

export default function SeguridadRoles() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [selectedRole, setSelectedRole] = useState<Rol | null>('VET');
  const [permisos, setPermisos] = useState<Permiso[]>(permisosIniciales);
  const [hasChanges, setHasChanges] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
      console.error('Error al cargar usuarios:', error);
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

  // Obtener usuarios del rol seleccionado
  const usuariosDelRol = useMemo(() => {
    if (!selectedRole) return [];
    return usuarios.filter(u => u.rol === selectedRole && u.activo !== false);
  }, [usuarios, selectedRole]);

  const handlePermissionChange = (index: number, field: keyof Omit<Permiso, 'modulo'>) => {
    const newPermisos = [...permisos];
    newPermisos[index] = {
      ...newPermisos[index],
      [field]: !newPermisos[index][field]
    };
    setPermisos(newPermisos);
    setHasChanges(true);
  };

  const handleSave = () => {
    // Nota: Los permisos reales están definidos en el backend con @PreAuthorize
    // Este es solo un resumen visual de los permisos por rol
    toast.success('Nota: Los permisos están definidos en el sistema. Esta es solo una vista informativa.');
    setHasChanges(false);
  };

  const handleCancel = () => {
    setPermisos(permisosIniciales);
    setHasChanges(false);
    toast.info('Cambios descartados');
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Roles y Permisos</h1>
          <p className="text-muted-foreground mt-1">Gestión de roles y control de acceso</p>
        </div>
        <Button 
          className="gap-2" 
          variant="outline"
          onClick={() => toast.info('Los roles están predefinidos en el sistema (ADMIN, VET, RECEPCION, ESTUDIANTE)')}
        >
          <Shield className="h-4 w-4" />
          Info Roles
        </Button>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
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
                      className={`p-3 rounded-lg border cursor-pointer transition-colors ${
                        selectedRole === rol
                          ? 'border-primary bg-primary/5'
                          : 'border-border hover:bg-accent/50'
                      }`}
                      onClick={() => setSelectedRole(rol)}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <h4 className="font-semibold">{roleLabels[rol]}</h4>
                        <Badge className={roleColors[rol]}>{count}</Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">{roleDescriptions[rol]}</p>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>
              Matriz de Permisos - {selectedRole ? roleLabels[selectedRole] : 'Selecciona un rol'}
            </CardTitle>
            <p className="text-sm text-muted-foreground mt-1">
              Nota: Los permisos reales están definidos en el sistema. Esta es una vista informativa.
            </p>
          </CardHeader>
          <CardContent>
            <div className="rounded-lg border">
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
                        <div className="flex justify-center">
                          <Checkbox
                            checked={permiso.ver}
                            onCheckedChange={() => handlePermissionChange(index, 'ver')}
                            className="data-[state=checked]:bg-primary data-[state=checked]:border-primary cursor-pointer"
                          />
                        </div>
                      </TableCell>
                      <TableCell className="text-center">
                        <div className="flex justify-center">
                          <Checkbox
                            checked={permiso.crear}
                            onCheckedChange={() => handlePermissionChange(index, 'crear')}
                            className="data-[state=checked]:bg-primary data-[state=checked]:border-primary cursor-pointer"
                          />
                        </div>
                      </TableCell>
                      <TableCell className="text-center">
                        <div className="flex justify-center">
                          <Checkbox
                            checked={permiso.editar}
                            onCheckedChange={() => handlePermissionChange(index, 'editar')}
                            className="data-[state=checked]:bg-primary data-[state=checked]:border-primary cursor-pointer"
                          />
                        </div>
                      </TableCell>
                      <TableCell className="text-center">
                        <div className="flex justify-center">
                          <Checkbox
                            checked={permiso.eliminar}
                            onCheckedChange={() => handlePermissionChange(index, 'eliminar')}
                            className="data-[state=checked]:bg-primary data-[state=checked]:border-primary cursor-pointer"
                          />
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
            <div className="flex justify-end gap-2 mt-4">
              <Button 
                variant="outline" 
                onClick={handleCancel}
                disabled={!hasChanges}
              >
                Cancelar
              </Button>
              <Button 
                onClick={handleSave}
                disabled={!hasChanges}
              >
                Guardar Cambios
              </Button>
            </div>
            {hasChanges && (
              <p className="text-sm text-warning text-right mt-2">
                Hay cambios sin guardar
              </p>
            )}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Users className="h-5 w-5" />
            Usuarios Asignados - {selectedRole ? roleLabels[selectedRole] : 'Selecciona un rol'}
          </CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-2">
              {[1, 2].map((i) => (
                <Skeleton key={i} className="h-16 w-full" />
              ))}
            </div>
          ) : !selectedRole ? (
            <div className="text-center py-8">
              <Shield className="h-12 w-12 text-muted-foreground mx-auto mb-2" />
              <p className="text-sm text-muted-foreground">Selecciona un rol para ver los usuarios asignados</p>
            </div>
          ) : usuariosDelRol.length === 0 ? (
            <div className="text-center py-8">
              <Users className="h-12 w-12 text-muted-foreground mx-auto mb-2" />
              <p className="text-sm text-muted-foreground">No hay usuarios activos con este rol</p>
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
                  <div key={usuario.id} className="flex items-center justify-between p-3 rounded-lg border border-border">
                    <div className="flex items-center gap-3">
                      <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                        <span className="font-semibold text-primary text-sm">{iniciales}</span>
                      </div>
                      <div>
                        <p className="font-medium">{usuario.nombre}</p>
                        <p className="text-sm text-muted-foreground">{usuario.email}</p>
                      </div>
                    </div>
                    <Badge className={usuario.activo !== false ? 'bg-success/10 text-success border-success/20' : 'bg-muted'}>
                      {usuario.activo !== false ? 'Activo' : 'Inactivo'}
                    </Badge>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
