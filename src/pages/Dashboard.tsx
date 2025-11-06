import { Calendar, Dog, Users, Activity, Clock, CheckCircle2, TrendingUp } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { useAuth } from '@/contexts/AuthContext';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';

const stats = [
  { title: 'Citas Hoy', value: '12', icon: Calendar, color: 'text-primary', link: '/agenda' },
  { title: 'Pacientes Activos', value: '248', icon: Dog, color: 'text-secondary', link: '/pacientes' },
  { title: 'Consultas Pendientes', value: '5', icon: Activity, color: 'text-warning', link: '/historias' },
  { title: 'Propietarios', value: '156', icon: Users, color: 'text-info', link: '/propietarios' },
];

const upcomingAppointments = [
  { id: '1', time: '09:00', patient: 'Max', owner: 'Juan Pérez', status: 'Confirmada' },
  { id: '2', time: '10:30', patient: 'Luna', owner: 'María García', status: 'Pendiente' },
  { id: '3', time: '11:00', patient: 'Rocky', owner: 'Carlos López', status: 'Confirmada' },
  { id: '4', time: '14:00', patient: 'Mia', owner: 'Ana Martínez', status: 'Confirmada' },
];

const statusColors = {
  Confirmada: 'bg-status-confirmed/10 text-status-confirmed',
  Pendiente: 'bg-status-pending/10 text-status-pending',
  Cancelada: 'bg-status-cancelled/10 text-status-cancelled',
  Atendida: 'bg-status-completed/10 text-status-completed',
};

// Datos para gráficos
const consultasPorDia = [
  { dia: 'Lun', consultas: 15 },
  { dia: 'Mar', consultas: 22 },
  { dia: 'Mié', consultas: 18 },
  { dia: 'Jue', consultas: 25 },
  { dia: 'Vie', consultas: 20 },
  { dia: 'Sáb', consultas: 12 },
  { dia: 'Dom', consultas: 8 },
];

const especiesDistribucion = [
  { nombre: 'Caninos', valor: 148, color: 'hsl(var(--primary))' },
  { nombre: 'Felinos', valor: 85, color: 'hsl(var(--secondary))' },
  { nombre: 'Otros', valor: 15, color: 'hsl(var(--info))' },
];

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-foreground">Bienvenido, {user?.nombre}</h1>
        <p className="text-muted-foreground mt-1">Resumen de actividades del día</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {stats.map((stat) => (
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
                    {appointment.status}
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
              <BarChart data={consultasPorDia}>
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
                  data={especiesDistribucion}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ nombre, valor }) => `${nombre}: ${valor}`}
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="valor"
                >
                  {especiesDistribucion.map((entry, index) => (
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
