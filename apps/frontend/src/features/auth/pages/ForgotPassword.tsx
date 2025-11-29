import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@shared/components/ui/card';
import { toast } from 'sonner';
import { Mail, ArrowLeft, CheckCircle2, RefreshCw, Loader2 } from 'lucide-react';
import { passwordResetService } from '../services/passwordResetService';
import { useCountdown } from '@shared/hooks/useCountdown';

interface ForgotPasswordProps {
  userType?: 'usuario' | 'cliente';
}

export default function ForgotPassword({ userType = 'usuario' }: ForgotPasswordProps) {
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const navigate = useNavigate();
  
  // Countdown de 60 segundos para reenvío
  const { seconds, start, reset } = useCountdown(60, () => {
    toast.info('Ya puedes solicitar otro email de recuperación');
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      if (userType === 'cliente') {
        await passwordResetService.forgotPasswordCliente(email);
      } else {
        await passwordResetService.forgotPasswordUsuario(email);
      }
      
      setIsSuccess(true);
      reset(); // Resetear contador
      start(); // Iniciar countdown para reenvío
      toast.success('Si el email existe, recibirás un enlace de recuperación en breve.');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Error al solicitar recuperación de contraseña');
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = async () => {
    if (seconds > 0) {
      toast.warning(`Espera ${seconds} segundos antes de solicitar otro email`);
      return;
    }

    setIsLoading(true);
    try {
      if (userType === 'cliente') {
        await passwordResetService.forgotPasswordCliente(email);
      } else {
        await passwordResetService.forgotPasswordUsuario(email);
      }
      
      reset();
      start();
      toast.success('Email de recuperación reenviado');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Error al reenviar email');
    } finally {
      setIsLoading(false);
    }
  };

  if (isSuccess) {
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
              <CardTitle className="text-2xl">Email Enviado</CardTitle>
              <CardDescription>
                Revisa tu bandeja de entrada para continuar
              </CardDescription>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="p-4 bg-muted rounded-lg space-y-3">
              <p className="text-sm text-muted-foreground">
                Si el email <strong>{email}</strong> existe en nuestro sistema, 
                recibirás un enlace de recuperación en breve.
              </p>
              <div className="flex items-center gap-2 text-xs text-muted-foreground">
                <Mail className="h-3 w-3" />
                <span>Revisa tu bandeja de entrada y carpeta de spam</span>
              </div>
            </div>
            <div className="space-y-2">
              <Button 
                onClick={() => navigate(userType === 'cliente' ? '/cliente/login' : '/login')}
                className="w-full"
              >
                Volver al inicio de sesión
              </Button>
              <Button 
                variant="outline"
                onClick={handleResend}
                disabled={isLoading || seconds > 0}
                className="w-full"
              >
                {isLoading ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Reenviando...
                  </>
                ) : seconds > 0 ? (
                  <>
                    <RefreshCw className="mr-2 h-4 w-4" />
                    Reenviar en {seconds}s
                  </>
                ) : (
                  <>
                    <RefreshCw className="mr-2 h-4 w-4" />
                    Reenviar Email
                  </>
                )}
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
              <Mail className="h-8 w-8 text-primary" />
            </div>
          </div>
          <div>
            <CardTitle className="text-2xl">Recuperar Contraseña</CardTitle>
            <CardDescription>
              Ingresa tu email y te enviaremos un enlace para restablecer tu contraseña
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Correo Electrónico</Label>
              <div className="relative">
                <Mail className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="email"
                  type="email"
                  placeholder="tu@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="pl-10"
                  required
                  disabled={isLoading}
                  autoComplete="email"
                />
              </div>
            </div>

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Enviando...
                </>
              ) : (
                <>
                  <Mail className="mr-2 h-4 w-4" />
                  Enviar Enlace de Recuperación
                </>
              )}
            </Button>
          </form>

          <div className="mt-6 text-center space-y-2">
            <Link 
              to={userType === 'cliente' ? '/cliente/login' : '/login'}
              className="text-sm text-muted-foreground hover:text-primary inline-flex items-center gap-1"
            >
              <ArrowLeft className="h-4 w-4" />
              Volver al inicio de sesión
            </Link>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

