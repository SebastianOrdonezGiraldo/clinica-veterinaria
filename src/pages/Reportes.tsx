import { useState } from 'react';
import { BarChart3, Download, Calendar, TrendingUp, Users, Dog } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { mockCitas, mockPacientes, mockConsultas, mockUsuarios } from '@/lib/mockData';
import { toast } from 'sonner';

export default function Reportes() {
  const [dateRange] = useState({ start: '2024-01-01', end: '2024-12-31' });

  // Estadísticas
  const citasPorEstado = {
    Confirmada: mockCitas.filter(c => c.estado === 'Confirmada').length,
    Pendiente: mockCitas.filter(c => c.estado === 'Pendiente').length,
    Atendida: mockCitas.filter(c => c.estado === 'Atendida').length,
    Cancelada: mockCitas.filter(c => c.estado === 'Cancelada').length,
  };

  const atencionesVet = mockUsuarios
    .filter(u => u.rol === 'VET')
    .map(vet => ({
      nombre: vet.nombre,
      consultas: mockConsultas.filter(c => c.profesionalId === vet.id).length,
    }));

  const pacientesPorEspecie = {
    Canino: mockPacientes.filter(p => p.especie === 'Canino').length,
    Felino: mockPacientes.filter(p => p.especie === 'Felino').length,
    Otro: mockPacientes.filter(p => p.especie === 'Otro').length,
  };

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
        <Button onClick={handleExport} className="gap-2">
          <Download className="h-4 w-4" />
          Exportar CSV
        </Button>
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
            <p className="text-xs text-muted-foreground mt-1">Este período</p>
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
            <p className="text-xs text-muted-foreground mt-1">Registradas</p>
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
            <p className="text-xs text-muted-foreground mt-1">Activos</p>
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
            <p className="text-xs text-muted-foreground mt-1">En el sistema</p>
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
            <div className="space-y-4">
              {Object.entries(citasPorEstado).map(([estado, cantidad]) => (
                <div key={estado}>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium">{estado}</span>
                    <span className="text-sm font-bold">{cantidad}</span>
                  </div>
                  <div className="w-full bg-muted rounded-full h-2">
                    <div
                      className="bg-primary rounded-full h-2 transition-all"
                      style={{ width: `${(cantidad / mockCitas.length) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
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
            <div className="space-y-3">
              {atencionesVet.map((vet) => (
                <div
                  key={vet.nombre}
                  className="flex items-center justify-between p-3 rounded-lg border border-border"
                >
                  <span className="font-medium">{vet.nombre}</span>
                  <Badge variant="outline" className="bg-secondary/10 text-secondary border-secondary/20">
                    {vet.consultas} consultas
                  </Badge>
                </div>
              ))}
            </div>
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
            <div className="space-y-4">
              {Object.entries(pacientesPorEspecie).map(([especie, cantidad]) => (
                <div key={especie}>
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium">{especie}</span>
                    <span className="text-sm font-bold">{cantidad}</span>
                  </div>
                  <div className="w-full bg-muted rounded-full h-2">
                    <div
                      className="bg-info rounded-full h-2 transition-all"
                      style={{ width: `${(cantidad / mockPacientes.length) * 100}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

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
    </div>
  );
}
