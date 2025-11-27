import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@core/auth/AuthContext';
import { clienteService } from '../services/clienteService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Calendar, Dog, User, LogOut, Clock, CheckCircle2, XCircle, AlertCircle } from 'lucide-react';
import { Skeleton } from '@shared/components/ui/skeleton';
import { toast } from 'sonner';
import { useNavigate } from 'react-router-dom';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

export default function ClienteDashboard() {
  const { cliente, logout } = useAuth();
  const navigate = useNavigate();

  const { data: citas, isLoading: isLoadingCitas } = useQuery({
    queryKey: ['mis-citas'],
    queryFn: () => clienteService.getMisCitas(),
  });

  const { data: mascotas, isLoading: isLoadingMascotas } = useQuery({
    queryKey: ['mis-mascotas'],
    queryFn: () => clienteService.getMisMascotas(),
  });

  const handleLogout = () => {
    logout();
    toast.success('Sesión cerrada');
    navigate('/login');
  };

  const getEstadoBadge = (estado: string) => {
    const estados = {
      PENDIENTE: { icon: Clock, color: 'bg-yellow-100 text-yellow-800', label: 'Pendiente' },
      CONFIRMADA: { icon: CheckCircle2, color: 'bg-blue-100 text-blue-800', label: 'Confirmada' },
      ATENDIDA: { icon: CheckCircle2, color: 'bg-green-100 text-green-800', label: 'Atendida' },
      CANCELADA: { icon: XCircle, color: 'bg-red-100 text-red-800', label: 'Cancelada' },
    };
    return estados[estado as keyof typeof estados] || estados.PENDIENTE;
  };

  const citasProximas = citas?.filter(cita => {
    const fechaCita = new Date(cita.fecha);
    return fechaCita >= new Date() && cita.estado !== 'CANCELADA';
  }).sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime()).slice(0, 5) || [];

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-green-50">
      {/* Header */}
      <header className="sticky top-0 z-50 w-full border-b bg-white/80 backdrop-blur-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="h-12 w-12 rounded-full bg-primary/10 flex items-center justify-center">
                <Dog className="h-6 w-6 text-primary" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-foreground">Portal del Cliente</h1>
                <p className="text-xs text-muted-foreground">VetClinic Pro</p>
              </div>
            </div>
            <div className="flex items-center gap-4">
              <span className="text-sm text-muted-foreground">{cliente?.nombre}</span>
              <Button variant="outline" size="sm" onClick={handleLogout}>
                <LogOut className="h-4 w-4 mr-2" />
                Salir
              </Button>
            </div>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-8 max-w-7xl">
        {/* Bienvenida */}
        <div className="mb-8">
          <h2 className="text-3xl font-bold mb-2">
            ¡Hola, {cliente?.nombre}!
          </h2>
          <p className="text-muted-foreground">
            Aquí puedes ver tus citas agendadas y tus mascotas registradas
          </p>
        </div>

        <div className="grid lg:grid-cols-3 gap-6">
          {/* Columna principal */}
          <div className="lg:col-span-2 space-y-6">
            {/* Próximas Citas */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Calendar className="h-5 w-5" />
                  Próximas Citas
                </CardTitle>
                <CardDescription>
                  Tus citas programadas próximamente
                </CardDescription>
              </CardHeader>
              <CardContent>
                {isLoadingCitas ? (
                  <div className="space-y-4">
                    {[1, 2, 3].map((i) => (
                      <Skeleton key={i} className="h-24 w-full" />
                    ))}
                  </div>
                ) : citasProximas.length === 0 ? (
                  <div className="text-center py-8 text-muted-foreground">
                    <Calendar className="h-12 w-12 mx-auto mb-4 opacity-50" />
                    <p>No tienes citas programadas</p>
                    <Button
                      variant="outline"
                      className="mt-4"
                      onClick={() => navigate('/agendar-cita')}
                    >
                      Agendar una cita
                    </Button>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {citasProximas.map((cita) => {
                      const estadoBadge = getEstadoBadge(cita.estado);
                      const EstadoIcon = estadoBadge.icon;
                      const fechaCita = new Date(cita.fecha);
                      
                      return (
                        <div
                          key={cita.id}
                          className="border rounded-lg p-4 hover:bg-accent/50 transition-colors"
                        >
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <div className="flex items-center gap-2 mb-2">
                                <span className={`px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1 ${estadoBadge.color}`}>
                                  <EstadoIcon className="h-3 w-3" />
                                  {estadoBadge.label}
                                </span>
                              </div>
                              <h3 className="font-semibold mb-1">{cita.pacienteNombre || 'Mascota'}</h3>
                              <p className="text-sm text-muted-foreground mb-2">{cita.motivo}</p>
                              <div className="flex items-center gap-4 text-sm text-muted-foreground">
                                <span className="flex items-center gap-1">
                                  <Calendar className="h-4 w-4" />
                                  {format(fechaCita, "EEEE, d 'de' MMMM 'de' yyyy", { locale: es })}
                                </span>
                                <span className="flex items-center gap-1">
                                  <Clock className="h-4 w-4" />
                                  {format(fechaCita, 'HH:mm')}
                                </span>
                              </div>
                              {cita.profesionalNombre && (
                                <p className="text-sm text-muted-foreground mt-1">
                                  Veterinario: {cita.profesionalNombre}
                                </p>
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Todas las Citas */}
            {citas && citas.length > citasProximas.length && (
              <Card>
                <CardHeader>
                  <CardTitle>Historial de Citas</CardTitle>
                  <CardDescription>
                    Todas tus citas anteriores
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {citas
                      .filter(cita => {
                        const fechaCita = new Date(cita.fecha);
                        return fechaCita < new Date() || cita.estado === 'CANCELADA';
                      })
                      .sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime())
                      .slice(0, 5)
                      .map((cita) => {
                        const estadoBadge = getEstadoBadge(cita.estado);
                        const EstadoIcon = estadoBadge.icon;
                        const fechaCita = new Date(cita.fecha);
                        
                        return (
                          <div
                            key={cita.id}
                            className="border rounded-lg p-4 opacity-75"
                          >
                            <div className="flex items-start justify-between">
                              <div className="flex-1">
                                <div className="flex items-center gap-2 mb-2">
                                  <span className={`px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1 ${estadoBadge.color}`}>
                                    <EstadoIcon className="h-3 w-3" />
                                    {estadoBadge.label}
                                  </span>
                                </div>
                                <h3 className="font-semibold mb-1">{cita.pacienteNombre || 'Mascota'}</h3>
                                <p className="text-sm text-muted-foreground mb-2">{cita.motivo}</p>
                                <div className="flex items-center gap-4 text-sm text-muted-foreground">
                                  <span>
                                    {format(fechaCita, "d 'de' MMMM 'de' yyyy", { locale: es })}
                                  </span>
                                  <span>{format(fechaCita, 'HH:mm')}</span>
                                </div>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                  </div>
                </CardContent>
              </Card>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Mis Mascotas */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Dog className="h-5 w-5" />
                  Mis Mascotas
                </CardTitle>
                <CardDescription>
                  Tus mascotas registradas
                </CardDescription>
              </CardHeader>
              <CardContent>
                {isLoadingMascotas ? (
                  <div className="space-y-4">
                    {[1, 2].map((i) => (
                      <Skeleton key={i} className="h-20 w-full" />
                    ))}
                  </div>
                ) : mascotas && mascotas.length > 0 ? (
                  <div className="space-y-4">
                    {mascotas.map((mascota) => (
                      <div
                        key={mascota.id}
                        className="border rounded-lg p-4 hover:bg-accent/50 transition-colors"
                      >
                        <h3 className="font-semibold mb-1">{mascota.nombre}</h3>
                        <div className="text-sm text-muted-foreground space-y-1">
                          <p>{mascota.especie} {mascota.raza && `- ${mascota.raza}`}</p>
                          {mascota.edadMeses && (
                            <p>Edad: {Math.floor(mascota.edadMeses / 12)} años {mascota.edadMeses % 12} meses</p>
                          )}
                          {mascota.pesoKg && <p>Peso: {mascota.pesoKg} kg</p>}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-8 text-muted-foreground">
                    <Dog className="h-12 w-12 mx-auto mb-4 opacity-50" />
                    <p>No tienes mascotas registradas</p>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Mi Perfil */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <User className="h-5 w-5" />
                  Mi Perfil
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Nombre</p>
                    <p className="text-sm">{cliente?.nombre}</p>
                  </div>
                  <div>
                    <p className="text-sm font-medium text-muted-foreground">Email</p>
                    <p className="text-sm">{cliente?.email}</p>
                  </div>
                  {cliente?.telefono && (
                    <div>
                      <p className="text-sm font-medium text-muted-foreground">Teléfono</p>
                      <p className="text-sm">{cliente.telefono}</p>
                    </div>
                  )}
                  {cliente?.direccion && (
                    <div>
                      <p className="text-sm font-medium text-muted-foreground">Dirección</p>
                      <p className="text-sm">{cliente.direccion}</p>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>

            {/* Acciones rápidas */}
            <Card>
              <CardHeader>
                <CardTitle>Acciones Rápidas</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <Button
                    className="w-full"
                    onClick={() => navigate('/agendar-cita')}
                  >
                    Agendar Nueva Cita
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}

