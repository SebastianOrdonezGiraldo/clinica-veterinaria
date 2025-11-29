import { useState, useEffect } from 'react';
import { useAuth } from '@core/auth/AuthContext';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { User, Mail, Shield, Save, Loader2, Eye, EyeOff } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Badge } from '@shared/components/ui/badge';
import { Separator } from '@shared/components/ui/separator';
import { usuarioService, UsuarioUpdateDTO } from '@features/usuarios/services/usuarioService';
import { Usuario, Rol } from '@core/types';
import { useLogger } from '@shared/hooks/useLogger';
import { useApiError } from '@shared/hooks/useApiError';

const roleLabels: Record<Rol, string> = {
  ADMIN: 'Administrador',
  VET: 'Veterinario',
  RECEPCION: 'Recepcionista',
  ESTUDIANTE: 'Estudiante',
};

// Schema de validación para el perfil
const perfilSchema = z.object({
  nombre: z.string().min(1, 'El nombre es requerido').max(100, 'El nombre no puede exceder 100 caracteres'),
  email: z.string().email('Email inválido').min(1, 'El email es requerido').max(100, 'El email no puede exceder 100 caracteres'),
  password: z.string().optional(),
  confirmPassword: z.string().optional(),
}).refine((data) => {
  // Si se proporciona password, también debe proporcionarse confirmPassword y deben coincidir
  if (data.password && data.password.trim()) {
    return data.confirmPassword && data.password === data.confirmPassword;
  }
  return true;
}, {
  message: 'Las contraseñas no coinciden',
  path: ['confirmPassword'],
}).refine((data) => {
  // Si se proporciona password, debe tener al menos 6 caracteres
  if (data.password && data.password.trim()) {
    return data.password.length >= 6;
  }
  return true;
}, {
  message: 'La contraseña debe tener al menos 6 caracteres',
  path: ['password'],
});

type PerfilFormData = z.infer<typeof perfilSchema>;

