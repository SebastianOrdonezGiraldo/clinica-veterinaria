import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams, Link } from 'react-router-dom';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@shared/components/ui/card';
import { toast } from 'sonner';
import { Lock, CheckCircle2, AlertCircle, ArrowLeft, Loader2, Clock } from 'lucide-react';
import { passwordResetService } from '../services/passwordResetService';

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const userType = searchParams.get('type') || 'cliente';
  
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isValidating, setIsValidating] = useState(true);
  const [isValid, setIsValid] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [expiresAt, setExpiresAt] = useState<Date | null>(null);
  const navigate = useNavigate();
  
  // Countdown para mostrar tiempo restante
  const [countdownSeconds, setCountdownSeconds] = useState(0);
  
  useEffect(() => {
    if (expiresAt) {
      const updateCountdown = () => {
        const remaining = Math.max(0, Math.floor((expiresAt.getTime() - Date.now()) / 1000));
        setCountdownSeconds(remaining);
      };
      
      updateCountdown();
      const interval = setInterval(updateCountdown, 1000);
      
      return () => clearInterval(interval);
    }
  }, [expiresAt]);
  
  const formatTime = (seconds: number) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    return `${hours}h ${minutes}m ${secs}s`;
  };

  useEffect(() => {
    const validateToken = async () => {
      if (!token) {
        setIsValidating(false);
        setIsValid(false);
        return;
      }

      try {
        const tokenInfo = await passwordResetService.validateToken(token);
        setIsValid(tokenInfo.valid);
        
        if (tokenInfo.valid && tokenInfo.expiresAt) {
          setExpiresAt(new Date(tokenInfo.expiresAt));
        }
        
        if (!tokenInfo.valid) {
          toast.error('El enlace de recuperación no es válido o ha expirado');
        }
      } catch (error: any) {
        setIsValid(false);
        toast.error('Error al validar el enlace de recuperación');
      } finally {
        setIsValidating(false);
      }
    };

    validateToken();
  }, [token]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      toast.error('Las contraseñas no coinciden');
      return;
    }

    if (password.length < 6) {
      toast.error('La contraseña debe tener al menos 6 caracteres');
      return;
    }

    if (!token) {
      toast.error('Token de recuperación no válido');
      return;
    }

    setIsLoading(true);

    try {
      await passwordResetService.resetPassword(token, password);
      setIsSuccess(true);
      toast.success('Contraseña restablecida exitosamente');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Error al restablecer la contraseña');
    } finally {
      setIsLoading(false);
    }
  };

  if (isValidating) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-4">
        <Card className="w-full max-w-md">
          <CardContent className="pt-6">
            <div className="text-center space-y-4">
              <Loader2 className="h-8 w-8 animate-spin text-primary mx-auto" />
              <p className="text-muted-foreground">Validando enlace de recuperación...</p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!isValid) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-4">
        <Card className="w-full max-w-md">
          <CardHeader className="text-center space-y-4">
            <div className="flex justify-center">
              <div className="h-16 w-16 rounded-full bg-red-100 flex items-center justify-center">
                <AlertCircle className="h-8 w-8 text-red-600" />
              </div>
            </div>
            <div>
              <CardTitle className="text-2xl">Enlace Inválido</CardTitle>
              <CardDescription>
                El enlace de recuperación no es válido o ha expirado
              </CardDescription>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="p-4 bg-muted rounded-lg">
              <p className="text-sm text-muted-foreground">
                Los enlaces de recuperación expiran después de 24 horas y solo pueden usarse una vez.
                Por favor, solicita un nuevo enlace de recuperación.
              </p>
            </div>
            <div className="space-y-2">
              <Button 
                onClick={() => navigate(userType === 'usuario' ? '/forgot-password' : '/cliente/forgot-password')}
                className="w-full"
              >
                Solicitar Nuevo Enlace
              </Button>
              <Button 
                variant="outline"
                onClick={() => navigate(userType === 'usuario' ? '/login' : '/cliente/login')}
                className="w-full"
              >
                Volver al inicio de sesión
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

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
              <CardTitle className="text-2xl">Contraseña Restablecida</CardTitle>
              <CardDescription>
                Tu contraseña ha sido restablecida exitosamente
              </CardDescription>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="p-4 bg-muted rounded-lg">
              <p className="text-sm text-muted-foreground">
                Ya puedes iniciar sesión con tu nueva contraseña.
              </p>
            </div>
            <Button 
              onClick={() => navigate(userType === 'usuario' ? '/login' : '/cliente/login')}
              className="w-full"
            >
              Ir al inicio de sesión
            </Button>
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
            <CardTitle className="text-2xl">Nueva Contraseña</CardTitle>
            <CardDescription>
              Ingresa tu nueva contraseña
            </CardDescription>
          </div>
          {expiresAt && countdownSeconds > 0 && (
            <div className="p-3 bg-blue-50 border border-blue-200 rounded-lg">
              <div className="flex items-center justify-center gap-2 text-sm text-blue-700">
                <Clock className="h-4 w-4" />
                <span>
                  Este enlace expira en <strong>{formatTime(countdownSeconds)}</strong>
                </span>
              </div>
            </div>
          )}
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="password">Nueva Contraseña</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="pl-10"
                  required
                  disabled={isLoading}
                  autoComplete="new-password"
                  minLength={6}
                />
              </div>
              <p className="text-xs text-muted-foreground">
                Mínimo 6 caracteres
              </p>
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirmar Contraseña</Label>
              <div className="relative">
                <Lock className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="confirmPassword"
                  type="password"
                  placeholder="••••••••"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="pl-10"
                  required
                  disabled={isLoading}
                  autoComplete="new-password"
                  minLength={6}
                />
              </div>
            </div>

            <Button type="submit" className="w-full" disabled={isLoading || countdownSeconds <= 0}>
              {isLoading ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Restableciendo...
                </>
              ) : countdownSeconds <= 0 ? (
                'Enlace Expirado'
              ) : (
                'Restablecer Contraseña'
              )}
            </Button>
          </form>

          <div className="mt-6 text-center">
            <Link 
              to={userType === 'usuario' ? '/login' : '/cliente/login'}
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

