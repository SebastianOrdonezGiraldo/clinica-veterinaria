import { useState } from 'react';
import { BarChart3, Download, Calendar, TrendingUp, Users, Dog } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Badge } from '@shared/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { mockCitas, mockPacientes, mockConsultas, mockUsuarios } from '@shared/utils/mockData';
import { toast } from 'sonner';
import { Bar, BarChart, Line, LineChart, Pie, PieChart, Cell, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const COLORS = ['hsl(var(--primary))', 'hsl(var(--secondary))', 'hsl(var(--info))', 'hsl(var(--warning))'];

export default function Reportes() {
  const [periodo, setPeriodo] = useState('mes');

  // Estadísticas
  const citasPorEstado = {
    Confirmada: mockCitas.filter(c => c.estado === 'Confirmada').length,
    Pendiente: mockCitas.filter(c => c.estado === 'Pendiente').length,
    Atendida: mockCitas.filter(c => c.estado === 'Atendida').length,
    Cancelada: mockCitas.filter(c => c.estado === 'Cancelada').length,
  };

  const dataEstados = Object.entries(citasPorEstado).map(([name, value]) => ({
    name,
    value
  }));

  const atencionesVet = mockUsuarios
    .filter(u => u.rol === 'VET')
    .map(vet => ({
      nombre: vet.nombre.split(' ')[0],
      consultas: mockConsultas.filter(c => c.profesionalId === vet.id).length,
    }));

  const pacientesPorEspecie = {
    Canino: mockPacientes.filter(p => p.especie === 'Canino').length,
    Felino: mockPacientes.filter(p => p.especie === 'Felino').length,
    Otro: mockPacientes.filter(p => p.especie === 'Otro').length,
  };

  const dataEspecies = Object.entries(pacientesPorEspecie).map(([name, value]) => ({
    name,
    value
  }));

  // Datos de tendencia (simulados)
  const tendenciaCitas = [
    { mes: 'Ene', citas: 45 },
    { mes: 'Feb', citas: 52 },
    { mes: 'Mar', citas: 48 },
    { mes: 'Abr', citas: 61 },
    { mes: 'May', citas: 55 },
    { mes: 'Jun', citas: 67 },
  ];

  const handleExport = () => {
    toast.success('Reporte exportado a CSV (simulado)');
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Reportes Operativos</h1>
          <p className="text-muted-foreground mt-1">Análisis y estadísticas de la clínica</p>
        </div>
        <div className="flex gap-3">
          <Select value={periodo} onValueChange={setPeriodo}>
            <SelectTrigger className="w-[180px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="hoy">Hoy</SelectItem>
              <SelectItem value="semana">Esta Semana</SelectItem>
              <SelectItem value="mes">Este Mes</SelectItem>
              <SelectItem value="año">Este Año</SelectItem>
            </SelectContent>
          </Select>
          <Button onClick={handleExport} className="gap-2">
            <Download className="h-4 w-4" />
            Exportar CSV
          </Button>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Total Citas
            </CardTitle>
            <Calendar className="h-4 w-4 text-primary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockCitas.length}</div>
            <p className="text-xs text-muted-foreground mt-1">+12% vs mes anterior</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Consultas
            </CardTitle>
            <TrendingUp className="h-4 w-4 text-secondary" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockConsultas.length}</div>
            <p className="text-xs text-muted-foreground mt-1">+8% vs mes anterior</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Pacientes
            </CardTitle>
            <Dog className="h-4 w-4 text-info" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{mockPacientes.length}</div>
            <p className="text-xs text-muted-foreground mt-1">+5% vs mes anterior</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Veterinarios
            </CardTitle>
            <Users className="h-4 w-4 text-warning" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {mockUsuarios.filter(u => u.rol === 'VET').length}
            </div>
            <p className="text-xs text-muted-foreground mt-1">Activos en el sistema</p>
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <BarChart3 className="h-5 w-5 text-primary" />
              Citas por Estado
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={dataEstados}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                <XAxis dataKey="name" className="text-xs" />
                <YAxis className="text-xs" />
                <Tooltip />
                <Bar dataKey="value" fill="hsl(var(--primary))" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp className="h-5 w-5 text-secondary" />
              Tendencia de Citas
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={tendenciaCitas}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                <XAxis dataKey="mes" className="text-xs" />
                <YAxis className="text-xs" />
                <Tooltip />
                <Line type="monotone" dataKey="citas" stroke="hsl(var(--secondary))" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Dog className="h-5 w-5 text-info" />
              Pacientes por Especie
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={dataEspecies}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {dataEspecies.map((_entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Users className="h-5 w-5 text-secondary" />
              Atenciones por Veterinario
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={atencionesVet} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
                <XAxis type="number" className="text-xs" />
                <YAxis dataKey="nombre" type="category" className="text-xs" width={80} />
                <Tooltip />
                <Bar dataKey="consultas" fill="hsl(var(--secondary))" radius={[0, 8, 8, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Top Motivos de Consulta</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="flex items-center justify-between p-3 rounded-lg bg-accent/50">
              <span className="text-sm font-medium">Vacunación</span>
              <Badge>35%</Badge>
            </div>
            <div className="flex items-center justify-between p-3 rounded-lg bg-accent/50">
              <span className="text-sm font-medium">Consulta General</span>
              <Badge>28%</Badge>
            </div>
            <div className="flex items-center justify-between p-3 rounded-lg bg-accent/50">
              <span className="text-sm font-medium">Control</span>
              <Badge>20%</Badge>
            </div>
            <div className="flex items-center justify-between p-3 rounded-lg bg-accent/50">
              <span className="text-sm font-medium">Desparasitación</span>
              <Badge>17%</Badge>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
