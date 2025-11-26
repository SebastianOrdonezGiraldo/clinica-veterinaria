import { useState } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { clienteService } from '../services/clienteService';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@shared/components/ui/card';
import { toast } from 'sonner';
import { Lock, Mail, CheckCircle2, ArrowLeft } from 'lucide-react';

const establecerPasswordSchema = z.object({
  email: z.string().email('Email inválido'),
  password: z.string().min(6, 'La contraseña debe tener al menos 6 caracteres'),
  confirmPassword: z.string().min(6, 'Confirma tu contraseña'),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Las contraseñas no coinciden',
  path: ['confirmPassword'],
});

type EstablecerPasswordFormData = z.infer<typeof establecerPasswordSchema>;

export default function EstablecerPassword() {
  const [isLoading, setIsLoading] = useState(false);
  const [success, setSuccess] = useState(false);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const emailFromQuery = searchParams.get('email') || '';

  const { register, handleSubmit, formState: { errors }, setValue } = useForm<EstablecerPasswordFormData>({
    resolver: zodResolver(establecerPasswordSchema),
    defaultValues: {
      email: emailFromQuery,
    },
  });

  // Si hay email en la URL, establecerlo en el formulario
  if (emailFromQuery && !errors.email) {
    setValue('email', emailFromQuery);
  }

  const onSubmit = async (data: EstablecerPasswordFormData) => {
    setIsLoading(true);

    try {
      await clienteService.establecerPassword(data.email, data.password);
      toast.success('Contraseña establecida correctamente');
      setSuccess(true);
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Error al establecer la contraseña';
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-4">
        <Card className="w-full max-w-md">
          <CardHeader className="text-center space-y-4">
            <div className="flex justify-center">
              <div className="h-16 w-16 rounded-full bg-green-100 flex items-center justify-center">
                <CheckCircle2 className="h-8 w-8 text-green-600" />
              </div>
            </div>
            <div>
              <CardTitle className="text-2xl">¡Contraseña Establecida!</CardTitle>
              <CardDescription>
                Tu contraseña ha sido establecida correctamente
              </CardDescription>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <p className="text-center text-muted-foreground">
              Ahora puedes iniciar sesión en el portal del cliente para ver tus citas y mascotas.
            </p>
            <div className="flex flex-col gap-2">
              <Button
                onClick={() => navigate('/cliente/login')}
                className="w-full"
              >
                Ir a Iniciar Sesión
              </Button>
              <Button
                variant="outline"
                onClick={() => navigate('/')}
                className="w-full"
              >
                Volver al Inicio
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-4">
          <div className="flex justify-center">
            <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center">
              <Lock className="h-8 w-8 text-primary" />
            </div>
          </div>
          <div>
            <CardTitle className="text-2xl">Establecer Contraseña</CardTitle>
            <CardDescription>
              Crea una contraseña para acceder al portal del cliente
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Correo Electrónico</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="email"
                  type="email"
                  placeholder="tu@email.com"
                  {...register('email')}
                  className="pl-10"
                  disabled={!!emailFromQuery}
                  required
                />
              </div>
              {errors.email && (
                <p className="text-sm text-destructive">{errors.email.message}</p>
              )}
              {emailFromQuery && (
                <p className="text-xs text-muted-foreground">
                  Email obtenido de tu cita agendada
                </p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Nueva Contraseña</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="password"
                  type="password"
                  placeholder="Mínimo 6 caracteres"
                  {...register('password')}
                  className="pl-10"
                  required
                  disabled={isLoading}
                />
              </div>
              {errors.password && (
                <p className="text-sm text-destructive">{errors.password.message}</p>
              )}
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirmar Contraseña</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="Repite tu contraseña"
                  {...register('confirmPassword')}
                  className="pl-10"
                  required
                  disabled={isLoading}
                />
              </div>
              {errors.confirmPassword && (
                <p className="text-sm text-destructive">{errors.confirmPassword.message}</p>
              )}
            </div>

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? 'Estableciendo contraseña...' : 'Establecer Contraseña'}
            </Button>
          </form>

          <div className="mt-6 text-center space-y-2">
            <p className="text-sm text-muted-foreground">
              <Link to="/cliente/login" className="text-primary hover:underline inline-flex items-center gap-1">
                <ArrowLeft className="h-3 w-3" />
                Volver al login
              </Link>
            </p>
            <p className="text-sm text-muted-foreground">
              <Link to="/" className="text-primary hover:underline">
                Volver al inicio
              </Link>
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