export default function Perfil() {
  const logger = useLogger('Perfil');
  const { handleError, showSuccess } = useApiError();
  const { user, updateUser } = useAuth();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const { register, handleSubmit, formState: { errors }, reset, watch } = useForm<PerfilFormData>({
    resolver: zodResolver(perfilSchema),
    defaultValues: {
      nombre: user?.nombre || '',
      email: user?.email || '',
      password: '',
      confirmPassword: '',
    },
  });

  // Actualizar valores del formulario cuando cambie el usuario
  useEffect(() => {
    if (user) {
      reset({
        nombre: user.nombre,
        email: user.email,
        password: '',
        confirmPassword: '',
      });
    }
  }, [user, reset]);

  const onSubmit = async (data: PerfilFormData) => {
    if (!user) {
      handleError(new Error('No se pudo obtener la información del usuario'), 'No se pudo obtener la información del usuario');
      return;
    }

    try {
      setIsSubmitting(true);

      // Validaciones adicionales
      if (!data.nombre || !data.nombre.trim()) {
        handleError(new Error('El nombre es requerido'), 'El nombre es requerido');
        setIsSubmitting(false);
        return;
      }

      if (!data.email || !data.email.trim()) {
        handleError(new Error('El email es requerido'), 'El email es requerido');
        setIsSubmitting(false);
        return;
      }

      // Validar contraseña si se proporciona
      if (data.password && data.password.trim()) {
        if (data.password.trim().length < 6) {
          handleError(new Error('La contraseña debe tener al menos 6 caracteres'), 'La contraseña debe tener al menos 6 caracteres');
          setIsSubmitting(false);
          return;
        }

        if (data.password !== data.confirmPassword) {
          handleError(new Error('Las contraseñas no coinciden'), 'Las contraseñas no coinciden');
          setIsSubmitting(false);
          return;
        }
      }

      // Preparar datos para actualización
      const updateData: UsuarioUpdateDTO = {
        nombre: data.nombre.trim(),
        email: data.email.trim().toLowerCase(),
        rol: user.rol, // No se puede cambiar el rol desde el perfil
        activo: user.activo, // No se puede cambiar el estado desde el perfil
        // Solo incluir password si se proporcionó una nueva
        password: data.password && data.password.trim().length >= 6 ? data.password.trim() : undefined,
      };

      // Actualizar perfil usando el endpoint especial para el usuario actual
      const updatedUser = await usuarioService.updateMyProfile({
        nombre: updateData.nombre,
        email: updateData.email,
        password: updateData.password,
      });

      // Actualizar el usuario en el contexto de autenticación
      updateUser(updatedUser);

      showSuccess('Perfil actualizado exitosamente');
      
      // Limpiar campos de contraseña
      reset({
        nombre: updatedUser.nombre,
        email: updatedUser.email,
        password: '',
        confirmPassword: '',
      });
    } catch (error: any) {
      logger.error('Error al actualizar perfil de usuario', error, {
        action: 'updatePerfil',
        userId: user?.id,
      });
      handleError(error, 'Error al actualizar el perfil');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-foreground mb-2">Usuario no encontrado</h1>
          <p className="text-muted-foreground">No se pudo cargar la información del usuario</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Mi Perfil</h1>
        <p className="text-muted-foreground mt-1">Administra tu información personal y configuración de cuenta</p>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* Información del Usuario */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="h-5 w-5" />
              Información Personal
            </CardTitle>
            <CardDescription>
              Actualiza tu información personal y credenciales de acceso
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="nombre">Nombre Completo *</Label>
                <Input
                  id="nombre"
                  {...register('nombre')}
                  placeholder="Tu nombre completo"
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

              <Separator className="my-4" />

              <div className="space-y-2">
                <Label htmlFor="password">Nueva Contraseña (opcional)</Label>
                <div className="relative">
                  <Input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    {...register('password')}
                    placeholder="Dejar vacío para mantener la actual"
                    disabled={isSubmitting}
                    className="pr-10"
                  />
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                    onClick={() => setShowPassword(!showPassword)}
                    disabled={isSubmitting}
                  >
                    {showPassword ? (
                      <EyeOff className="h-4 w-4 text-muted-foreground" />
                    ) : (
                      <Eye className="h-4 w-4 text-muted-foreground" />
                    )}
                  </Button>
                </div>
                {errors.password && (
                  <p className="text-sm text-destructive">{errors.password.message}</p>
                )}
                <p className="text-xs text-muted-foreground">
                  Solo completa este campo si deseas cambiar tu contraseña
                </p>
              </div>

              {watch('password') && watch('password')?.trim() && (
                <div className="space-y-2">
                  <Label htmlFor="confirmPassword">Confirmar Nueva Contraseña *</Label>
                  <div className="relative">
                    <Input
                      id="confirmPassword"
                      type={showConfirmPassword ? 'text' : 'password'}
                      {...register('confirmPassword')}
                      placeholder="Confirma tu nueva contraseña"
                      disabled={isSubmitting}
                      className="pr-10"
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                      onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                      disabled={isSubmitting}
                    >
                      {showConfirmPassword ? (
                        <EyeOff className="h-4 w-4 text-muted-foreground" />
                      ) : (
                        <Eye className="h-4 w-4 text-muted-foreground" />
                      )}
                    </Button>
                  </div>
                  {errors.confirmPassword && (
                    <p className="text-sm text-destructive">{errors.confirmPassword.message}</p>
                  )}
                </div>
              )}

              <Button type="submit" className="w-full" disabled={isSubmitting}>
                {isSubmitting ? (
                  <>
                    <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                    Guardando...
                  </>
                ) : (
                  <>
                    <Save className="h-4 w-4 mr-2" />
                    Guardar Cambios
                  </>
                )}
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* Información de la Cuenta */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="h-5 w-5" />
              Información de la Cuenta
            </CardTitle>
            <CardDescription>
              Detalles de tu cuenta que no se pueden modificar
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label className="text-muted-foreground">Rol</Label>
              <div>
                <Badge className="bg-primary/10 text-primary border-primary/20">
                  <Shield className="h-3 w-3 mr-1" />
                  {roleLabels[user.rol]}
                </Badge>
              </div>
            </div>

            <div className="space-y-2">
              <Label className="text-muted-foreground">Estado</Label>
              <div>
                <Badge className={user.activo ? 'bg-success/10 text-success border-success/20' : 'bg-muted'}>
                  {user.activo ? 'Activo' : 'Inactivo'}
                </Badge>
              </div>
            </div>

            <Separator className="my-4" />

            <div className="space-y-2">
              <Label className="text-muted-foreground">ID de Usuario</Label>
              <p className="text-sm font-mono text-muted-foreground">{user.id}</p>
            </div>

            <div className="space-y-2">
              <Label className="text-muted-foreground">Email Actual</Label>
              <div className="flex items-center gap-2">
                <Mail className="h-4 w-4 text-muted-foreground" />
                <p className="text-sm">{user.email}</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

