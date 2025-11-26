import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '@core/auth/AuthContext';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Label } from '@shared/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@shared/components/ui/card';
import { toast } from 'sonner';
import { Dog } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await login(email, password);
      toast.success('Sesión iniciada correctamente');
      navigate('/');
    } catch (error) {
      toast.error('Credenciales inválidas. Revisa los usuarios de prueba.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="text-center">
          <div className="mx-auto h-16 w-16 rounded-full bg-primary/10 flex items-center justify-center mb-4">
            <Dog className="h-8 w-8 text-primary" />
          </div>
          <CardTitle className="text-2xl">VetClinic Pro</CardTitle>
          <CardDescription>Sistema de Gestión Clínica Veterinaria</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Correo Electrónico</Label>
              <Input
                id="email"
                type="email"
                placeholder="usuario@vetclinic.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Contraseña</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
            </Button>
          </form>

          <div className="mt-6 p-4 bg-muted rounded-lg">
            <p className="text-sm font-medium mb-2">Usuarios de prueba:</p>
            <div className="text-xs space-y-1 text-muted-foreground">
              <p>• admin@clinica.com (Admin) - Contraseña: <span className="font-medium">admin123</span></p>
              <p>• carlos@clinica.com (Veterinario) - Contraseña: <span className="font-medium">vet123</span></p>
              <p>• ana@clinica.com (Recepción) - Contraseña: <span className="font-medium">recep123</span></p>
              <p>• juan@clinica.com (Estudiante) - Contraseña: <span className="font-medium">est123</span></p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
