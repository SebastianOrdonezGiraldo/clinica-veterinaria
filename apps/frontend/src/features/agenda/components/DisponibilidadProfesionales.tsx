import { useMemo } from 'react';
import { format, startOfDay, endOfDay, addDays, isSameDay, getHours, getMinutes } from 'date-fns';
import { es } from 'date-fns/locale';
import { Clock, User } from 'lucide-react';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Cita, Usuario } from '@core/types';
import { cn } from '@shared/utils/utils';

interface DisponibilidadProfesionalesProps {
  citas: Cita[];
  veterinarios: Usuario[];
  fechaSeleccionada: Date;
  className?: string;
}

const horasLaborales = Array.from({ length: 12 }, (_, i) => i + 8); // 8:00 a 19:00

export default function DisponibilidadProfesionales({
  citas,
  veterinarios,
  fechaSeleccionada,
  className,
}: DisponibilidadProfesionalesProps) {
  // Agrupar citas por veterinario y hora
  const disponibilidad = useMemo(() => {
    const fechaStr = format(fechaSeleccionada, 'yyyy-MM-dd');
    const citasDelDia = citas.filter(cita => {
      const fechaCita = format(new Date(cita.fecha), 'yyyy-MM-dd');
      return fechaCita === fechaStr;
    });

    const disponibilidadMap = new Map<string, Map<number, number>>();

    veterinarios.forEach(vet => {
      const horasMap = new Map<number, number>();
      horasLaborales.forEach(hora => {
        horasMap.set(hora, 0);
      });
      disponibilidadMap.set(vet.id, horasMap);
    });

    citasDelDia.forEach(cita => {
      if (cita.profesionalId && disponibilidadMap.has(cita.profesionalId)) {
        const fecha = new Date(cita.fecha);
        const hora = getHours(fecha);
        const horasMap = disponibilidadMap.get(cita.profesionalId)!;
        const actual = horasMap.get(hora) || 0;
        horasMap.set(hora, actual + 1);
      }
    });

    return disponibilidadMap;
  }, [citas, veterinarios, fechaSeleccionada]);

  const getDisponibilidadColor = (citasEnHora: number) => {
    if (citasEnHora === 0) return 'bg-green-100 border-green-300 text-green-800';
    if (citasEnHora === 1) return 'bg-yellow-100 border-yellow-300 text-yellow-800';
    if (citasEnHora === 2) return 'bg-orange-100 border-orange-300 text-orange-800';
    return 'bg-red-100 border-red-300 text-red-800';
  };

  const getDisponibilidadTexto = (citasEnHora: number) => {
    if (citasEnHora === 0) return 'Disponible';
    if (citasEnHora === 1) return 'Ocupado';
    if (citasEnHora === 2) return 'Muy ocupado';
    return 'Sobrecargado';
  };

  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <User className="h-5 w-5 text-primary" />
          Disponibilidad de Profesionales - {format(fechaSeleccionada, 'PPP', { locale: es })}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {veterinarios.map(vet => {
            const horasMap = disponibilidad.get(vet.id) || new Map();
            const totalCitas = Array.from(horasMap.values()).reduce((sum, count) => sum + count, 0);

            return (
              <div key={vet.id} className="border rounded-lg p-4">
                <div className="flex items-center justify-between mb-3">
                  <div>
                    <h4 className="font-semibold">{vet.nombre}</h4>
                    <p className="text-sm text-muted-foreground">{vet.email}</p>
                  </div>
                  <Badge variant="secondary">
                    {totalCitas} {totalCitas === 1 ? 'cita' : 'citas'}
                  </Badge>
                </div>
                <div className="grid grid-cols-6 gap-2">
                  {horasLaborales.map(hora => {
                    const citasEnHora = horasMap.get(hora) || 0;
                    return (
                      <div
                        key={hora}
                        className={cn(
                          "p-2 rounded border text-center text-xs",
                          getDisponibilidadColor(citasEnHora)
                        )}
                      >
                        <div className="font-medium">{hora.toString().padStart(2, '0')}:00</div>
                        <div className="text-xs mt-1">
                          {citasEnHora > 0 && (
                            <Badge variant="outline" className="text-xs">
                              {citasEnHora}
                            </Badge>
                          )}
                        </div>
                        <div className="text-xs mt-1 opacity-75">
                          {citasEnHora === 0 && getDisponibilidadTexto(citasEnHora)}
                        </div>
                      </div>
                    );
                  })}
                </div>
                <div className="mt-3 flex items-center gap-4 text-xs">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded bg-green-100 border border-green-300"></div>
                    <span>Disponible</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded bg-yellow-100 border border-yellow-300"></div>
                    <span>Ocupado</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded bg-orange-100 border border-orange-300"></div>
                    <span>Muy ocupado</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 rounded bg-red-100 border border-red-300"></div>
                    <span>Sobrecargado</span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
        {veterinarios.length === 0 && (
          <div className="text-center py-8 text-muted-foreground">
            No hay veterinarios disponibles
          </div>
        )}
      </CardContent>
    </Card>
  );
}

