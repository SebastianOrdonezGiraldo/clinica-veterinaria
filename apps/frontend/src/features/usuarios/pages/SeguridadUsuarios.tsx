import { useState, useEffect, useMemo } from 'react';
import { Users, Plus, Search, Mail, Shield, X, AlertCircle, Loader2 } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Badge } from '@shared/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@shared/components/ui/dialog';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from '@shared/components/ui/alert-dialog';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Switch } from '@shared/components/ui/switch';
import { Skeleton } from '@shared/components/ui/skeleton';
import { Usuario, Rol } from '@core/types';
import { usuarioService, UsuarioCreateDTO, UsuarioUpdateDTO } from '@features/usuarios/services/usuarioService';
import { useAuth } from '@core/auth/AuthContext';
import { toast } from 'sonner';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';

const roleLabels: Record<Rol, string> = {
  ADMIN: 'Administrador',
  VET: 'Veterinario',
  RECEPCION: 'Recepcionista',
  ESTUDIANTE: 'Estudiante',
};

const roleColors: Record<Rol, string> = {
  ADMIN: 'bg-destructive/10 text-destructive border-destructive/20',
  VET: 'bg-primary/10 text-primary border-primary/20',
  RECEPCION: 'bg-secondary/10 text-secondary border-secondary/20',
  ESTUDIANTE: 'bg-info/10 text-info border-info/20',
};

// Schema de validación para el formulario
// Usamos un schema base y luego lo refinamos según si es creación o edición
const usuarioSchemaBase = z.object({
  nombre: z.string().min(1, 'El nombre es requerido').max(100, 'El nombre no puede exceder 100 caracteres'),
  email: z.string().email('Email inválido').min(1, 'El email es requerido').max(100, 'El email no puede exceder 100 caracteres'),
  rol: z.enum(['ADMIN', 'VET', 'RECEPCION', 'ESTUDIANTE'], { required_error: 'El rol es requerido' }),
  activo: z.boolean().default(true),
  password: z.string().optional(),
});

type UsuarioFormData = z.infer<typeof usuarioSchemaBase>;

// Schema para reset de contraseña
const resetPasswordSchema = z.object({
  password: z.string().min(6, 'La contraseña debe tener al menos 6 caracteres'),
});

type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>;

