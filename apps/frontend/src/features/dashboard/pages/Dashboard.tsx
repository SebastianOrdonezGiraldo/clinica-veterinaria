import { useState, useEffect } from 'react';
import { Calendar, Dog, Users, Activity, Clock, CheckCircle2, TrendingUp } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { useAuth } from '@core/auth/AuthContext';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import { dashboardService, DashboardStats } from '@features/dashboard/services/dashboardService';

const statusColors = {
  CONFIRMADA: 'bg-status-confirmed/10 text-status-confirmed',
  PENDIENTE: 'bg-status-pending/10 text-status-pending',
  CANCELADA: 'bg-status-cancelled/10 text-status-cancelled',
  ATENDIDA: 'bg-status-completed/10 text-status-completed',
};

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const data = await dashboardService.getStats();
      setStats(data);
    } catch (error) {
      console.error('Error al cargar estadísticas:', error);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading || !stats) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-muted-foreground">Cargando estadísticas...</p>
      </div>
    );
  }

  const statsCards = [
    { title: 'Citas Hoy', value: stats.citasHoy.toString(), icon: Calendar, color: 'text-primary', link: '/agenda' },
    { title: 'Pacientes Activos', value: stats.pacientesActivos.toString(), icon: Dog, color: 'text-secondary', link: '/pacientes' },
    { title: 'Consultas Pendientes', value: stats.consultasPendientes.toString(), icon: Activity, color: 'text-warning', link: '/historias' },
    { title: 'Propietarios', value: stats.totalPropietarios.toString(), icon: Users, color: 'text-info', link: '/propietarios' },
  ];

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
      <div>
        <h1 className="text-3xl font-bold text-foreground">Bienvenido, {user?.nombre}</h1>
        <p className="text-muted-foreground mt-1">Resumen de actividades del día</p>
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
              <div 
                className="flex gap-3 cursor-pointer hover:bg-accent/50 p-2 rounded-lg transition-colors"
                onClick={() => navigate('/historias')}
              >
                <div className="h-8 w-8 rounded-full bg-secondary/10 flex items-center justify-center flex-shrink-0">
                  <CheckCircle2 className="h-4 w-4 text-secondary" />
                </div>
                <div>
                  <p className="text-sm font-medium">Consulta completada</p>
                  <p className="text-xs text-muted-foreground">Max - Hace 15 minutos</p>
                </div>
              </div>
              <div 
                className="flex gap-3 cursor-pointer hover:bg-accent/50 p-2 rounded-lg transition-colors"
                onClick={() => navigate('/agenda')}
              >
                <div className="h-8 w-8 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
                  <Calendar className="h-4 w-4 text-primary" />
                </div>
                <div>
                  <p className="text-sm font-medium">Nueva cita agendada</p>
                  <p className="text-xs text-muted-foreground">Luna - Hace 1 hora</p>
                </div>
              </div>
              <div 
                className="flex gap-3 cursor-pointer hover:bg-accent/50 p-2 rounded-lg transition-colors"
                onClick={() => navigate('/pacientes')}
              >
                <div className="h-8 w-8 rounded-full bg-info/10 flex items-center justify-center flex-shrink-0">
                  <Dog className="h-4 w-4 text-info" />
                </div>
                <div>
                  <p className="text-sm font-medium">Nuevo paciente registrado</p>
                  <p className="text-xs text-muted-foreground">Toby - Hace 2 horas</p>
                </div>
              </div>
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
            <CardDescription>Últimos 7 días</CardDescription>
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
    </div>
  );
}
