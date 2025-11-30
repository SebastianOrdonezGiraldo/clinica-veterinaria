import { useState } from 'react';
import { Calendar, Dog, Users, Activity, Clock, CheckCircle2, TrendingUp, Syringe, Package, Pill, AlertCircle, Filter } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Skeleton } from '@shared/components/ui/skeleton';
import { Button } from '@shared/components/ui/button';
import { Input } from '@shared/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { useAuth } from '@core/auth/AuthContext';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend, LineChart, Line } from 'recharts';
import { useDashboard } from '../hooks/useDashboard';
import { format, subDays, startOfMonth, endOfMonth } from 'date-fns';
import { es } from 'date-fns/locale';

const statusColors = {
  CONFIRMADA: 'bg-status-confirmed/10 text-status-confirmed',
  PENDIENTE: 'bg-status-pending/10 text-status-pending',
  CANCELADA: 'bg-status-cancelled/10 text-status-cancelled',
  ATENDIDA: 'bg-status-completed/10 text-status-completed',
};

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [filtroFecha, setFiltroFecha] = useState<string>('hoy');
  const [fechaInicio, setFechaInicio] = useState<string>('');
  const [fechaFin, setFechaFin] = useState<string>('');

  // Calcular filtros según selección
  const getFilters = () => {
    const hoy = new Date();
    let inicio: string | undefined;
    let fin: string | undefined;

    switch (filtroFecha) {
      case 'hoy':
        const hoyStr = format(hoy, 'yyyy-MM-dd');
        inicio = hoyStr;
        fin = hoyStr;
        break;
      case 'semana':
        inicio = format(subDays(hoy, 7), 'yyyy-MM-dd');
        fin = format(hoy, 'yyyy-MM-dd');
        break;
      case 'mes':
        inicio = format(startOfMonth(hoy), 'yyyy-MM-dd');
        fin = format(endOfMonth(hoy), 'yyyy-MM-dd');
        break;
      case 'personalizado':
        inicio = fechaInicio || undefined;
        fin = fechaFin || undefined;
        break;
      default:
        break;
    }

    return { fechaInicio: inicio, fechaFin: fin };
  };

  const { stats, isLoading } = useDashboard(getFilters());

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div>
          <Skeleton className="h-9 w-64 mb-2" />
          <Skeleton className="h-5 w-96" />
        </div>
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => (
            <Card key={i}>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <Skeleton className="h-4 w-24" />
                <Skeleton className="h-4 w-4 rounded" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-16" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  if (!stats) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-muted-foreground">No se pudieron cargar las estadísticas</p>
      </div>
    );
  }

  const statsCards = [
    { title: 'Citas Hoy', value: stats.citasHoy.toString(), icon: Calendar, color: 'text-primary', link: '/agenda' },
    { title: 'Pacientes Activos', value: stats.pacientesActivos.toString(), icon: Dog, color: 'text-secondary', link: '/pacientes' },
    { title: 'Consultas Pendientes', value: stats.consultasPendientes.toString(), icon: Activity, color: 'text-warning', link: '/historias' },
    { title: 'Propietarios', value: stats.totalPropietarios.toString(), icon: Users, color: 'text-info', link: '/propietarios' },
  ];

  // Tarjetas adicionales si están disponibles
  const additionalCards = [];
  if (stats.vacunacionesProximas !== undefined) {
    additionalCards.push({ 
      title: 'Vacunaciones Próximas', 
      value: stats.vacunacionesProximas.toString(), 
      icon: Syringe, 
      color: 'text-primary', 
      link: '/vacunaciones?tab=proximas',
      badge: stats.vacunacionesVencidas && stats.vacunacionesVencidas > 0 
        ? { text: `${stats.vacunacionesVencidas} vencidas`, variant: 'destructive' as const }
        : undefined
    });
  }
  if (stats.productosStockBajo !== undefined) {
    additionalCards.push({ 
      title: 'Productos Stock Bajo', 
      value: stats.productosStockBajo.toString(), 
      icon: Package, 
      color: 'text-destructive', 
      link: '/inventario/productos',
      badge: stats.productosStockBajo > 0 
        ? { text: 'Revisar', variant: 'destructive' as const }
        : undefined
    });
  }
  if (stats.prescripcionesMes !== undefined) {
    additionalCards.push({ 
      title: 'Prescripciones Mes', 
      value: stats.prescripcionesMes.toString(), 
      icon: Pill, 
      color: 'text-info', 
      link: '/prescripciones' 
    });
  }

  // Mapear próximas citas al formato esperado
  const upcomingAppointments = stats.proximasCitas.map(cita => ({
    id: cita.id,
    time: cita.hora,
    patient: cita.pacienteNombre,
    owner: cita.propietarioNombre,
    status: cita.estado,
  }));

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Bienvenido, {user?.nombre}</h1>
          <p className="text-muted-foreground mt-1">Resumen de actividades</p>
        </div>
        <div className="flex gap-2 items-center">
          <Select value={filtroFecha} onValueChange={setFiltroFecha}>
            <SelectTrigger className="w-[180px]">
              <Filter className="h-4 w-4 mr-2" />
              <SelectValue placeholder="Filtrar por fecha" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="hoy">Hoy</SelectItem>
              <SelectItem value="semana">Última semana</SelectItem>
              <SelectItem value="mes">Este mes</SelectItem>
              <SelectItem value="personalizado">Personalizado</SelectItem>
            </SelectContent>
          </Select>
          {filtroFecha === 'personalizado' && (
            <div className="flex gap-2">
              <Input
                type="date"
                value={fechaInicio}
                onChange={(e) => setFechaInicio(e.target.value)}
                placeholder="Desde"
                className="w-[140px]"
              />
              <Input
                type="date"
                value={fechaFin}
                onChange={(e) => setFechaFin(e.target.value)}
                placeholder="Hasta"
                className="w-[140px]"
              />
            </div>
          )}
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {statsCards.map((stat) => (
          <Card 
            key={stat.title}
            className="cursor-pointer hover:shadow-lg transition-shadow"
            onClick={() => navigate(stat.link)}
          >
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.title}
              </CardTitle>
              <stat.icon className={`h-4 w-4 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
            </CardContent>
          </Card>
        ))}
        {additionalCards.map((stat) => (
          <Card 
            key={stat.title}
            className="cursor-pointer hover:shadow-lg transition-shadow"
            onClick={() => navigate(stat.link)}
          >
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.title}
              </CardTitle>
              <stat.icon className={`h-4 w-4 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="flex items-center justify-between">
                <div className="text-2xl font-bold">{stat.value}</div>
                {stat.badge && (
                  <Badge variant={stat.badge.variant} className="text-xs">
                    {stat.badge.text}
                  </Badge>
                )}
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5 text-primary" />
              Próximas Citas
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {upcomingAppointments.map((appointment) => (
                <div
                  key={appointment.id}
                  className="flex items-center justify-between p-3 rounded-lg border border-border hover:bg-accent/50 transition-colors cursor-pointer"
                  onClick={() => navigate('/agenda')}
                >
                  <div className="flex items-center gap-3">
                    <div className="flex flex-col items-center justify-center w-16 h-16 rounded-lg bg-primary/10">
                      <span className="text-xs text-muted-foreground">Hora</span>
                      <span className="text-sm font-bold text-primary">{appointment.time}</span>
                    </div>
                    <div>
                      <p className="font-medium">{appointment.patient}</p>
                      <p className="text-sm text-muted-foreground">{appointment.owner}</p>
                    </div>
                  </div>
                  <Badge className={statusColors[appointment.status as keyof typeof statusColors]}>
                    {appointment.status.replace(/_/g, ' ')}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Activity className="h-5 w-5 text-secondary" />
              Actividad Reciente
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {stats.actividadReciente && stats.actividadReciente.length > 0 ? (
                stats.actividadReciente.map((actividad, index) => {
                  const getIcon = () => {
                    switch (actividad.tipo) {
                      case 'CONSULTA':
                        return <CheckCircle2 className="h-4 w-4 text-secondary" />;
                      case 'CITA':
                        return <Calendar className="h-4 w-4 text-primary" />;
                      case 'PACIENTE':
                        return <Dog className="h-4 w-4 text-info" />;
                      case 'PRESCRIPCION':
                        return <Pill className="h-4 w-4 text-primary" />;
                      case 'VACUNACION':
                        return <Syringe className="h-4 w-4 text-primary" />;
                      default:
                        return <Activity className="h-4 w-4 text-muted-foreground" />;
                    }
                  };

                  const getBgColor = () => {
                    switch (actividad.tipo) {
                      case 'CONSULTA':
                        return 'bg-secondary/10';
                      case 'CITA':
                        return 'bg-primary/10';
                      case 'PACIENTE':
                        return 'bg-info/10';
                      case 'PRESCRIPCION':
                        return 'bg-primary/10';
                      case 'VACUNACION':
                        return 'bg-primary/10';
                      default:
                        return 'bg-muted';
                    }
                  };

                  return (
                    <div
                      key={index}
                      className="flex gap-3 cursor-pointer hover:bg-accent/50 p-2 rounded-lg transition-colors"
                      onClick={() => actividad.link && navigate(actividad.link)}
                    >
                      <div className={`h-8 w-8 rounded-full ${getBgColor()} flex items-center justify-center flex-shrink-0`}>
                        {getIcon()}
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">{actividad.descripcion}</p>
                        <p className="text-xs text-muted-foreground">{actividad.fecha}</p>
                      </div>
                    </div>
                  );
                })
              ) : (
                <p className="text-sm text-muted-foreground text-center py-4">
                  No hay actividad reciente
                </p>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Gráficos */}
      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="h-5 w-5 text-primary" />
              Consultas por Día
            </CardTitle>
            <CardDescription>
              {filtroFecha === 'hoy' ? 'Últimos 7 días' : 
               filtroFecha === 'semana' ? 'Última semana' :
               filtroFecha === 'mes' ? 'Este mes' : 'Período seleccionado'}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={stats.consultasPorDia}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                <XAxis dataKey="dia" className="text-xs" />
                <YAxis className="text-xs" />
                <Tooltip 
                  contentStyle={{ 
                    backgroundColor: 'hsl(var(--background))', 
                    border: '1px solid hsl(var(--border))',
                    borderRadius: '0.5rem'
                  }}
                />
                <Bar dataKey="consultas" fill="hsl(var(--primary))" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Dog className="h-5 w-5 text-secondary" />
              Distribución por Especie
            </CardTitle>
            <CardDescription>Pacientes activos</CardDescription>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={stats.distribucionEspecies}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ nombre, valor }) => `${nombre}: ${valor}`}
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="valor"
                >
                  {stats.distribucionEspecies.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip 
                  contentStyle={{ 
                    backgroundColor: 'hsl(var(--background))', 
                    border: '1px solid hsl(var(--border))',
                    borderRadius: '0.5rem'
                  }}
                />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      {/* Gráficos adicionales */}
      {stats.citasPorEstado && stats.citasPorEstado.length > 0 && (
        <div className="grid gap-6 md:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Calendar className="h-5 w-5 text-primary" />
                Citas por Estado
              </CardTitle>
              <CardDescription>Distribución de citas</CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={stats.citasPorEstado} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                  <XAxis type="number" className="text-xs" />
                  <YAxis dataKey="estado" type="category" className="text-xs" width={80} />
                  <Tooltip 
                    contentStyle={{ 
                      backgroundColor: 'hsl(var(--background))', 
                      border: '1px solid hsl(var(--border))',
                      borderRadius: '0.5rem'
                    }}
                  />
                  <Bar dataKey="cantidad" fill="hsl(var(--primary))" radius={[0, 8, 8, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>

          {stats.tendenciasConsultas && stats.tendenciasConsultas.length > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5 text-primary" />
                  Tendencias de Consultas
                </CardTitle>
                <CardDescription>Últimos 30 días</CardDescription>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={stats.tendenciasConsultas}>
                    <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                    <XAxis dataKey="fecha" className="text-xs" angle={-45} textAnchor="end" height={80} />
                    <YAxis className="text-xs" />
                    <Tooltip 
                      contentStyle={{ 
                        backgroundColor: 'hsl(var(--background))', 
                        border: '1px solid hsl(var(--border))',
                        borderRadius: '0.5rem'
                      }}
                    />
                    <Line 
                      type="monotone" 
                      dataKey="consultas" 
                      stroke="hsl(var(--primary))" 
                      strokeWidth={2}
                      dot={{ fill: 'hsl(var(--primary))', r: 4 }}
                      activeDot={{ r: 6 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          )}
        </div>
      )}

      {/* Alertas y recordatorios */}
      {(stats.vacunacionesVencidas && stats.vacunacionesVencidas > 0) || 
       (stats.productosStockBajo && stats.productosStockBajo > 0) ? (
        <Card className="border-destructive/50">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-destructive">
              <AlertCircle className="h-5 w-5" />
              Alertas y Recordatorios
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {stats.vacunacionesVencidas && stats.vacunacionesVencidas > 0 && (
                <div 
                  className="flex items-center justify-between p-3 rounded-lg bg-destructive/10 border border-destructive/20 cursor-pointer hover:bg-destructive/20 transition-colors"
                  onClick={() => navigate('/vacunaciones?tab=vencidas')}
                >
                  <div className="flex items-center gap-3">
                    <Syringe className="h-5 w-5 text-destructive" />
                    <div>
                      <p className="font-medium text-destructive">
                        {stats.vacunacionesVencidas} vacunación{stats.vacunacionesVencidas > 1 ? 'es' : ''} vencida{stats.vacunacionesVencidas > 1 ? 's' : ''}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        Requieren atención inmediata
                      </p>
                    </div>
                  </div>
                  <Button variant="outline" size="sm">
                    Ver
                  </Button>
                </div>
              )}
              {stats.productosStockBajo && stats.productosStockBajo > 0 && (
                <div 
                  className="flex items-center justify-between p-3 rounded-lg bg-orange-500/10 border border-orange-500/20 cursor-pointer hover:bg-orange-500/20 transition-colors"
                  onClick={() => navigate('/inventario/productos')}
                >
                  <div className="flex items-center gap-3">
                    <Package className="h-5 w-5 text-orange-600" />
                    <div>
                      <p className="font-medium text-orange-600">
                        {stats.productosStockBajo} producto{stats.productosStockBajo > 1 ? 's' : ''} con stock bajo
                      </p>
                      <p className="text-sm text-muted-foreground">
                        Revisar inventario
                      </p>
                    </div>
                  </div>
                  <Button variant="outline" size="sm">
                    Ver
                  </Button>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}