export default function SeguridadUsuarios() {
  const { user, isLoading: isLoadingAuth } = useAuth();
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isResetDialogOpen, setIsResetDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<Usuario | null>(null);
  const [resettingUser, setResettingUser] = useState<Usuario | null>(null);
  const [deletingUser, setDeletingUser] = useState<Usuario | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isResetting, setIsResetting] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Verificar si el usuario tiene permisos de ADMIN
  // Como la ruta está protegida con allowedRoles={['ADMIN']}, si el usuario puede acceder, es ADMIN
  // Mostramos los botones siempre que el usuario esté cargado (la ruta ya valida permisos)
  const canManageUsers = !isLoadingAuth && user !== null;

  // Form para crear/editar usuario
  // El schema se valida dinámicamente en onSubmit
  const { register, handleSubmit, formState: { errors }, reset, setValue, watch } = useForm<UsuarioFormData>({
    resolver: zodResolver(usuarioSchemaBase),
    defaultValues: {
      nombre: '',
      email: '',
      rol: 'VET',
      activo: true,
      password: '',
    },
  });

  // Form para reset de contraseña
  const { register: registerReset, handleSubmit: handleSubmitReset, formState: { errors: errorsReset }, reset: resetResetForm } = useForm<ResetPasswordFormData>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: {
      password: '',
    },
  });

  const activo = watch('activo');

  // Debounce para búsqueda
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  // Cargar usuarios
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

  const openCreateDialog = () => {
    setEditingUser(null);
    reset({
      nombre: '',
      email: '',
      rol: 'VET',
      activo: true,
      password: '',
    });
    setIsDialogOpen(true);
  };

  const openEditDialog = (usuario: Usuario) => {
    setEditingUser(usuario);
    reset({
      nombre: usuario.nombre,
      email: usuario.email,
      rol: usuario.rol,
      activo: usuario.activo ?? true,
      password: '', // No se muestra la contraseña
    });
    setIsDialogOpen(true);
  };

  const openResetDialog = (usuario: Usuario) => {
    setResettingUser(usuario);
    resetResetForm({ password: '' });
    setIsResetDialogOpen(true);
  };

  const openDeleteDialog = (usuario: Usuario) => {
    setDeletingUser(usuario);
    setIsDeleteDialogOpen(true);
  };

  const onSubmit = async (data: UsuarioFormData) => {
    try {
      setIsSubmitting(true);
      
      // Validaciones adicionales
      if (!data.nombre || !data.nombre.trim()) {
        toast.error('El nombre es requerido');
        setIsSubmitting(false);
        return;
      }
      
      if (!data.email || !data.email.trim()) {
        toast.error('El email es requerido');
        setIsSubmitting(false);
        return;
      }
      
      // Validar contraseña para nuevos usuarios
      if (!editingUser) {
        if (!data.password || !data.password.trim() || data.password.trim().length < 6) {
          toast.error('La contraseña es requerida y debe tener al menos 6 caracteres');
          setIsSubmitting(false);
          return;
        }
      }

      if (editingUser) {
        const usuarioUpdateData: UsuarioUpdateDTO = {
          nombre: data.nombre.trim(),
          email: data.email.trim().toLowerCase(),
          rol: data.rol,
          activo: data.activo,
          // Solo incluir password si se proporcionó una nueva (mínimo 6 caracteres)
          password: data.password && data.password.trim().length >= 6 ? data.password.trim() : undefined,
        };
        await usuarioService.update(editingUser.id, usuarioUpdateData);
        toast.success('Usuario actualizado exitosamente');
      } else {
        const usuarioCreateData: UsuarioCreateDTO = {
          nombre: data.nombre.trim(),
          email: data.email.trim().toLowerCase(),
          rol: data.rol,
          activo: data.activo,
          password: data.password.trim(),
        };
        await usuarioService.create(usuarioCreateData);
        toast.success('Usuario creado exitosamente');
      }

      setIsDialogOpen(false);
      await loadUsuarios();
    } catch (error: any) {
      console.error('Error al guardar usuario:', error);
      const statusCode = error?.response?.status;
      const errorMessage = error?.response?.data?.message;
      
      if (statusCode === 400) {
        toast.error(errorMessage || 'Error de validación: Verifica que todos los campos sean correctos');
      } else if (statusCode === 403) {
        toast.error('No tienes permisos para realizar esta acción');
      } else if (statusCode === 409 || errorMessage?.includes('ya está registrado')) {
        toast.error('El email ya está registrado en el sistema');
      } else if (statusCode === 500) {
        toast.error('Error del servidor. Por favor, intenta nuevamente');
      } else {
        toast.error(errorMessage || 'Error al guardar el usuario. Por favor, intenta nuevamente');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const onResetPassword = async (data: ResetPasswordFormData) => {
    if (!resettingUser) return;
    
    try {
      setIsResetting(true);
      await usuarioService.resetPassword(resettingUser.id, data.password);
      toast.success(`Contraseña de ${resettingUser.nombre} reiniciada exitosamente`);
      setIsResetDialogOpen(false);
      resetResetForm({ password: '' });
      setResettingUser(null);
    } catch (error: any) {
      console.error('Error al resetear contraseña:', error);
      const statusCode = error?.response?.status;
      const errorMessage = error?.response?.data?.message;
      
      if (statusCode === 400) {
        toast.error(errorMessage || 'Error de validación: La contraseña debe tener al menos 6 caracteres');
      } else if (statusCode === 403) {
        toast.error('No tienes permisos para resetear contraseñas');
      } else if (statusCode === 404) {
        toast.error('Usuario no encontrado');
      } else if (statusCode === 500) {
        toast.error('Error del servidor. Por favor, intenta nuevamente');
      } else {
        toast.error(errorMessage || 'Error al resetear la contraseña. Por favor, intenta nuevamente');
      }
    } finally {
      setIsResetting(false);
    }
  };

  const handleDelete = async () => {
    if (!deletingUser) return;
    
    try {
      setIsDeleting(true);
      await usuarioService.delete(deletingUser.id);
      toast.success('Usuario desactivado exitosamente');
      setIsDeleteDialogOpen(false);
      setDeletingUser(null);
      await loadUsuarios();
    } catch (error: any) {
      console.error('Error al eliminar usuario:', error);
      const statusCode = error?.response?.status;
      const errorMessage = error?.response?.data?.message;
      
      if (statusCode === 403) {
        toast.error('No tienes permisos para eliminar usuarios');
      } else if (statusCode === 404) {
        toast.error('Usuario no encontrado');
      } else if (statusCode === 500) {
        toast.error('Error del servidor. Por favor, intenta nuevamente');
      } else {
        toast.error(errorMessage || 'Error al eliminar el usuario. Por favor, intenta nuevamente');
      }
    } finally {
      setIsDeleting(false);
    }
  };

  const filteredUsuarios = useMemo(() => {
    if (!debouncedSearchTerm) return usuarios;
    const term = debouncedSearchTerm.toLowerCase();
    return usuarios.filter(u =>
      u.nombre.toLowerCase().includes(term) ||
      u.email.toLowerCase().includes(term)
    );
  }, [usuarios, debouncedSearchTerm]);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Gestión de Usuarios</h1>
          <p className="text-muted-foreground mt-1">Administración de usuarios del sistema</p>
        </div>
        {canManageUsers && (
          <Button onClick={openCreateDialog} className="gap-2">
            <Plus className="h-4 w-4" />
            Nuevo Usuario
          </Button>
        )}
      </div>

      <div className="relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Buscar por nombre o email..."
          className="pl-10"
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {isLoading ? (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-6 w-32" />
                <Skeleton className="h-4 w-48 mt-2" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-20 w-full" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : error ? (
        <Card className="border-destructive">
          <CardContent className="flex flex-col items-center justify-center py-16">
            <div className="rounded-full bg-destructive/10 p-4 mb-4">
              <AlertCircle className="h-8 w-8 text-destructive" />
            </div>
            <h3 className="text-lg font-semibold text-foreground mb-2">Error al cargar usuarios</h3>
            <p className="text-sm text-muted-foreground text-center max-w-sm mb-4">{error}</p>
            <Button onClick={loadUsuarios} variant="outline">
              Reintentar
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {filteredUsuarios.map((usuario) => (
          <Card key={usuario.id} className="hover:shadow-lg transition-shadow">
            <CardHeader className="pb-3">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                    <Users className="h-6 w-6 text-primary" />
                  </div>
                  <div>
                    <CardTitle className="text-lg">{usuario.nombre}</CardTitle>
                    <div className="flex items-center gap-1 mt-1 text-sm text-muted-foreground">
                      <Mail className="h-3 w-3" />
                      <span className="text-xs">{usuario.email}</span>
                    </div>
                  </div>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Rol</span>
                <Badge className={roleColors[usuario.rol]}>
                  <Shield className="h-3 w-3 mr-1" />
                  {roleLabels[usuario.rol]}
                </Badge>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-sm text-muted-foreground">Estado</span>
                <Badge className={usuario.activo ? 'bg-success/10 text-success border-success/20' : 'bg-muted'}>
                  {usuario.activo ? 'Activo' : 'Inactivo'}
                </Badge>
              </div>
              {canManageUsers && (
                <div className="flex gap-2 pt-2">
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="flex-1"
                    onClick={() => openEditDialog(usuario)}
                  >
                    Editar
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="flex-1"
                    onClick={() => openResetDialog(usuario)}
                  >
                    Reset Clave
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="flex-1 text-destructive hover:text-destructive"
                    onClick={() => openDeleteDialog(usuario)}
                    disabled={usuario.rol === 'ADMIN'}
                  >
                    <X className="h-3 w-3 mr-1" />
                    Eliminar
                  </Button>
                </div>
              )}
            </CardContent>
          </Card>
          ))}
        </div>
      )}

      {!isLoading && !error && filteredUsuarios.length === 0 && (
        <div className="text-center py-12">
          <Users className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-lg font-medium text-foreground">No se encontraron usuarios</h3>
          <p className="text-muted-foreground mt-1">Intenta con otros términos de búsqueda</p>
        </div>
      )}

      {/* Dialog para crear/editar usuario */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>
              {editingUser ? 'Editar Usuario' : 'Nuevo Usuario'}
            </DialogTitle>
            <DialogDescription>
              {editingUser 
                ? 'Modifica los datos del usuario' 
                : 'Completa la información del nuevo usuario'}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="nombre">Nombre Completo *</Label>
              <Input
                id="nombre"
                {...register('nombre')}
                placeholder="Nombre del usuario"
                disabled={isSubmitting}
              />
              {errors.nombre && (
                <p className="text-sm text-destructive">{errors.nombre.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Email *</Label>
              <Input
                id="email"
                type="email"
                {...register('email')}
                placeholder="correo@ejemplo.com"
                disabled={isSubmitting}
              />
              {errors.email && (
                <p className="text-sm text-destructive">{errors.email.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="rol">Rol *</Label>
              <Select 
                value={watch('rol')} 
                onValueChange={(value: Rol) => setValue('rol', value)}
                disabled={isSubmitting}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ADMIN">Administrador</SelectItem>
                  <SelectItem value="VET">Veterinario</SelectItem>
                  <SelectItem value="RECEPCION">Recepcionista</SelectItem>
                  <SelectItem value="ESTUDIANTE">Estudiante</SelectItem>
                </SelectContent>
              </Select>
              {errors.rol && (
                <p className="text-sm text-destructive">{errors.rol.message}</p>
              )}
            </div>

            {!editingUser && (
              <div className="space-y-2">
                <Label htmlFor="password">Contraseña *</Label>
                <Input
                  id="password"
                  type="password"
                  {...register('password')}
                  placeholder="Mínimo 6 caracteres"
                  disabled={isSubmitting}
                />
                {errors.password && (
                  <p className="text-sm text-destructive">{errors.password.message}</p>
                )}
              </div>
            )}

            {editingUser && (
              <div className="space-y-2">
                <Label htmlFor="password">Nueva Contraseña (opcional)</Label>
                <Input
                  id="password"
                  type="password"
                  {...register('password')}
                  placeholder="Dejar vacío para mantener la actual"
                  disabled={isSubmitting}
                />
                {errors.password && (
                  <p className="text-sm text-destructive">{errors.password.message}</p>
                )}
                <p className="text-xs text-muted-foreground">
                  Solo completa este campo si deseas cambiar la contraseña
                </p>
              </div>
            )}

            <div className="flex items-center justify-between py-2">
              <Label htmlFor="activo">Usuario Activo</Label>
              <Switch
                id="activo"
                checked={activo}
                onCheckedChange={(checked) => setValue('activo', checked)}
                disabled={isSubmitting}
              />
            </div>

            <DialogFooter>
              <Button 
                type="button"
                variant="outline" 
                onClick={() => setIsDialogOpen(false)}
                disabled={isSubmitting}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    {editingUser ? 'Actualizando...' : 'Creando...'}
                  </>
                ) : (
                  editingUser ? 'Actualizar' : 'Crear Usuario'
                )}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Dialog para reset de contraseña */}
      <Dialog open={isResetDialogOpen} onOpenChange={setIsResetDialogOpen}>
        <DialogContent className="sm:max-w-[400px]">
          <DialogHeader>
            <DialogTitle>Resetear Contraseña</DialogTitle>
            <DialogDescription>
              Establece una nueva contraseña para {resettingUser?.nombre}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmitReset(onResetPassword)} className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="reset-password">Nueva Contraseña *</Label>
              <Input
                id="reset-password"
                type="password"
                {...registerReset('password')}
                placeholder="Mínimo 6 caracteres"
                disabled={isResetting}
              />
              {errorsReset.password && (
                <p className="text-sm text-destructive">{errorsReset.password.message}</p>
              )}
            </div>

            <DialogFooter>
              <Button 
                type="button"
                variant="outline" 
                onClick={() => {
                  setIsResetDialogOpen(false);
                  resetResetForm({ password: '' });
                  setResettingUser(null);
                }}
                disabled={isResetting}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={isResetting}>
                {isResetting ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Reseteando...
                  </>
                ) : (
                  'Resetear Contraseña'
                )}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Dialog de confirmación para eliminar */}
      <AlertDialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>¿Desactivar usuario?</AlertDialogTitle>
            <AlertDialogDescription>
              Estás a punto de desactivar al usuario <strong>{deletingUser?.nombre}</strong> ({deletingUser?.email}).
              El usuario no podrá iniciar sesión, pero sus datos se mantendrán en el sistema.
              {deletingUser?.rol === 'ADMIN' && (
                <span className="block mt-2 text-destructive font-medium">
                  ⚠️ No puedes desactivar usuarios administradores.
                </span>
              )}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel onClick={() => {
              setIsDeleteDialogOpen(false);
              setDeletingUser(null);
            }} disabled={isDeleting}>
              Cancelar
            </AlertDialogCancel>
            <AlertDialogAction
              onClick={handleDelete}
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={isDeleting || deletingUser?.rol === 'ADMIN'}
            >
              {isDeleting ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Desactivando...
                </>
              ) : (
                'Desactivar Usuario'
              )}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
