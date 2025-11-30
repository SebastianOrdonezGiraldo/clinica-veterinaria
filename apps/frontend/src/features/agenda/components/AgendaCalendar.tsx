import { useState, useMemo } from 'react';
import { format, startOfWeek, endOfWeek, startOfMonth, endOfMonth, eachDayOfInterval, 
  isSameDay, isSameMonth, addWeeks, subWeeks, addMonths, subMonths, addDays, subDays,
  startOfDay, getHours, getMinutes, setHours, setMinutes } from 'date-fns';
import { es } from 'date-fns/locale';
import { Calendar as CalendarIcon, ChevronLeft, ChevronRight, LayoutGrid, CalendarDays, Clock } from 'lucide-react';
import { Button } from '@shared/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@shared/components/ui/card';
import { Badge } from '@shared/components/ui/badge';
import { Tabs, TabsList, TabsTrigger } from '@shared/components/ui/tabs';
import { Cita } from '@core/types';
import { cn } from '@shared/utils/utils';
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragOverlay,
} from '@dnd-kit/core';
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import CitaDraggable from './CitaDraggable';

export type VistaCalendario = 'mes' | 'semana' | 'dia';

interface AgendaCalendarProps {
  citas: Cita[];
  fechaSeleccionada: Date;
  onFechaChange: (fecha: Date) => void;
  onCitaClick?: (cita: Cita) => void;
  onCitaDrag?: (citaId: string, nuevaFecha: Date) => void;
  veterinarioId?: string;
  className?: string;
}

const statusColors = {
  CONFIRMADA: 'bg-status-confirmed/10 text-status-confirmed border-status-confirmed/20',
  PENDIENTE: 'bg-status-pending/10 text-status-pending border-status-pending/20',
  EN_PROCESO: 'bg-blue-500/10 text-blue-500 border-blue-500/20',
  COMPLETADA: 'bg-status-completed/10 text-status-completed border-status-completed/20',
  CANCELADA: 'bg-status-cancelled/10 text-status-cancelled border-status-cancelled/20',
  ATENDIDA: 'bg-status-completed/10 text-status-completed border-status-completed/20',
};

const horasDelDia = Array.from({ length: 24 }, (_, i) => i);

