import { useQuery } from '@tanstack/react-query';
import { useAuth } from '@core/auth/AuthContext';
import { clienteService } from '../services/clienteService';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Badge } from '@shared/components/ui/badge';
import { Calendar, Dog, User, LogOut, Clock, CheckCircle2, XCircle, AlertCircle, Phone, MapPin, Mail, Plus, CalendarDays, TrendingUp, Heart } from 'lucide-react';
import { Skeleton } from '@shared/components/ui/skeleton';
import { toast } from 'sonner';
import { useNavigate } from 'react-router-dom';
import { format, formatDistanceToNow, isToday, isTomorrow, differenceInDays } from 'date-fns';
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

  const citasHistorial = citas?.filter(cita => {
    const fechaCita = new Date(cita.fecha);
    return fechaCita < new Date() || cita.estado === 'CANCELADA';
  }).sort((a, b) => new Date(b.fecha).getTime() - new Date(a.fecha).getTime()) || [];

  const proximaCita = citasProximas[0];
  const fechaProximaCita = proximaCita ? new Date(proximaCita.fecha) : null;

  // Calcular estadísticas
  const totalCitas = citas?.length || 0;
  const citasPendientes = citas?.filter(c => c.estado === 'PENDIENTE' || c.estado === 'CONFIRMADA').length || 0;
  const citasAtendidas = citas?.filter(c => c.estado === 'ATENDIDA').length || 0;
  const totalMascotas = mascotas?.length || 0;

  // Formatear tiempo hasta próxima cita
  const getTiempoHastaCita = (fecha: Date): string => {
    if (isToday(fecha)) {
      return 'Hoy';
    } else if (isTomorrow(fecha)) {
      return 'Mañana';
    } else {
      const dias = differenceInDays(fecha, new Date());
      return `En ${dias} día${dias !== 1 ? 's' : ''}`;
    }
  };

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
            ¡Hola, {cliente?.nombre?.split(' ')[0]}! 👋
          </h2>
          <p className="text-muted-foreground">
            Aquí puedes ver tus citas agendadas y tus mascotas registradas
          </p>
        </div>

        {/* Tarjetas de Estadísticas */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Total Citas</p>
                  <p className="text-2xl font-bold">{totalCitas}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center">
                  <Calendar className="h-6 w-6 text-blue-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Citas Pendientes</p>
                  <p className="text-2xl font-bold">{citasPendientes}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-yellow-100 flex items-center justify-center">
                  <Clock className="h-6 w-6 text-yellow-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Citas Atendidas</p>
                  <p className="text-2xl font-bold">{citasAtendidas}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center">
                  <CheckCircle2 className="h-6 w-6 text-green-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Mis Mascotas</p>
                  <p className="text-2xl font-bold">{totalMascotas}</p>
                </div>
                <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center">
                  <Dog className="h-6 w-6 text-purple-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Recordatorio de Próxima Cita */}
        {proximaCita && fechaProximaCita && (
          <Card className="mb-6 border-primary/20 bg-gradient-to-r from-primary/5 to-blue-50">
            <CardContent className="p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <AlertCircle className="h-5 w-5 text-primary" />
                    <h3 className="font-semibold text-lg">Próxima Cita</h3>
                    <Badge variant="outline" className="ml-2">
                      {getTiempoHastaCita(fechaProximaCita)}
                    </Badge>
                  </div>
                  <div className="space-y-2">
                    <p className="font-medium text-lg">{proximaCita.pacienteNombre || 'Mascota'}</p>
                    <p className="text-sm text-muted-foreground">{proximaCita.motivo}</p>
                    <div className="flex items-center gap-4 text-sm">
                      <span className="flex items-center gap-1">
                        <Calendar className="h-4 w-4" />
                        {format(fechaProximaCita, "EEEE, d 'de' MMMM 'de' yyyy", { locale: es })}
                      </span>
                      <span className="flex items-center gap-1">
                        <Clock className="h-4 w-4" />
                        {format(fechaProximaCita, 'HH:mm')}
                      </span>
                      {proximaCita.profesionalNombre && (
                        <span className="flex items-center gap-1">
                          <User className="h-4 w-4" />
                          {proximaCita.profesionalNombre}
                        </span>
                      )}
                    </div>
                  </div>
                </div>
                <div className="flex flex-col gap-2">
                  <Badge className={getEstadoBadge(proximaCita.estado).color}>
                    {getEstadoBadge(proximaCita.estado).label}
                  </Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        )}

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
                      onClick={() => navigate('/cliente/agendar-cita')}
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

            {/* Historial de Citas */}
            {citasHistorial && citasHistorial.length > 0 && (
              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div>
                      <CardTitle>Historial de Citas</CardTitle>
                      <CardDescription>
                        Tus citas anteriores y canceladas
                      </CardDescription>
                    </div>
                    {citasHistorial.length > 5 && (
                      <Badge variant="outline">
                        {citasHistorial.length} total
                      </Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {citasHistorial.slice(0, 5).map((cita) => {
                      const estadoBadge = getEstadoBadge(cita.estado);
                      const EstadoIcon = estadoBadge.icon;
                      const fechaCita = new Date(cita.fecha);
                      
                      return (
                        <div
                          key={cita.id}
                          className="border rounded-lg p-4 hover:bg-accent/30 transition-colors"
                        >
                          <div className="flex items-start justify-between">
                            <div className="flex-1">
                              <div className="flex items-center gap-2 mb-2">
                                <span className={`px-2 py-1 rounded-full text-xs font-medium flex items-center gap-1 ${estadoBadge.color}`}>
                                  <EstadoIcon className="h-3 w-3" />
                                  {estadoBadge.label}
                                </span>
                                <span className="text-xs text-muted-foreground">
                                  {formatDistanceToNow(fechaCita, { addSuffix: true, locale: es })}
                                </span>
                              </div>
                              <h3 className="font-semibold mb-1">{cita.pacienteNombre || 'Mascota'}</h3>
                              <p className="text-sm text-muted-foreground mb-2">{cita.motivo}</p>
                              <div className="flex items-center gap-4 text-sm text-muted-foreground">
                                <span>
                                  {format(fechaCita, "d 'de' MMMM 'de' yyyy", { locale: es })}
                                </span>
                                <span>{format(fechaCita, 'HH:mm')}</span>
                                {cita.profesionalNombre && (
                                  <span>• {cita.profesionalNombre}</span>
                                )}
                              </div>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                  {citasHistorial.length > 5 && (
                    <div className="mt-4 text-center">
                      <Button variant="outline" size="sm">
                        Ver todas las citas ({citasHistorial.length})
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Mis Mascotas */}
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle className="flex items-center gap-2">
                      <Dog className="h-5 w-5" />
                      Mis Mascotas
                    </CardTitle>
                    <CardDescription>
                      {totalMascotas > 0 ? `${totalMascotas} mascota${totalMascotas !== 1 ? 's' : ''} registrada${totalMascotas !== 1 ? 's' : ''}` : 'Tus mascotas registradas'}
                    </CardDescription>
                  </div>
                  {totalMascotas > 0 && (
                    <Badge variant="secondary">{totalMascotas}</Badge>
                  )}
                </div>
              </CardHeader>
              <CardContent>
                {isLoadingMascotas ? (
                  <div className="space-y-4">
                    {[1, 2].map((i) => (
                      <Skeleton key={i} className="h-24 w-full" />
                    ))}
                  </div>
                ) : mascotas && mascotas.length > 0 ? (
                  <div className="space-y-3">
                    {mascotas.map((mascota) => {
                      const citasMascota = citas?.filter(c => c.pacienteId === mascota.id) || [];
                      const proximaCitaMascota = citasMascota
                        .filter(c => {
                          const fecha = new Date(c.fecha);
                          return fecha >= new Date() && c.estado !== 'CANCELADA';
                        })
                        .sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime())[0];
                      
                      return (
                        <div
                          key={mascota.id}
                          className="border rounded-lg p-4 hover:bg-accent/50 transition-colors cursor-pointer"
                        >
                          <div className="flex items-start justify-between mb-2">
                            <div className="flex-1">
                              <h3 className="font-semibold mb-1 flex items-center gap-2">
                                <Heart className="h-4 w-4 text-red-500" />
                                {mascota.nombre}
                              </h3>
                              <div className="text-sm text-muted-foreground space-y-1">
                                <p className="font-medium">{mascota.especie} {mascota.raza && `- ${mascota.raza}`}</p>
                                {mascota.edadMeses && (
                                  <p>Edad: {Math.floor(mascota.edadMeses / 12)} años {mascota.edadMeses % 12} meses</p>
                                )}
                                {mascota.pesoKg && <p>Peso: {mascota.pesoKg} kg</p>}
                                {mascota.sexo && <p>Sexo: {mascota.sexo}</p>}
                              </div>
                            </div>
                            {proximaCitaMascota && (
                              <Badge variant="outline" className="text-xs">
                                Próxima: {format(new Date(proximaCitaMascota.fecha), 'd MMM', { locale: es })}
                              </Badge>
                            )}
                          </div>
                          {citasMascota.length > 0 && (
                            <div className="mt-2 pt-2 border-t text-xs text-muted-foreground">
                              {citasMascota.length} cita{citasMascota.length !== 1 ? 's' : ''} registrada{citasMascota.length !== 1 ? 's' : ''}
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                ) : (
                  <div className="text-center py-8 text-muted-foreground">
                    <Dog className="h-12 w-12 mx-auto mb-4 opacity-50" />
                    <p className="mb-2">No tienes mascotas registradas</p>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => navigate('/agendar-cita')}
                    >
                      <Plus className="h-4 w-4 mr-2" />
                      Registrar mascota (formulario público)
                    </Button>
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
                    onClick={() => navigate('/cliente/agendar-cita')}
                  >
                    <Calendar className="h-4 w-4 mr-2" />
                    Agendar Nueva Cita
                  </Button>
                  <Button
                    variant="outline"
                    className="w-full"
                    onClick={() => navigate('/')}
                  >
                    <MapPin className="h-4 w-4 mr-2" />
                    Ver Información de la Clínica
                  </Button>
                </div>
              </CardContent>
            </Card>

            {/* Información de Contacto */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Phone className="h-5 w-5" />
                  Contacto
                </CardTitle>
                <CardDescription>
                  Información de contacto de la clínica
                </CardDescription>
              </CardHeader>
              <CardContent>
                <div className="space-y-3 text-sm">
                  <div className="flex items-center gap-2">
                    <Phone className="h-4 w-4 text-muted-foreground" />
                    <span>+573186160630</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Mail className="h-4 w-4 text-muted-foreground" />
                    <span>contacto@vetclinic.com</span>
                  </div>
                  <div className="flex items-start gap-2">
                    <MapPin className="h-4 w-4 text-muted-foreground mt-0.5" />
                    <span>Calle 6 Norte # 14-26</span>
                  </div>
                  <div className="pt-2 border-t">
                    <p className="text-xs text-muted-foreground">
                      Horarios: Lunes-Viernes 8am-12m y 2pm-6pm<br />
                      Sábados 8am-12m | Domingos cerrado
                    </p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}

