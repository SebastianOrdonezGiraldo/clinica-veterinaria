import { useState } from 'react';
import { Shield, Plus, Edit, Trash2, Check, X } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Badge } from '@shared/components/ui/badge';
import { Checkbox } from '@shared/components/ui/checkbox';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@shared/components/ui/table';
import { toast } from 'sonner';

const roles = [
  { id: 'ADMIN', nombre: 'Administrador', descripcion: 'Acceso total al sistema', usuarios: 1, color: 'bg-destructive/10 text-destructive' },
  { id: 'VET', nombre: 'Veterinario', descripcion: 'Acceso a consultas y tratamientos', usuarios: 2, color: 'bg-primary/10 text-primary' },
  { id: 'RECEPCION', nombre: 'Recepcionista', descripcion: 'Gestión de citas y registro', usuarios: 1, color: 'bg-secondary/10 text-secondary' },
  { id: 'ESTUDIANTE', nombre: 'Estudiante', descripcion: 'Solo lectura de historias', usuarios: 1, color: 'bg-info/10 text-info' },
];

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
  const [selectedRole, setSelectedRole] = useState<string | null>('VET');
  const [permisos, setPermisos] = useState<Permiso[]>(permisosIniciales);
  const [hasChanges, setHasChanges] = useState(false);

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
    toast.success('Permisos guardados exitosamente');
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
        <Button className="gap-2">
          <Plus className="h-4 w-4" />
          Nuevo Rol
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
            <div className="space-y-2">
              {roles.map((rol) => (
                <div
                  key={rol.id}
                  className={`p-3 rounded-lg border cursor-pointer transition-colors ${
                    selectedRole === rol.id
                      ? 'border-primary bg-primary/5'
                      : 'border-border hover:bg-accent/50'
                  }`}
                  onClick={() => setSelectedRole(rol.id)}
                >
                  <div className="flex items-center justify-between mb-2">
                    <h4 className="font-semibold">{rol.nombre}</h4>
                    <Badge className={rol.color}>{rol.usuarios}</Badge>
                  </div>
                  <p className="text-sm text-muted-foreground">{rol.descripcion}</p>
                  {rol.id !== 'ADMIN' && (
                    <div className="flex gap-2 mt-3">
                      <Button variant="ghost" size="sm" className="h-7">
                        <Edit className="h-3 w-3 mr-1" />
                        Editar
                      </Button>
                      <Button variant="ghost" size="sm" className="h-7 text-destructive">
                        <Trash2 className="h-3 w-3 mr-1" />
                        Eliminar
                      </Button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle>
              Matriz de Permisos - {roles.find(r => r.id === selectedRole)?.nombre}
            </CardTitle>
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
          <CardTitle>Usuarios Asignados - {roles.find(r => r.id === selectedRole)?.nombre}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-2">
            <div className="flex items-center justify-between p-3 rounded-lg border border-border">
              <div className="flex items-center gap-3">
                <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                  <span className="font-semibold text-primary">MP</span>
                </div>
                <div>
                  <p className="font-medium">Dra. María Pérez</p>
                  <p className="text-sm text-muted-foreground">maria@vetclinic.com</p>
                </div>
              </div>
              <Badge className="bg-success/10 text-success border-success/20">Activo</Badge>
            </div>
            {selectedRole === 'VET' && (
              <div className="flex items-center justify-between p-3 rounded-lg border border-border">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-full bg-primary/10 flex items-center justify-center">
                    <span className="font-semibold text-primary">CR</span>
                  </div>
                  <div>
                    <p className="font-medium">Dr. Carlos Ruiz</p>
                    <p className="text-sm text-muted-foreground">carlos@vetclinic.com</p>
                  </div>
                </div>
                <Badge className="bg-success/10 text-success border-success/20">Activo</Badge>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
