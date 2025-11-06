import { useState, useEffect } from 'react';
import { Calendar, Dog, Users, Activity, Clock, CheckCircle2, TrendingUp } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { useAuth } from '@/contexts/AuthContext';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import { citaService } from '@/services/citaService';
import { pacienteService } from '@/services/pacienteService';
import { propietarioService } from '@/services/propietarioService';
import { consultaService } from '@/services/consultaService';
import { Cita, Paciente, Propietario } from '@/types';

const statusColors = {
  CONFIRMADA: 'bg-status-confirmed/10 text-status-confirmed',
  PROGRAMADA: 'bg-status-pending/10 text-status-pending',
  CANCELADA: 'bg-status-cancelled/10 text-status-cancelled',
  COMPLETADA: 'bg-status-completed/10 text-status-completed',
  EN_CURSO: 'bg-status-confirmed/10 text-status-confirmed',
  NO_ASISTIO: 'bg-status-cancelled/10 text-status-cancelled',
};

// Datos para gráficos (estos serían calculados con datos reales)
const consultasPorDia = [
  { dia: 'Lun', consultas: 15 },
  { dia: 'Mar', consultas: 22 },
  { dia: 'Mié', consultas: 18 },
  { dia: 'Jue', consultas: 25 },
  { dia: 'Vie', consultas: 20 },
  { dia: 'Sáb', consultas: 12 },
  { dia: 'Dom', consultas: 8 },
];

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [citas, setCitas] = useState<Cita[]>([]);
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [propietarios, setPropietarios] = useState<Propietario[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [citasData, pacientesData, propietariosData] = await Promise.all([
        citaService.getAll(),
        pacienteService.getAll(),
        propietarioService.getAll(),
      ]);
      setCitas(citasData);
      setPacientes(pacientesData);
      setPropietarios(propietariosData);
    } catch (error) {
      console.error('Error al cargar datos:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // Calcular estadísticas
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const citasHoy = citas.filter(cita => {
    const citaDate = new Date(cita.fecha);
    citaDate.setHours(0, 0, 0, 0);
    return citaDate.getTime() === today.getTime();
  });

  const consultasPendientes = citas.filter(c => c.estado === 'PROGRAMADA' || c.estado === 'CONFIRMADA');

  const stats = [
    { title: 'Citas Hoy', value: citasHoy.length.toString(), icon: Calendar, color: 'text-primary', link: '/agenda' },
    { title: 'Pacientes Activos', value: pacientes.length.toString(), icon: Dog, color: 'text-secondary', link: '/pacientes' },
    { title: 'Consultas Pendientes', value: consultasPendientes.length.toString(), icon: Activity, color: 'text-warning', link: '/historias' },
    { title: 'Propietarios', value: propietarios.length.toString(), icon: Users, color: 'text-info', link: '/propietarios' },
  ];

  // Próximas citas de hoy ordenadas por hora
  const upcomingAppointments = citasHoy
    .sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime())
    .slice(0, 4)
    .map(cita => {
      const paciente = pacientes.find(p => p.id === cita.pacienteId);
      const propietario = propietarios.find(p => p.id === paciente?.propietarioId);
      return {
        id: cita.id,
        time: new Date(cita.fecha).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' }),
        patient: paciente?.nombre || 'N/A',
        owner: propietario?.nombre || 'N/A',
        status: cita.estado,
      };
    });

  // Distribución por especies
  const especiesDistribucion = [
    { 
      nombre: 'Caninos', 
      valor: pacientes.filter(p => p.especie.toLowerCase().includes('canino')).length, 
      color: 'hsl(var(--primary))' 
    },
    { 
      nombre: 'Felinos', 
      valor: pacientes.filter(p => p.especie.toLowerCase().includes('felino')).length, 
      color: 'hsl(var(--secondary))' 
    },
    { 
      nombre: 'Otros', 
      valor: pacientes.filter(p => !p.especie.toLowerCase().includes('canino') && !p.especie.toLowerCase().includes('felino')).length, 
      color: 'hsl(var(--info))' 
    },
  ];

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
