import { useState, useEffect } from 'react';
import { BarChart3, Download, Calendar, TrendingUp, Users, Dog, AlertCircle } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Button } from '@shared/components/ui/button';
import { Badge } from '@shared/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@shared/components/ui/select';
import { Skeleton } from '@shared/components/ui/skeleton';
import { toast } from 'sonner';
import { Bar, BarChart, Line, LineChart, Pie, PieChart, Cell, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { reporteService, PeriodoReporte, ReporteDTO } from '@features/reportes/services/reporteService';

const COLORS = ['hsl(var(--primary))', 'hsl(var(--secondary))', 'hsl(var(--info))', 'hsl(var(--warning))'];

export default function Reportes() {
  const [periodo, setPeriodo] = useState<PeriodoReporte>('mes');
  const [reporte, setReporte] = useState<ReporteDTO | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadReporte();
  }, [periodo]);

  const loadReporte = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await reporteService.generarReporte(periodo);
      setReporte(data);
    } catch (error: any) {
      console.error('Error al cargar reporte:', error);
      const errorMessage = error?.response?.data?.message || 'Error al cargar el reporte';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  const handleExport = () => {
    if (!reporte) return;
    
    // Generar CSV simple
    const csv = [
      ['Reporte Operativo', `Periodo: ${periodo}`],
      [''],
      ['Estadísticas Generales'],
      ['Total Citas', reporte.totalCitas],
      ['Total Consultas', reporte.totalConsultas],
      ['Total Pacientes', reporte.totalPacientes],
      ['Total Veterinarios', reporte.totalVeterinarios],
      [''],
      ['Citas por Estado'],
      ...reporte.citasPorEstado.map(c => [c.estado, c.cantidad]),
      [''],
      ['Tendencia de Citas'],
      ...reporte.tendenciaCitas.map(t => [t.mes, t.citas]),
    ].map(row => row.join(',')).join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `reporte_${periodo}_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    toast.success('Reporte exportado exitosamente');
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-10 w-64" />
        <div className="grid gap-4 md:grid-cols-4">
          {[1, 2, 3, 4].map((i) => (
            <Card key={`skeleton-${i}`}>
              <CardHeader>
                <Skeleton className="h-4 w-24" />
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

  if (error || !reporte) {
    return (
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold text-foreground">Reportes Operativos</h1>
            <p className="text-muted-foreground mt-1">Análisis y estadísticas de la clínica</p>
          </div>
        </div>
        <Card className="border-destructive">
          <CardContent className="flex flex-col items-center justify-center py-16">
            <div className="rounded-full bg-destructive/10 p-4 mb-4">
              <AlertCircle className="h-8 w-8 text-destructive" />
            </div>
            <h3 className="text-lg font-semibold text-foreground mb-2">Error al cargar reporte</h3>
            <p className="text-sm text-muted-foreground text-center max-w-sm mb-4">
              {error || 'No se pudo cargar el reporte. Por favor, intenta nuevamente.'}
            </p>
            <Button onClick={loadReporte} variant="outline">
              Reintentar
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  // Preparar datos para gráficos
  const dataEstados = reporte.citasPorEstado.map(c => ({
    name: c.estado === 'PENDIENTE' ? 'Pendiente' :
          c.estado === 'CONFIRMADA' ? 'Confirmada' :
          c.estado === 'ATENDIDA' ? 'Atendida' :
          c.estado === 'CANCELADA' ? 'Cancelada' : c.estado,
    value: c.cantidad
  }));

  const dataEspecies = reporte.pacientesPorEspecie.map(p => ({
    name: p.especie,
    value: p.cantidad
  }));

  const atencionesVet = reporte.atencionesPorVeterinario;

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Reportes Operativos</h1>
          <p className="text-muted-foreground mt-1">Análisis y estadísticas de la clínica</p>
        </div>
        <div className="flex gap-3">
          <Select value={periodo} onValueChange={(value) => setPeriodo(value as PeriodoReporte)}>
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
            <div className="text-2xl font-bold">{reporte.totalCitas}</div>
            <p className="text-xs text-muted-foreground mt-1">Total de citas registradas</p>
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
            <div className="text-2xl font-bold">{reporte.totalConsultas}</div>
            <p className="text-xs text-muted-foreground mt-1">Total de consultas realizadas</p>
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
            <div className="text-2xl font-bold">{reporte.totalPacientes}</div>
            <p className="text-xs text-muted-foreground mt-1">Pacientes activos</p>
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
            <div className="text-2xl font-bold">{reporte.totalVeterinarios}</div>
            <p className="text-xs text-muted-foreground mt-1">Veterinarios activos</p>
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
              <LineChart data={reporte.tendenciaCitas}>
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
                  {dataEspecies.map((entry, index) => (
                    <Cell key={`cell-${entry.name}`} fill={COLORS[index % COLORS.length]} />
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
          {reporte.topMotivosConsulta.length > 0 ? (
            <div className="space-y-3">
              {reporte.topMotivosConsulta.map((motivo) => (
                <div key={motivo.motivo} className="flex items-center justify-between p-3 rounded-lg bg-accent/50">
                  <span className="text-sm font-medium">{motivo.motivo}</span>
                  <div className="flex items-center gap-2">
                    <Badge>{motivo.cantidad}</Badge>
                    <Badge variant="outline">{motivo.porcentaje.toFixed(1)}%</Badge>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground text-center py-4">
              No hay datos de motivos de consulta para el periodo seleccionado
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
