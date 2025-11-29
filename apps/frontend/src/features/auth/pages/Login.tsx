import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '@core/auth/AuthContext';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@shared/components/ui/card';
import { toast } from 'sonner';
import { Dog, Mail, Lock } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { login, userType } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await login(email, password);
      toast.success('Sesión iniciada correctamente');
      
      // Redirigir según el tipo de usuario
      if (userType === 'CLIENTE') {
        navigate('/cliente/dashboard');
      } else {
        navigate('/dashboard');
      }
    } catch (error: any) {
      toast.error(error.message || 'Credenciales inválidas');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div 
      className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-4"
      role="main"
      aria-label="Página de inicio de sesión"
    >
      <Card className="w-full max-w-md">
        <CardHeader className="text-center space-y-4">
          <div className="flex justify-center" aria-hidden="true">
            <div className="h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center">
              <Dog className="h-8 w-8 text-primary" />
            </div>
          </div>
          <div>
            <CardTitle className="text-2xl" id="login-title">Iniciar Sesión</CardTitle>
            <CardDescription id="login-description">
              Accede al sistema o al portal del cliente
            </CardDescription>
          </div>
        </CardHeader>
        <CardContent>
          <form 
            onSubmit={handleSubmit} 
            className="space-y-4"
            aria-labelledby="login-title"
            aria-describedby="login-description"
            noValidate
          >
            <div className="space-y-2">
              <Label htmlFor="email">Correo Electrónico</Label>
              <div className="relative">
                <Mail 
                  className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" 
                  aria-hidden="true"
                />
                <Input
                  id="email"
                  type="email"
                  placeholder="tu@email.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="pl-10"
                  required
                  disabled={isLoading}
                  aria-required="true"
                  aria-invalid={false}
                  autoComplete="email"
                  aria-describedby="email-description"
                />
              </div>
              <span id="email-description" className="sr-only">
                Ingrese su dirección de correo electrónico
              </span>
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Contraseña</Label>
              <div className="relative">
                <Lock 
                  className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" 
                  aria-hidden="true"
                />
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  autoComplete="current-password"
                  aria-required="true"
                  aria-describedby="password-description"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="pl-10"
                  required
                  disabled={isLoading}
                />
              </div>
              <span id="password-description" className="sr-only">
                Ingrese su contraseña
              </span>
            </div>
            <Button 
              type="submit" 
              className="w-full" 
              disabled={isLoading}
              aria-busy={isLoading}
              aria-label={isLoading ? 'Iniciando sesión, por favor espere' : 'Iniciar sesión'}
            >
              {isLoading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
            </Button>
          </form>

          <div className="mt-4 text-center">
            <Link 
              to="/forgot-password"
              className="text-sm text-primary hover:underline"
            >
              ¿Olvidaste tu contraseña?
            </Link>
          </div>

          <div className="mt-6 space-y-4">
            <div className="p-4 bg-muted rounded-lg">
              <p className="text-sm font-medium mb-2">Usuarios del sistema:</p>
              <div className="text-xs space-y-1 text-muted-foreground">
                <p>• admin@clinica.com (Admin) - Contraseña: <span className="font-medium">admin123</span></p>
                <p>• carlos@clinica.com (Veterinario) - Contraseña: <span className="font-medium">vet123</span></p>
                <p>• ana@clinica.com (Recepción) - Contraseña: <span className="font-medium">recep123</span></p>
                <p>• juan@clinica.com (Estudiante) - Contraseña: <span className="font-medium">est123</span></p>
              </div>
            </div>
            
            <div className="text-center space-y-2">
              <p className="text-sm text-muted-foreground">
                ¿Eres cliente?{' '}
                <Link to="/agendar-cita" className="text-primary hover:underline">
                  Regístrate al agendar una cita
                </Link>
              </p>
              <p className="text-sm text-muted-foreground">
                <Link to="/" className="text-primary hover:underline">
                  Volver al inicio
                </Link>
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