export default function AgendaCalendar({
  citas,
  fechaSeleccionada,
  onFechaChange,
  onCitaClick,
  onCitaDrag,
  veterinarioId,
  className,
}: AgendaCalendarProps) {
  const [vista, setVista] = useState<VistaCalendario>('semana');
  const [fechaVista, setFechaVista] = useState<Date>(fechaSeleccionada);
  const [activeId, setActiveId] = useState<string | null>(null);

  // Sensores para drag & drop
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  // Filtrar citas por veterinario si se especifica
  const citasFiltradas = useMemo(() => {
    if (!veterinarioId || veterinarioId === 'todos') {
      return citas;
    }
    return citas.filter(cita => cita.profesionalId === veterinarioId);
  }, [citas, veterinarioId]);

  // Agrupar citas por fecha
  const citasPorFecha = useMemo(() => {
    const agrupadas = new Map<string, Cita[]>();
    citasFiltradas.forEach(cita => {
      const fecha = new Date(cita.fecha);
      const fechaStr = format(fecha, 'yyyy-MM-dd');
      if (!agrupadas.has(fechaStr)) {
        agrupadas.set(fechaStr, []);
      }
      agrupadas.get(fechaStr)!.push(cita);
    });
    return agrupadas;
  }, [citasFiltradas]);

  // Navegación de fechas
  const navegarAnterior = () => {
    if (vista === 'mes') {
      setFechaVista(subMonths(fechaVista, 1));
    } else if (vista === 'semana') {
      setFechaVista(subWeeks(fechaVista, 1));
    } else {
      setFechaVista(subDays(fechaVista, 1));
    }
  };

  const navegarSiguiente = () => {
    if (vista === 'mes') {
      setFechaVista(addMonths(fechaVista, 1));
    } else if (vista === 'semana') {
      setFechaVista(addWeeks(fechaVista, 1));
    } else {
      setFechaVista(addDays(fechaVista, 1));
    }
  };

  const irAHoy = () => {
    const hoy = new Date();
    setFechaVista(hoy);
    onFechaChange(hoy);
  };

  // Renderizar vista mensual
  const renderVistaMensual = () => {
    const inicioMes = startOfMonth(fechaVista);
    const finMes = endOfMonth(fechaVista);
    const inicioSemana = startOfWeek(inicioMes, { locale: es });
    const finSemana = endOfWeek(finMes, { locale: es });
    const dias = eachDayOfInterval({ start: inicioSemana, end: finSemana });

    const diasSemana = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

    return (
      <div className="space-y-2">
        {/* Encabezado de días de la semana */}
        <div className="grid grid-cols-7 gap-1">
          {diasSemana.map(dia => (
            <div key={dia} className="text-center text-sm font-medium text-muted-foreground p-2">
              {dia}
            </div>
          ))}
        </div>

        {/* Días del mes */}
        <div className="grid grid-cols-7 gap-1">
          {dias.map(dia => {
            const fechaStr = format(dia, 'yyyy-MM-dd');
            const citasDelDia = citasPorFecha.get(fechaStr) || [];
            const esHoy = isSameDay(dia, new Date());
            const esMesActual = isSameMonth(dia, fechaVista);
            const esSeleccionado = isSameDay(dia, fechaSeleccionada);

            return (
              <div
                key={dia.toISOString()}
                className={cn(
                  "min-h-[80px] p-1 border rounded-md cursor-pointer transition-colors",
                  !esMesActual && "opacity-40",
                  esHoy && "border-primary border-2",
                  esSeleccionado && "bg-primary/10",
                  "hover:bg-accent"
                )}
                onClick={() => {
                  setFechaVista(dia);
                  onFechaChange(dia);
                }}
              >
                <div className={cn(
                  "text-sm font-medium mb-1",
                  esHoy && "text-primary font-bold"
                )}>
                  {format(dia, 'd')}
                </div>
                <div className="space-y-1">
                  {citasDelDia.slice(0, 3).map(cita => (
                    <div
                      key={cita.id}
                      className={cn(
                        "text-xs p-1 rounded truncate",
                        statusColors[cita.estado as keyof typeof statusColors] || "bg-gray-100"
                      )}
                      onClick={(e) => {
                        e.stopPropagation();
                        onCitaClick?.(cita);
                      }}
                    >
                      {new Date(cita.fecha).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })} - {cita.pacienteNombre}
                    </div>
                  ))}
                  {citasDelDia.length > 3 && (
                    <div className="text-xs text-muted-foreground">
                      +{citasDelDia.length - 3} más
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  // Renderizar vista semanal
  const renderVistaSemanal = () => {
    const inicioSemana = startOfWeek(fechaVista, { locale: es });
    const finSemana = endOfWeek(fechaVista, { locale: es });
    const dias = eachDayOfInterval({ start: inicioSemana, end: finSemana });

    return (
      <div className="space-y-2">
        {/* Encabezado con días */}
        <div className="grid grid-cols-7 gap-2 mb-4">
          {dias.map(dia => {
            const fechaStr = format(dia, 'yyyy-MM-dd');
            const citasDelDia = citasPorFecha.get(fechaStr) || [];
            const esHoy = isSameDay(dia, new Date());
            const esSeleccionado = isSameDay(dia, fechaSeleccionada);

            return (
              <div
                key={dia.toISOString()}
                className={cn(
                  "p-3 border rounded-lg cursor-pointer transition-colors",
                  esHoy && "border-primary border-2 bg-primary/5",
                  esSeleccionado && !esHoy && "bg-accent",
                  "hover:bg-accent/50"
                )}
                onClick={() => {
                  setFechaVista(dia);
                  onFechaChange(dia);
                }}
              >
                <div className="text-center">
                  <div className={cn(
                    "text-sm font-medium mb-1",
                    esHoy && "text-primary font-bold"
                  )}>
                    {format(dia, 'EEE', { locale: es })}
                  </div>
                  <div className={cn(
                    "text-2xl font-bold mb-2",
                    esHoy && "text-primary"
                  )}>
                    {format(dia, 'd')}
                  </div>
                  <Badge variant="secondary" className="text-xs">
                    {citasDelDia.length} {citasDelDia.length === 1 ? 'cita' : 'citas'}
                  </Badge>
                </div>
              </div>
            );
          })}
        </div>

        {/* Citas por día en formato de lista */}
        <div className="space-y-4">
          {dias.map(dia => {
            const fechaStr = format(dia, 'yyyy-MM-dd');
            const citasDelDia = citasPorFecha.get(fechaStr) || [];
            const esSeleccionado = isSameDay(dia, fechaSeleccionada);

            if (!esSeleccionado && citasDelDia.length === 0) return null;

            return (
              <Card key={dia.toISOString()} className={cn(esSeleccionado && "border-primary")}>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm">
                    {format(dia, 'EEEE, d \'de\' MMMM', { locale: es })}
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  {citasDelDia.length > 0 ? (
                    <div className="space-y-2">
                      {citasDelDia
                        .sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime())
                        .map(cita => (
                          <div
                            key={cita.id}
                            className={cn(
                              "p-3 rounded-lg border cursor-pointer hover:shadow-md transition-shadow",
                              statusColors[cita.estado as keyof typeof statusColors] || "bg-gray-100",
                              "flex items-center justify-between"
                            )}
                            onClick={() => onCitaClick?.(cita)}
                          >
                            <div className="flex-1">
                              <div className="flex items-center gap-2 mb-1">
                                <Clock className="h-4 w-4 text-muted-foreground" />
                                <span className="font-semibold">
                                  {new Date(cita.fecha).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}
                                </span>
                                <Badge className={statusColors[cita.estado as keyof typeof statusColors]}>
                                  {cita.estado}
                                </Badge>
                              </div>
                              <div className="text-sm font-medium">{cita.pacienteNombre}</div>
                              <div className="text-xs text-muted-foreground">{cita.propietarioNombre}</div>
                              {cita.motivo && (
                                <div className="text-xs text-muted-foreground mt-1">{cita.motivo}</div>
                              )}
                            </div>
                          </div>
                        ))}
                    </div>
                  ) : (
                    <div className="text-center py-4 text-muted-foreground text-sm">
                      No hay citas programadas
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      </div>
    );
  };

  // Manejar inicio de drag
  const handleDragStart = (event: any) => {
    setActiveId(event.active.id);
  };

  // Manejar fin de drag
  const handleDragEnd = (event: any) => {
    const { active, over } = event;
    setActiveId(null);

    if (!over || !onCitaDrag) return;

    const cita = citasFiltradas.find(c => c.id === active.id);
    if (!cita) return;

    // Obtener la hora del drop target (si tiene data-hora)
    const horaTarget = over.data?.current?.hora;
    if (horaTarget !== undefined) {
      const nuevaFecha = setHours(setMinutes(startOfDay(fechaVista), getMinutes(new Date(cita.fecha))), horaTarget);
      onCitaDrag(active.id, nuevaFecha);
    }
  };

  // Renderizar vista diaria con drag & drop
  const renderVistaDiaria = () => {
    const fechaStr = format(fechaVista, 'yyyy-MM-dd');
    const citasDelDia = citasPorFecha.get(fechaStr) || [];
    const citasPorHora = new Map<number, Cita[]>();

    citasDelDia.forEach(cita => {
      const fecha = new Date(cita.fecha);
      const hora = getHours(fecha);
      if (!citasPorHora.has(hora)) {
        citasPorHora.set(hora, []);
      }
      citasPorHora.get(hora)!.push(cita);
    });

    const citasIds = citasDelDia.map(c => c.id);
    const citaActiva = activeId ? citasDelDia.find(c => c.id === activeId) : null;

    return (
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
        <div className="space-y-2">
          <Card>
            <CardHeader>
              <CardTitle>
                {format(fechaVista, 'EEEE, d \'de\' MMMM \'de\' yyyy', { locale: es })}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <SortableContext items={citasIds} strategy={verticalListSortingStrategy}>
                <div className="space-y-1 max-h-[600px] overflow-y-auto">
                  {horasDelDia.map(hora => {
                    const citasHora = citasPorHora.get(hora) || [];
                    const esHoraPasada = hora < new Date().getHours() && isSameDay(fechaVista, new Date());

                    return (
                      <div
                        key={hora}
                        className={cn(
                          "grid grid-cols-12 gap-2 p-2 border-b",
                          esHoraPasada && "opacity-50"
                        )}
                        data-hora={hora}
                      >
                        <div className="col-span-1 text-sm font-medium text-muted-foreground">
                          {hora.toString().padStart(2, '0')}:00
                        </div>
                        <div className="col-span-11 space-y-2">
                          {citasHora
                            .sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime())
                            .map(cita => (
                              <CitaDraggable
                                key={cita.id}
                                cita={cita}
                                onClick={() => onCitaClick?.(cita)}
                              />
                            ))}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </SortableContext>
              {citasDelDia.length === 0 && (
                <div className="text-center py-8 text-muted-foreground">
                  No hay citas programadas para este día
                </div>
              )}
            </CardContent>
          </Card>
        </div>
        <DragOverlay>
          {citaActiva ? (
            <CitaDraggable cita={citaActiva} />
          ) : null}
        </DragOverlay>
      </DndContext>
    );
  };

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <CalendarIcon className="h-5 w-5 text-primary" />
            Calendario
          </CardTitle>
          <div className="flex items-center gap-2">
            {/* Selector de vista */}
            <Tabs value={vista} onValueChange={(v) => setVista(v as VistaCalendario)}>
              <TabsList>
                <TabsTrigger value="mes" className="gap-2">
                  <LayoutGrid className="h-4 w-4" />
                  Mes
                </TabsTrigger>
                <TabsTrigger value="semana" className="gap-2">
                  <CalendarDays className="h-4 w-4" />
                  Semana
                </TabsTrigger>
                <TabsTrigger value="dia" className="gap-2">
                  <Clock className="h-4 w-4" />
                  Día
                </TabsTrigger>
              </TabsList>
            </Tabs>

            {/* Navegación */}
            <div className="flex items-center gap-2">
              <Button variant="outline" size="icon" onClick={navegarAnterior}>
                <ChevronLeft className="h-4 w-4" />
              </Button>
              <Button variant="outline" onClick={irAHoy}>
                Hoy
              </Button>
              <Button variant="outline" size="icon" onClick={navegarSiguiente}>
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {vista === 'mes' && renderVistaMensual()}
        {vista === 'semana' && renderVistaSemanal()}
        {vista === 'dia' && renderVistaDiaria()}
      </CardContent>
    </Card>
  );
}

