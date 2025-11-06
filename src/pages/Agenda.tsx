import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Calendar as CalendarIcon, Clock, Plus, Filter } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Calendar } from '@/components/ui/calendar';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';
import { citaService } from '@/services/citaService';
import { pacienteService } from '@/services/pacienteService';
import { propietarioService } from '@/services/propietarioService';
import { usuarioService } from '@/services/usuarioService';
import { Cita, Paciente, Propietario, Usuario } from '@/types';
import { toast } from 'sonner';

const statusColors = {
  CONFIRMADA: 'bg-status-confirmed/10 text-status-confirmed border-status-confirmed/20',
  PROGRAMADA: 'bg-status-pending/10 text-status-pending border-status-pending/20',
  CANCELADA: 'bg-status-cancelled/10 text-status-cancelled border-status-cancelled/20',
  COMPLETADA: 'bg-status-completed/10 text-status-completed border-status-completed/20',
  EN_CURSO: 'bg-status-confirmed/10 text-status-confirmed border-status-confirmed/20',
  NO_ASISTIO: 'bg-status-cancelled/10 text-status-cancelled border-status-cancelled/20',
};

export default function Agenda() {
  const navigate = useNavigate();
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [filtroVet, setFiltroVet] = useState<string>('todos');
  const [filtroEstado, setFiltroEstado] = useState<string>('todos');
  const [citas, setCitas] = useState<Cita[]>([]);
  const [pacientes, setPacientes] = useState<Paciente[]>([]);
  const [propietarios, setPropietarios] = useState<Propietario[]>([]);
  const [veterinarios, setVeterinarios] = useState<Usuario[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setIsLoading(true);
      const [citasData, pacientesData, propietariosData, usuariosData] = await Promise.all([
        citaService.getAll(),
        pacienteService.getAll(),
        propietarioService.getAll(),
        usuarioService.getAll(),
      ]);
      setCitas(citasData);
      setPacientes(pacientesData);
      setPropietarios(propietariosData);
      setVeterinarios(usuariosData.filter(u => u.rol === 'VET'));
    } catch (error) {
      console.error('Error al cargar datos:', error);
      toast.error('Error al cargar las citas');
    } finally {
      setIsLoading(false);
    }
  };

  let citasFiltradas = citas.map(cita => ({
    ...cita,
    paciente: pacientes.find(p => p.id === cita.pacienteId),
    propietario: propietarios.find(p => pacientes.find(pac => pac.id === cita.pacienteId)?.propietarioId === p.id),
  }));

  // Filtrar por fecha
  citasFiltradas = citasFiltradas.filter(cita => {
    const citaDate = new Date(cita.fecha);
    return citaDate.toDateString() === selectedDate.toDateString();
  });

  // Filtrar por veterinario
  if (filtroVet !== 'todos') {
    citasFiltradas = citasFiltradas.filter(cita => cita.profesionalId === filtroVet);
  }

  // Filtrar por estado
  if (filtroEstado !== 'todos') {
    citasFiltradas = citasFiltradas.filter(cita => cita.estado === filtroEstado);
  }

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

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="h-5 w-5 text-primary" />
            Filtros
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-3">
            <div>
              <label className="text-sm font-medium mb-2 block">Fecha</label>
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="outline" className="w-full justify-start">
                    <CalendarIcon className="mr-2 h-4 w-4" />
                    {format(selectedDate, 'PPP', { locale: es })}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0">
                  <Calendar
                    mode="single"
                    selected={selectedDate}
                    onSelect={(date) => date && setSelectedDate(date)}
                    initialFocus
                  />
                </PopoverContent>
              </Popover>
            </div>
            <div>
              <label className="text-sm font-medium mb-2 block">Veterinario</label>
              <Select value={filtroVet} onValueChange={setFiltroVet}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Todos</SelectItem>
                  {veterinarios.map(vet => (
                    <SelectItem key={vet.id} value={vet.id}>{vet.nombre}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <label className="text-sm font-medium mb-2 block">Estado</label>
              <Select value={filtroEstado} onValueChange={setFiltroEstado}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="todos">Todos</SelectItem>
                  <SelectItem value="PROGRAMADA">Programada</SelectItem>
                  <SelectItem value="CONFIRMADA">Confirmada</SelectItem>
                  <SelectItem value="EN_CURSO">En Curso</SelectItem>
                  <SelectItem value="COMPLETADA">Completada</SelectItem>
                  <SelectItem value="CANCELADA">Cancelada</SelectItem>
                  <SelectItem value="NO_ASISTIO">No Asistió</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card className="lg:col-span-1">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <CalendarIcon className="h-5 w-5 text-primary" />
              Calendario
            </CardTitle>
          </CardHeader>
          <CardContent>
            <Calendar
              mode="single"
              selected={selectedDate}
              onSelect={(date) => date && setSelectedDate(date)}
              className="rounded-md border"
            />
          </CardContent>
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5 text-primary" />
              Citas del Día ({citasFiltradas.length})
            </CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="text-center py-12">
                <p className="text-muted-foreground">Cargando citas...</p>
              </div>
            ) : citasFiltradas.length > 0 ? (
              <div className="space-y-3">
                {citasFiltradas.map((cita) => (
                <div
                  key={cita.id}
                  className="flex items-center gap-4 p-4 rounded-lg border border-border hover:bg-accent/50 transition-colors cursor-pointer"
                  onClick={() => navigate(`/agenda/${cita.id}`)}
                >
                  <div className="flex flex-col items-center justify-center w-20 h-20 rounded-lg bg-primary/10 flex-shrink-0">
                    <span className="text-xs text-muted-foreground">Hora</span>
                    <span className="text-lg font-bold text-primary">
                      {new Date(cita.fecha).toLocaleTimeString('es-ES', { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <h4 className="font-semibold text-foreground">{cita.paciente?.nombre || 'N/A'}</h4>
                      <Badge variant="outline" className="text-xs">
                        {cita.paciente?.especie || 'N/A'}
                      </Badge>
                    </div>
                    <p className="text-sm text-muted-foreground truncate">
                      Propietario: {cita.propietario?.nombre || 'N/A'}
                    </p>
                    {cita.motivo && (
                      <p className="text-sm text-muted-foreground mt-1">{cita.motivo}</p>
                    )}
                  </div>
                  <Badge className={statusColors[cita.estado as keyof typeof statusColors]}>
                    {cita.estado.replace(/_/g, ' ')}
                  </Badge>
                </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-12">
                <Clock className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <h3 className="text-lg font-medium text-foreground">No hay citas para esta fecha</h3>
                <p className="text-muted-foreground mt-1">Selecciona otra fecha o ajusta los filtros</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
