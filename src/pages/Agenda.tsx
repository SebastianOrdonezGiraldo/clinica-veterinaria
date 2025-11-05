import { useState } from 'react';
import { Calendar as CalendarIcon, Clock, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { mockCitas, mockPacientes, mockPropietarios } from '@/lib/mockData';

const statusColors = {
  Confirmada: 'bg-status-confirmed/10 text-status-confirmed border-status-confirmed/20',
  Pendiente: 'bg-status-pending/10 text-status-pending border-status-pending/20',
  Cancelada: 'bg-status-cancelled/10 text-status-cancelled border-status-cancelled/20',
  Atendida: 'bg-status-completed/10 text-status-completed border-status-completed/20',
};

export default function Agenda() {
  const [selectedDate] = useState(new Date());

  const citasConDetalles = mockCitas.map(cita => ({
    ...cita,
    paciente: mockPacientes.find(p => p.id === cita.pacienteId),
    propietario: mockPropietarios.find(p => p.id === cita.propietarioId),
  }));

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-foreground">Agenda</h1>
          <p className="text-muted-foreground mt-1">Gestión de citas y calendario</p>
        </div>
        <Button className="gap-2" onClick={() => navigate('/agenda/nuevo')}>
          <Plus className="h-4 w-4" />
          Nueva Cita
        </Button>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CalendarIcon className="h-5 w-5 text-primary" />
              Calendario
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-center p-8 border rounded-lg bg-accent/50">
              <div className="text-6xl font-bold text-primary">
                {selectedDate.getDate()}
              </div>
              <div className="text-lg text-muted-foreground mt-2">
                {selectedDate.toLocaleDateString('es-ES', { month: 'long', year: 'numeric' })}
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5 text-primary" />
              Citas del Día
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {citasConDetalles.map((cita) => (
                <div
                  key={cita.id}
                  className="flex items-center gap-4 p-4 rounded-lg border border-border hover:bg-accent/50 transition-colors cursor-pointer"
                >
                  <div className="flex flex-col items-center justify-center w-20 h-20 rounded-lg bg-primary/10 flex-shrink-0">
                    <span className="text-xs text-muted-foreground">Hora</span>
                    <span className="text-lg font-bold text-primary">
                      {new Date(cita.fecha).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h4 className="font-semibold text-foreground">{cita.paciente?.nombre}</h4>
                      <Badge variant="outline" className="text-xs">
                        {cita.paciente?.especie}
                      </Badge>
                    </div>
                    <p className="text-sm text-muted-foreground truncate">
                      Propietario: {cita.propietario?.nombre}
                    </p>
                    {cita.motivo && (
                      <p className="text-sm text-muted-foreground mt-1">{cita.motivo}</p>
                    )}
                  </div>
                  <Badge className={statusColors[cita.estado as keyof typeof statusColors]}>
                    {cita.estado}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
